package com.hms.billing.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="invoices")
public class Invoice {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 private UUID reservationId; private UUID guestId; private BigDecimal subtotal; private BigDecimal taxRate; private BigDecimal discountAmount; private BigDecimal totalAmount;
 @Enumerated(EnumType.STRING) private InvoiceStatus status; private OffsetDateTime issuedAt;
 public UUID getId(){return id;} public void setId(UUID v){id=v;} public UUID getReservationId(){return reservationId;} public void setReservationId(UUID v){reservationId=v;} public UUID getGuestId(){return guestId;} public void setGuestId(UUID v){guestId=v;} public BigDecimal getSubtotal(){return subtotal;} public void setSubtotal(BigDecimal v){subtotal=v;} public BigDecimal getTaxRate(){return taxRate;} public void setTaxRate(BigDecimal v){taxRate=v;} public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;} public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;} public InvoiceStatus getStatus(){return status;} public void setStatus(InvoiceStatus v){status=v;} public OffsetDateTime getIssuedAt(){return issuedAt;} public void setIssuedAt(OffsetDateTime v){issuedAt=v;}
}
