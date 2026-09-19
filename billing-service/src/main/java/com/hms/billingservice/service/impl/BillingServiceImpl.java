package com.hms.billingservice.service.impl;

import com.hms.billingservice.client.ReservationClient;
import com.hms.billingservice.client.RoomClient;
import com.hms.billingservice.client.dto.ReservationDto;
import com.hms.billingservice.client.dto.RoomDto;
import com.hms.billingservice.dto.request.AddBillItemRequest;
import com.hms.billingservice.dto.request.CreateBillRequest;
import com.hms.billingservice.dto.request.FinalizeBillRequest;
import com.hms.billingservice.dto.response.BillItemResponse;
import com.hms.billingservice.dto.response.BillResponse;
import com.hms.billingservice.dto.response.PrintBillResponse;
import com.hms.billingservice.entity.Bill;
import com.hms.billingservice.entity.BillItem;
import com.hms.billingservice.entity.BillItemType;
import com.hms.billingservice.entity.BillStatus;
import com.hms.billingservice.event.publisher.BillingEventPublisher;
import com.hms.billingservice.exception.BusinessRuleException;
import com.hms.billingservice.exception.ResourceNotFoundException;
import com.hms.billingservice.repository.BillItemRepository;
import com.hms.billingservice.repository.BillRepository;
import com.hms.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillingEventPublisher billingEventPublisher;
    private final ReservationClient reservationClient;
    private final RoomClient roomClient;

    @Override
    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        log.info("Creating bill for reservationId: {}, guestId: {}", request.getReservationId(), request.getGuestId());

        billRepository.findByReservationId(request.getReservationId()).ifPresent(b -> {
            throw new BusinessRuleException("A bill already exists for reservation ID: " + request.getReservationId());
        });

        // 1. Fetch Reservation details via Feign
        ReservationDto reservation = null;
        try {
            reservation = reservationClient.getReservationById(request.getReservationId());
        } catch (Exception e) {
            log.warn("Could not retrieve reservation details from reservation-service for id {}: {}", request.getReservationId(), e.getMessage());
        }

        Long guestId = request.getGuestId() != null ? request.getGuestId() : (reservation != null ? reservation.getGuestId() : null);
        Long roomId = request.getRoomId() != null ? request.getRoomId() : (reservation != null ? reservation.getRoomId() : null);
        Long hallId = request.getHallId() != null ? request.getHallId() : (reservation != null ? reservation.getHallId() : null);

        LocalDate checkInDate = request.getCheckInDate();
        if (checkInDate == null && reservation != null && reservation.getCheckInDateTime() != null) {
            checkInDate = reservation.getCheckInDateTime().toLocalDate();
        }
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }

        LocalDate checkOutDate = request.getCheckOutDate();
        if (checkOutDate == null && reservation != null && reservation.getCheckOutDateTime() != null) {
            checkOutDate = reservation.getCheckOutDateTime().toLocalDate();
        }
        if (checkOutDate == null) {
            checkOutDate = checkInDate.plusDays(1);
        }

        // 2. Determine Stay Nights
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) {
            nights = 1;
        }

        // 3. Determine Room/Hall details and Unit Price
        String resourceType = reservation != null && reservation.getResourceType() != null ? reservation.getResourceType() : "ROOM";
        String roomNumber = null;
        String categoryName = null;
        BigDecimal unitPrice = null;

        if ("HALL".equalsIgnoreCase(resourceType)) {
            if (hallId != null) {
                try {
                    Map<String, Object> hallMap = roomClient.getHallById(hallId);
                    if (hallMap != null) {
                        if (hallMap.get("name") != null) roomNumber = hallMap.get("name").toString();
                        if (hallMap.get("pricePerDay") != null) unitPrice = new BigDecimal(hallMap.get("pricePerDay").toString());
                    }
                } catch (Exception ignored) {}
            }
            if (unitPrice == null && reservation != null && reservation.getHallCategoryId() != null) {
                try {
                    Map<String, Object> hallCat = roomClient.getHallCategoryById(reservation.getHallCategoryId());
                    if (hallCat != null) {
                        if (hallCat.get("name") != null) categoryName = hallCat.get("name").toString();
                        if (hallCat.get("basePricePerDay") != null) unitPrice = new BigDecimal(hallCat.get("basePricePerDay").toString());
                    }
                } catch (Exception ignored) {}
            }
        } else {
            // Room resource
            if (roomId != null) {
                try {
                    RoomDto room = roomClient.getRoomById(roomId);
                    if (room != null) {
                        roomNumber = room.getRoomNumber();
                        if (room.getPricePerNight() != null) {
                            unitPrice = room.getPricePerNight();
                        }
                        if (room.getCategory() instanceof Map) {
                            Map<?, ?> cat = (Map<?, ?>) room.getCategory();
                            if (cat.get("name") != null) categoryName = cat.get("name").toString();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve room details for roomId {}: {}", roomId, e.getMessage());
                }
            }

            if (unitPrice == null && reservation != null && reservation.getRoomCategoryId() != null) {
                try {
                    Map<String, Object> cat = roomClient.getRoomCategoryById(reservation.getRoomCategoryId());
                    if (cat != null) {
                        if (cat.get("name") != null) categoryName = cat.get("name").toString();
                        if (cat.get("basePrice") != null) unitPrice = new BigDecimal(cat.get("basePrice").toString());
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve category details for categoryId {}: {}", reservation.getRoomCategoryId(), e.getMessage());
                }
            }
        }

        // If unitPrice is still not found, fallback to quotedAmount / nights, or a sensible standard rate
        if (unitPrice == null && reservation != null && reservation.getQuotedAmount() != null && reservation.getQuotedAmount().compareTo(BigDecimal.ZERO) > 0) {
            unitPrice = reservation.getQuotedAmount().divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
        }
        if (unitPrice == null) {
            unitPrice = new BigDecimal("100.00");
        }

        BigDecimal roomChargeTotal = unitPrice.multiply(BigDecimal.valueOf(nights));

        String billingNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Bill bill = Bill.builder()
                .billingNumber(billingNumber)
                .reservationId(request.getReservationId())
                .guestId(guestId != null ? guestId : 1L)
                .roomId(roomId)
                .hallId(hallId)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .subtotal(roomChargeTotal)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .breakageCharges(BigDecimal.ZERO)
                .lateCheckoutCharges(BigDecimal.ZERO)
                .upgradeCharges(BigDecimal.ZERO)
                .totalAmount(roomChargeTotal)
                .paidAmount(BigDecimal.ZERO)
                .outstandingAmount(roomChargeTotal)
                .status(BillStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        Bill saved = billRepository.save(bill);

        // 4. Create and Persist the initial ROOM_CHARGE bill item
        String description = String.format("%s - %s%s (%d night%s)",
                "HALL".equalsIgnoreCase(resourceType) ? "Hall Booking Charge" : "Room Charge",
                roomNumber != null ? (("HALL".equalsIgnoreCase(resourceType) ? "" : "Room ") + roomNumber) : ("Resource #" + (roomId != null ? roomId : (hallId != null ? hallId : "TBD"))),
                categoryName != null ? " (" + categoryName + ")" : "",
                nights,
                nights > 1 ? "s" : ""
        );

        BillItem initialItem = BillItem.builder()
                .bill(saved)
                .description(description)
                .itemType(BillItemType.ROOM_CHARGE)
                .quantity((int) nights)
                .unitPrice(unitPrice)
                .totalPrice(roomChargeTotal)
                .build();

        BillItem savedItem = billItemRepository.save(initialItem);
        saved.getItems().add(savedItem);

        // Recalculate and persist totals
        recalculateBillTotals(saved);
        Bill finalSaved = billRepository.save(saved);

        log.info("Bill created successfully with id: {}, number: {}, roomCharge: {}", finalSaved.getId(), finalSaved.getBillingNumber(), roomChargeTotal);
        return mapToBillResponse(finalSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return mapToBillResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByReservationId(Long reservationId) {
        Bill bill = billRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for reservationId: " + reservationId));
        return mapToBillResponse(bill);
    }

    @Override
    @Transactional
    public BillItemResponse addItemToBill(Long billId, AddBillItemRequest request) {
        log.info("Adding item to billId: {}, item: {}", billId, request.getDescription());

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        if (bill.getStatus() == BillStatus.FINALIZED) {
            throw new BusinessRuleException("Cannot add items to a finalized and locked bill");
        }

        BigDecimal totalPrice = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        BillItem item = BillItem.builder()
                .bill(bill)
                .description(request.getDescription())
                .itemType(request.getItemType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(totalPrice)
                .build();

        BillItem savedItem = billItemRepository.save(item);
        bill.getItems().add(savedItem);

        // Re-calculate totals
        recalculateBillTotals(bill);
        billRepository.save(bill);

        return mapToBillItemResponse(savedItem);
    }

    @Override
    @Transactional
    public BillResponse calculateBill(Long id) {
        log.info("Calculating bill id: {}", id);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (bill.getStatus() == BillStatus.FINALIZED) {
            throw new BusinessRuleException("Cannot recalculate a finalized and locked bill");
        }

        recalculateBillTotals(bill);
        Bill saved = billRepository.save(bill);
        return mapToBillResponse(saved);
    }

    @Override
    @Transactional
    public BillResponse finalizeBill(Long id, FinalizeBillRequest request) {
        log.info("Finalizing bill id: {}", id);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (bill.getStatus() == BillStatus.FINALIZED) {
            throw new BusinessRuleException("Bill is already finalized and locked");
        }

        if (request != null) {
            if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) >= 0) {
                bill.setDiscountAmount(request.getDiscountAmount());
            }
            if (request.getTaxPercentage() != null && request.getTaxPercentage().compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal calculatedTax = bill.getSubtotal().multiply(request.getTaxPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                bill.setTaxAmount(calculatedTax);
            }
            if (request.getPaidAmount() != null && request.getPaidAmount().compareTo(BigDecimal.ZERO) >= 0) {
                bill.setPaidAmount(request.getPaidAmount());
            }
        }

        recalculateBillTotals(bill);
        bill.setStatus(BillStatus.FINALIZED);
        bill.setFinalizedAt(LocalDateTime.now());

        Bill saved = billRepository.save(bill);
        log.info("Bill finalized with id: {}, total: {}, paid: {}, outstanding: {}",
                saved.getId(), saved.getTotalAmount(), saved.getPaidAmount(), saved.getOutstandingAmount());

        // Publish event
        billingEventPublisher.publishBillFinalized(saved);

        return mapToBillResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrintBillResponse getPrintBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        List<BillItemResponse> itemResponses = bill.getItems().stream()
                .map(this::mapToBillItemResponse)
                .collect(Collectors.toList());

        return PrintBillResponse.builder()
                .billingNumber(bill.getBillingNumber())
                .guestId(bill.getGuestId())
                .reservationId(bill.getReservationId())
                .roomId(bill.getRoomId())
                .hallId(bill.getHallId())
                .checkInDate(bill.getCheckInDate())
                .checkOutDate(bill.getCheckOutDate())
                .items(itemResponses)
                .subtotal(bill.getSubtotal())
                .taxAmount(bill.getTaxAmount())
                .discountAmount(bill.getDiscountAmount())
                .breakageCharges(bill.getBreakageCharges())
                .lateCheckoutCharges(bill.getLateCheckoutCharges())
                .upgradeCharges(bill.getUpgradeCharges())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .outstandingAmount(bill.getOutstandingAmount())
                .status(bill.getStatus().name())
                .invoiceDate(bill.getCreatedAt())
                .finalizedAt(bill.getFinalizedAt())
                .hotelHeader("i-TRANSFORM ONLINE HOTEL & RESORT MANAGEMENT")
                .footerNotes("Thank you for choosing our hotel. For queries, contact billing@hms.com")
                .build();
    }

    @Override
    @Transactional
    public BillResponse recordPaymentForReservation(Long reservationId, BigDecimal amount) {
        log.info("Recording payment of {} for reservationId: {}", amount, reservationId);
        Bill bill = billRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("No bill found for reservation ID: " + reservationId));

        BigDecimal currentPaid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount != null ? amount : BigDecimal.ZERO);
        if (newPaid.compareTo(bill.getTotalAmount()) > 0) {
            newPaid = bill.getTotalAmount();
        }
        bill.setPaidAmount(newPaid);
        recalculateBillTotals(bill);

        if (bill.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(BillStatus.PAID);
        }

        Bill saved = billRepository.save(bill);
        log.info("Bill id {} updated with payment. Total: {}, Paid: {}, Outstanding: {}, Status: {}",
                saved.getId(), saved.getTotalAmount(), saved.getPaidAmount(), saved.getOutstandingAmount(), saved.getStatus());
        return mapToBillResponse(saved);
    }

    private void recalculateBillTotals(Bill bill) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal breakage = BigDecimal.ZERO;
        BigDecimal lateCheckout = BigDecimal.ZERO;
        BigDecimal upgrade = BigDecimal.ZERO;
        BigDecimal downgradeAdjustments = BigDecimal.ZERO;

        if (bill.getItems() != null) {
            for (BillItem item : bill.getItems()) {
                switch (item.getItemType()) {
                    case BREAKAGE:
                        breakage = breakage.add(item.getTotalPrice());
                        break;
                    case LATE_CHECKOUT:
                        lateCheckout = lateCheckout.add(item.getTotalPrice());
                        break;
                    case ROOM_UPGRADE:
                        upgrade = upgrade.add(item.getTotalPrice());
                        break;
                    case ROOM_DOWNGRADE_ADJUSTMENT:
                        downgradeAdjustments = downgradeAdjustments.add(item.getTotalPrice());
                        break;
                    default:
                        subtotal = subtotal.add(item.getTotalPrice());
                        break;
                }
            }
        }

        bill.setSubtotal(subtotal);
        bill.setBreakageCharges(breakage);
        bill.setLateCheckoutCharges(lateCheckout);
        bill.setUpgradeCharges(upgrade);

        // Total = Subtotal + Taxes + Breakage + LateCheckout + Upgrade - Discount - DowngradeAdjustments
        BigDecimal total = subtotal
                .add(bill.getTaxAmount() != null ? bill.getTaxAmount() : BigDecimal.ZERO)
                .add(breakage)
                .add(lateCheckout)
                .add(upgrade)
                .subtract(bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO)
                .subtract(downgradeAdjustments);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        bill.setTotalAmount(total);

        BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        if (paid.compareTo(total) > 0) {
            paid = total;
            bill.setPaidAmount(paid);
        }
        BigDecimal outstanding = total.subtract(paid);
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            outstanding = BigDecimal.ZERO;
            if (bill.getStatus() == BillStatus.FINALIZED) {
                bill.setStatus(BillStatus.PAID);
            }
        }
        bill.setOutstandingAmount(outstanding);
    }

    private BillResponse mapToBillResponse(Bill bill) {
        List<BillItemResponse> itemResponses = bill.getItems() != null
                ? bill.getItems().stream().map(this::mapToBillItemResponse).collect(Collectors.toList())
                : List.of();

        return BillResponse.builder()
                .id(bill.getId())
                .billingNumber(bill.getBillingNumber())
                .reservationId(bill.getReservationId())
                .guestId(bill.getGuestId())
                .roomId(bill.getRoomId())
                .hallId(bill.getHallId())
                .checkInDate(bill.getCheckInDate())
                .checkOutDate(bill.getCheckOutDate())
                .subtotal(bill.getSubtotal())
                .taxAmount(bill.getTaxAmount())
                .discountAmount(bill.getDiscountAmount())
                .breakageCharges(bill.getBreakageCharges())
                .lateCheckoutCharges(bill.getLateCheckoutCharges())
                .upgradeCharges(bill.getUpgradeCharges())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .outstandingAmount(bill.getOutstandingAmount())
                .status(bill.getStatus())
                .items(itemResponses)
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .finalizedAt(bill.getFinalizedAt())
                .build();
    }

    private BillItemResponse mapToBillItemResponse(BillItem item) {
        return BillItemResponse.builder()
                .id(item.getId())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
