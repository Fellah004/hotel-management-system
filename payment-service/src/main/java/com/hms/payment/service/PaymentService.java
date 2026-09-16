package com.hms.payment.service;
import com.hms.payment.dto.*; import com.hms.payment.entity.*; import com.hms.payment.repository.*;
import jakarta.persistence.EntityNotFoundException; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.*; import java.util.*; import java.util.concurrent.ThreadLocalRandom;
@Service @RequiredArgsConstructor
public class PaymentService {
 private final PaymentRepository payments; private final RefundRepository refunds;
 @Transactional public PaymentResponse process(PaymentRequest r) {
  if(r.getIdempotencyKey()!=null&&!r.getIdempotencyKey().isBlank()){var e=payments.findByIdempotencyKey(r.getIdempotencyKey());if(e.isPresent())return map(e.get());}
  Payment p=Payment.builder().paymentNumber("PAY-"+ThreadLocalRandom.current().nextInt(100000,999999)).reservationId(r.getReservationId()).guestId(r.getGuestId()).amount(r.getAmount()).method(r.getMethod()).providerReference(r.getProviderReference()).idempotencyKey(r.getIdempotencyKey()).status(PaymentStatus.SUCCESS).build();
  return map(payments.save(p));
 }
 public PaymentResponse get(UUID id){return map(payments.findById(id).orElseThrow(()->new EntityNotFoundException("Payment not found: "+id)));}
 public List<PaymentResponse> reservation(UUID id){return payments.findByReservationIdOrderByCreatedAtDesc(id).stream().map(this::map).toList();}
 public List<PaymentResponse> guest(UUID id){return payments.findByGuestIdOrderByCreatedAtDesc(id).stream().map(this::map).toList();}
 @Transactional public PaymentResponse refund(UUID id,RefundRequest r){
  Payment p=payments.findById(id).orElseThrow(()->new EntityNotFoundException("Payment not found: "+id));
  BigDecimal old=refunds.findByPaymentIdOrderByCreatedAtDesc(id).stream().map(Refund::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
  if(old.add(r.getAmount()).compareTo(p.getAmount())>0)throw new IllegalArgumentException("Refund amount exceeds refundable payment amount");
  refunds.save(Refund.builder().paymentId(id).amount(r.getAmount()).reason(r.getReason()).providerReference(p.getProviderReference()).build());
  p.setStatus(old.add(r.getAmount()).compareTo(p.getAmount())==0?PaymentStatus.REFUNDED:PaymentStatus.PARTIALLY_REFUNDED);
  return map(payments.save(p));
 }
 private PaymentResponse map(Payment p){return PaymentResponse.builder().id(p.getId()).paymentNumber(p.getPaymentNumber()).reservationId(p.getReservationId()).guestId(p.getGuestId()).amount(p.getAmount()).method(p.getMethod()).status(p.getStatus()).providerReference(p.getProviderReference()).createdAt(p.getCreatedAt()).build();}
}