package com.hms.purchaseservice.entity;

public enum OrderStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SENT_TO_SUPPLIER,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CLOSED,
    CANCELLED
}
