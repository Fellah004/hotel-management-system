package com.hms.billing.repository;
import com.hms.billing.entity.Invoice; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice,UUID> { List<Invoice> findByReservationIdOrderByCreatedAtDesc(UUID id); List<Invoice> findByGuestIdOrderByCreatedAtDesc(UUID id); }