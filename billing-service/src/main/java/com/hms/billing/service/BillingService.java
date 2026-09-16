package com.hms.billing.service;
import com.hms.billing.dto.*; import com.hms.billing.entity.*; import com.hms.billing.repository.InvoiceRepository; import jakarta.persistence.EntityNotFoundException; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.*; import java.util.*; import java.util.concurrent.ThreadLocalRandom;
@Service @RequiredArgsConstructor
public class BillingService {
 private final InvoiceRepository repo;
 @Transactional public InvoiceResponse create(InvoiceRequest r){
  if(r.getItems()==null||r.getItems().isEmpty())throw new IllegalArgumentException("At least one invoice line item is required");
  BigDecimal sub=r.getItems().stream().map(i->i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal rate=r.getTaxRate()==null?BigDecimal.ZERO:r.getTaxRate();
  BigDecimal tax=sub.multiply(rate).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
  BigDecimal discount=r.getDiscountAmount()==null?BigDecimal.ZERO:r.getDiscountAmount(); BigDecimal total=sub.add(tax).subtract(discount);
  if(total.signum()<0)throw new IllegalArgumentException("Invoice total cannot be negative");
  Invoice inv=Invoice.builder().invoiceNumber("INV-"+ThreadLocalRandom.current().nextInt(100000,999999)).reservationId(r.getReservationId()).guestId(r.getGuestId()).status(InvoiceStatus.ISSUED).subtotal(sub).taxAmount(tax).discountAmount(discount).totalAmount(total).build();
  r.getItems().forEach(i->inv.getItems().add(BillItem.builder().invoice(inv).description(i.getDescription()).quantity(i.getQuantity()).unitPrice(i.getUnitPrice()).lineTotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).build()));
  return map(repo.save(inv));
 }
 public InvoiceResponse get(UUID id){return map(repo.findById(id).orElseThrow(()->new EntityNotFoundException("Invoice not found: "+id)));}
 @Transactional public InvoiceResponse addCharge(UUID id,AdditionalChargeRequest r){
  Invoice inv=repo.findById(id).orElseThrow(()->new EntityNotFoundException("Invoice not found: "+id));
  inv.getItems().add(BillItem.builder().invoice(inv).description(r.getDescription()).quantity(1).unitPrice(r.getAmount()).lineTotal(r.getAmount()).build());
  inv.setSubtotal(inv.getSubtotal().add(r.getAmount())); inv.setTotalAmount(inv.getTotalAmount().add(r.getAmount())); return map(repo.save(inv));
 }
 private InvoiceResponse map(Invoice i){return InvoiceResponse.builder().id(i.getId()).invoiceNumber(i.getInvoiceNumber()).reservationId(i.getReservationId()).guestId(i.getGuestId()).status(i.getStatus()).subtotal(i.getSubtotal()).taxAmount(i.getTaxAmount()).discountAmount(i.getDiscountAmount()).totalAmount(i.getTotalAmount()).items(i.getItems().stream().map(x->InvoiceItemResponse.builder().id(x.getId()).description(x.getDescription()).quantity(x.getQuantity()).unitPrice(x.getUnitPrice()).lineTotal(x.getLineTotal()).build()).toList()).createdAt(i.getCreatedAt()).build();}
}