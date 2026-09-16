package com.hms.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID reservationId;
    private UUID guestId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private PaymentStatus status;
    private String transactionReference;
    private OffsetDateTime paidAt;
    public UUID getId(){return id;} public void setId(UUID id){this.id=id;}
    public UUID getReservationId(){return reservationId;} public void setReservationId(UUID v){reservationId=v;}
    public UUID getGuestId(){return guestId;} public void setGuestId(UUID v){guestId=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod v){paymentMethod=v;}
    public PaymentStatus getStatus(){return status;} public void setStatus(PaymentStatus v){status=v;}
    public String getTransactionReference(){return transactionReference;} public void setTransactionReference(String v){transactionReference=v;}
    public OffsetDateTime getPaidAt(){return paidAt;} public void setPaidAt(OffsetDateTime v){paidAt=v;}
}
