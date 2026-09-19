package com.hms.guestexperienceservice.service.impl;

import com.hms.guestexperienceservice.dto.request.*;
import com.hms.guestexperienceservice.dto.response.*;
import com.hms.guestexperienceservice.entity.*;
import com.hms.guestexperienceservice.event.publisher.GuestExperienceEventPublisher;
import com.hms.guestexperienceservice.exception.BusinessRuleException;
import com.hms.guestexperienceservice.exception.ResourceNotFoundException;
import com.hms.guestexperienceservice.repository.*;
import com.hms.guestexperienceservice.service.GuestExperienceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestExperienceServiceImpl implements GuestExperienceService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestHistoryRepository serviceRequestHistoryRepository;
    private final FeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final GuestExperienceEventPublisher eventPublisher;

    // --- Service Requests ---

    @Override
    @Transactional
    public ServiceRequestResponse createServiceRequest(CreateServiceRequest request) {
        log.info("Creating service request type: {} for reservationId: {}, roomId: {}",
                request.getRequestType(), request.getReservationId(), request.getRoomId());

        String code = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .requestCode(code)
                .reservationId(request.getReservationId())
                .guestId(request.getGuestId())
                .roomId(request.getRoomId())
                .requestType(request.getRequestType())
                .description(request.getDescription())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .status(RequestStatus.PENDING)
                .build();

        ServiceRequest saved = serviceRequestRepository.save(serviceRequest);

        // Record history
        recordServiceRequestHistory(saved.getId(), null, RequestStatus.PENDING, "GUEST", "Request created");

        // Publish event for Operations to handle
        eventPublisher.publishServiceRequestCreated(saved);

        return mapToServiceRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestById(Long id) {
        ServiceRequest req = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
        return mapToServiceRequestResponse(req);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByReservationId(Long reservationId) {
        return serviceRequestRepository.findByReservationId(reservationId).stream()
                .map(this::mapToServiceRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getServiceRequestsByGuestId(Long guestId) {
        return serviceRequestRepository.findByGuestId(guestId).stream()
                .map(this::mapToServiceRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServiceRequestResponse cancelServiceRequest(Long id) {
        ServiceRequest req = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));

        if (req.getStatus() == RequestStatus.COMPLETED || req.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot cancel request in status: " + req.getStatus());
        }

        RequestStatus oldStatus = req.getStatus();
        req.setStatus(RequestStatus.CANCELLED);
        ServiceRequest saved = serviceRequestRepository.save(req);

        recordServiceRequestHistory(saved.getId(), oldStatus, RequestStatus.CANCELLED, "GUEST", "Cancelled by guest");
        return mapToServiceRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRequestHistoryResponse> getServiceRequestHistory(Long serviceRequestId) {
        return serviceRequestHistoryRepository.findByServiceRequestIdOrderByCreatedAtDesc(serviceRequestId).stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    // --- Feedback ---

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(CreateFeedbackRequest request) {
        log.info("Submitting feedback for guestId: {}, rating: {}", request.getGuestId(), request.getRating());

        Feedback feedback = Feedback.builder()
                .guestId(request.getGuestId())
                .reservationId(request.getReservationId())
                .roomId(request.getRoomId())
                .rating(request.getRating())
                .comments(request.getComments())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return mapToFeedbackResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByGuestId(Long guestId) {
        return feedbackRepository.findByGuestId(guestId).stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByReservationId(Long reservationId) {
        return feedbackRepository.findByReservationId(reservationId).stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    // --- Complaints ---

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request) {
        log.info("Creating complaint for guestId: {}, category: {}", request.getGuestId(), request.getCategory());

        String code = "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Complaint complaint = Complaint.builder()
                .complaintCode(code)
                .guestId(request.getGuestId())
                .reservationId(request.getReservationId())
                .roomId(request.getRoomId())
                .category(request.getCategory())
                .description(request.getDescription())
                .status(ComplaintStatus.OPEN)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        recordComplaintHistory(saved.getId(), null, ComplaintStatus.OPEN, "GUEST", "Complaint filed");
        eventPublisher.publishComplaintCreated(saved);

        return mapToComplaintResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByGuestId(Long guestId) {
        return complaintRepository.findByGuestId(guestId).stream()
                .map(this::mapToComplaintResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToComplaintResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return mapToComplaintResponse(c);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        ComplaintStatus oldStatus = c.getStatus();
        c.setStatus(request.getStatus());
        if (request.getResolutionRemarks() != null) {
            c.setResolutionRemarks(request.getResolutionRemarks());
        }
        if (request.getStatus() == ComplaintStatus.RESOLVED) {
            c.setResolvedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(c);

        recordComplaintHistory(saved.getId(), oldStatus, saved.getStatus(),
                request.getChangedBy() != null ? request.getChangedBy() : "STAFF",
                request.getResolutionRemarks());

        if (saved.getStatus() == ComplaintStatus.RESOLVED) {
            eventPublisher.publishComplaintResolved(saved);
        }

        return mapToComplaintResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse assignComplaint(Long id, AssignComplaintRequest request) {
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        c.setAssignedStaffId(request.getStaffId());
        c.setStatus(ComplaintStatus.IN_PROGRESS);

        Complaint saved = complaintRepository.save(c);
        recordComplaintHistory(saved.getId(), ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS,
                request.getChangedBy() != null ? request.getChangedBy() : "STAFF",
                "Assigned to staff id: " + request.getStaffId());

        return mapToComplaintResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintHistoryResponse> getComplaintHistory(Long complaintId) {
        return complaintHistoryRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId).stream()
                .map(this::mapToComplaintHistoryResponse)
                .collect(Collectors.toList());
    }

    // --- Loyalty ---

    @Override
    @Transactional
    public LoyaltyAccountResponse getLoyaltyAccountByGuestId(Long guestId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByGuestId(guestId)
                .orElseGet(() -> loyaltyAccountRepository.save(LoyaltyAccount.builder()
                        .guestId(guestId)
                        .tier(LoyaltyTier.SILVER)
                        .pointsBalance(0)
                        .build()));
        return mapToLoyaltyAccountResponse(account);
    }

    @Override
    @Transactional
    public LoyaltyAccountResponse earnPoints(EarnLoyaltyRequest request) {
        LoyaltyAccount account = loyaltyAccountRepository.findByGuestId(request.getGuestId())
                .orElseGet(() -> loyaltyAccountRepository.save(LoyaltyAccount.builder()
                        .guestId(request.getGuestId())
                        .tier(LoyaltyTier.SILVER)
                        .pointsBalance(0)
                        .build()));

        // 1 point per 10 currency units
        int earnedPoints = request.getAmount().divideToIntegralValue(BigDecimal.valueOf(10)).intValue();
        if (earnedPoints <= 0) {
            earnedPoints = 1;
        }

        account.setPointsBalance(account.getPointsBalance() + earnedPoints);
        updateLoyaltyTier(account);
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        // Record transaction
        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccountId(saved.getId())
                .points(earnedPoints)
                .transactionType(LoyaltyTransactionType.EARN)
                .description(request.getDescription() != null ? request.getDescription() : "Points earned from spending")
                .build();
        loyaltyTransactionRepository.save(tx);

        log.info("Guest {} earned {} points. New balance: {}, tier: {}",
                request.getGuestId(), earnedPoints, saved.getPointsBalance(), saved.getTier());

        return mapToLoyaltyAccountResponse(saved);
    }

    @Override
    @Transactional
    public LoyaltyAccountResponse redeemPoints(RedeemLoyaltyRequest request) {
        LoyaltyAccount account = loyaltyAccountRepository.findByGuestId(request.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Loyalty account not found for guest: " + request.getGuestId()));

        if (account.getPointsBalance() < request.getPoints()) {
            throw new BusinessRuleException("Insufficient loyalty points balance: " + account.getPointsBalance() +
                    ", requested: " + request.getPoints());
        }

        account.setPointsBalance(account.getPointsBalance() - request.getPoints());
        updateLoyaltyTier(account);
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);

        // Record transaction
        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccountId(saved.getId())
                .points(request.getPoints())
                .transactionType(LoyaltyTransactionType.REDEEM)
                .description(request.getDescription() != null ? request.getDescription() : "Points redeemed")
                .build();
        loyaltyTransactionRepository.save(tx);

        log.info("Guest {} redeemed {} points. Remaining balance: {}",
                request.getGuestId(), request.getPoints(), saved.getPointsBalance());

        return mapToLoyaltyAccountResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getLoyaltyTransactions(Long accountId) {
        return loyaltyTransactionRepository.findByLoyaltyAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::mapToLoyaltyTransactionResponse)
                .collect(Collectors.toList());
    }

    // --- Helpers & Mappers ---

    private void updateLoyaltyTier(LoyaltyAccount account) {
        int balance = account.getPointsBalance();
        if (balance >= 5000) {
            account.setTier(LoyaltyTier.PLATINUM);
        } else if (balance >= 1000) {
            account.setTier(LoyaltyTier.GOLD);
        } else {
            account.setTier(LoyaltyTier.SILVER);
        }
    }

    private void recordServiceRequestHistory(Long reqId, RequestStatus oldStatus, RequestStatus newStatus, String changedBy, String remarks) {
        ServiceRequestHistory history = ServiceRequestHistory.builder()
                .serviceRequestId(reqId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .build();
        serviceRequestHistoryRepository.save(history);
    }

    private void recordComplaintHistory(Long cmpId, ComplaintStatus oldStatus, ComplaintStatus newStatus, String changedBy, String remarks) {
        ComplaintHistory history = ComplaintHistory.builder()
                .complaintId(cmpId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .build();
        complaintHistoryRepository.save(history);
    }

    private ServiceRequestResponse mapToServiceRequestResponse(ServiceRequest req) {
        return ServiceRequestResponse.builder()
                .id(req.getId())
                .requestCode(req.getRequestCode())
                .reservationId(req.getReservationId())
                .guestId(req.getGuestId())
                .roomId(req.getRoomId())
                .requestType(req.getRequestType())
                .description(req.getDescription())
                .quantity(req.getQuantity())
                .priority(req.getPriority())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .completedAt(req.getCompletedAt())
                .build();
    }

    private ServiceRequestHistoryResponse mapToHistoryResponse(ServiceRequestHistory h) {
        return ServiceRequestHistoryResponse.builder()
                .id(h.getId())
                .serviceRequestId(h.getServiceRequestId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .changedBy(h.getChangedBy())
                .remarks(h.getRemarks())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private FeedbackResponse mapToFeedbackResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .guestId(f.getGuestId())
                .reservationId(f.getReservationId())
                .roomId(f.getRoomId())
                .rating(f.getRating())
                .comments(f.getComments())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private ComplaintResponse mapToComplaintResponse(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .complaintCode(c.getComplaintCode())
                .guestId(c.getGuestId())
                .reservationId(c.getReservationId())
                .roomId(c.getRoomId())
                .category(c.getCategory())
                .description(c.getDescription())
                .status(c.getStatus())
                .assignedStaffId(c.getAssignedStaffId())
                .resolutionRemarks(c.getResolutionRemarks())
                .resolvedAt(c.getResolvedAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ComplaintHistoryResponse mapToComplaintHistoryResponse(ComplaintHistory h) {
        return ComplaintHistoryResponse.builder()
                .id(h.getId())
                .complaintId(h.getComplaintId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .changedBy(h.getChangedBy())
                .remarks(h.getRemarks())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private LoyaltyAccountResponse mapToLoyaltyAccountResponse(LoyaltyAccount a) {
        return LoyaltyAccountResponse.builder()
                .id(a.getId())
                .guestId(a.getGuestId())
                .tier(a.getTier())
                .pointsBalance(a.getPointsBalance())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private LoyaltyTransactionResponse mapToLoyaltyTransactionResponse(LoyaltyTransaction t) {
        return LoyaltyTransactionResponse.builder()
                .id(t.getId())
                .loyaltyAccountId(t.getLoyaltyAccountId())
                .points(t.getPoints())
                .transactionType(t.getTransactionType())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
