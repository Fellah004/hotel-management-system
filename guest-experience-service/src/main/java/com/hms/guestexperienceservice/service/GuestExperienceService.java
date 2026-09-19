package com.hms.guestexperienceservice.service;

import com.hms.guestexperienceservice.dto.request.*;
import com.hms.guestexperienceservice.dto.response.*;

import java.util.List;

public interface GuestExperienceService {
    // Service Requests
    ServiceRequestResponse createServiceRequest(CreateServiceRequest request);
    ServiceRequestResponse getServiceRequestById(Long id);
    List<ServiceRequestResponse> getServiceRequestsByReservationId(Long reservationId);
    List<ServiceRequestResponse> getServiceRequestsByGuestId(Long guestId);
    ServiceRequestResponse cancelServiceRequest(Long id);
    List<ServiceRequestHistoryResponse> getServiceRequestHistory(Long serviceRequestId);

    // Feedback
    FeedbackResponse submitFeedback(CreateFeedbackRequest request);
    List<FeedbackResponse> getFeedbackByGuestId(Long guestId);
    List<FeedbackResponse> getFeedbackByReservationId(Long reservationId);

    // Complaints
    ComplaintResponse createComplaint(CreateComplaintRequest request);
    List<ComplaintResponse> getComplaintsByGuestId(Long guestId);
    List<ComplaintResponse> getAllComplaints();
    ComplaintResponse getComplaintById(Long id);
    ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request);
    ComplaintResponse assignComplaint(Long id, AssignComplaintRequest request);
    List<ComplaintHistoryResponse> getComplaintHistory(Long complaintId);

    // Loyalty
    LoyaltyAccountResponse getLoyaltyAccountByGuestId(Long guestId);
    LoyaltyAccountResponse earnPoints(EarnLoyaltyRequest request);
    LoyaltyAccountResponse redeemPoints(RedeemLoyaltyRequest request);
    List<LoyaltyTransactionResponse> getLoyaltyTransactions(Long accountId);
}
