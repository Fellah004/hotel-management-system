package com.hms.billing.controller;
import com.hms.billing.entity.Invoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/billing")
public class BillingController {
 @PostMapping("/invoices") public ResponseEntity<Invoice> generateBill(@RequestBody Invoice invoice){return ResponseEntity.ok(invoice);}
 @GetMapping("/invoices/{invoiceId}") public ResponseEntity<Invoice> getBill(@PathVariable UUID invoiceId){Invoice i=new Invoice();i.setId(invoiceId);return ResponseEntity.ok(i);}
 @GetMapping("/invoices/{invoiceId}/print") public ResponseEntity<Invoice> printInvoice(@PathVariable UUID invoiceId){Invoice i=new Invoice();i.setId(invoiceId);return ResponseEntity.ok(i);}
 @PostMapping("/breakage-charges") public ResponseEntity<Invoice> addBreakageCharge(@RequestBody Invoice invoice){return ResponseEntity.ok(invoice);}
}
