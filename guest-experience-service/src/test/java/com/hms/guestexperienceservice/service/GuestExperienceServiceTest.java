package com.hms.guestexperienceservice.service;

import com.hms.guestexperienceservice.dto.request.*;
import com.hms.guestexperienceservice.dto.response.*;
import com.hms.guestexperienceservice.entity.*;
import com.hms.guestexperienceservice.event.publisher.GuestExperienceEventPublisher;
import com.hms.guestexperienceservice.exception.BusinessRuleException;
import com.hms.guestexperienceservice.repository.*;
import com.hms.guestexperienceservice.service.impl.GuestExperienceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestExperienceServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private ServiceRequestHistoryRepository serviceRequestHistoryRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintHistoryRepository complaintHistoryRepository;

    @Mock
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Mock
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Mock
    private GuestExperienceEventPublisher eventPublisher;

    @InjectMocks
    private GuestExperienceServiceImpl guestExperienceService;

    private ServiceRequest sampleRequest;
    private Complaint sampleComplaint;
    private LoyaltyAccount sampleLoyaltyAccount;

    @BeforeEach
    void setUp() {
        sampleRequest = ServiceRequest.builder()
                .id(1L)
                .requestCode("REQ-12345678")
                .reservationId(100L)
                .guestId(50L)
                .roomId(201L)
                .requestType(RequestType.EXTRA_TOWELS)
                .description("2 extra bath towels")
                .quantity(2)
                .priority("HIGH")
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleComplaint = Complaint.builder()
                .id(1L)
                .complaintCode("CMP-12345678")
                .guestId(50L)
                .reservationId(100L)
                .roomId(201L)
                .category("Noise")
                .description("Loud AC noise")
                .status(ComplaintStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleLoyaltyAccount = LoyaltyAccount.builder()
                .id(1L)
                .guestId(50L)
                .tier(LoyaltyTier.SILVER)
                .pointsBalance(150)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create service request and publish event")
    void testCreateServiceRequest_Success() {
        CreateServiceRequest request = CreateServiceRequest.builder()
                .reservationId(100L)
                .guestId(50L)
                .roomId(201L)
                .requestType(RequestType.EXTRA_TOWELS)
                .description("2 extra bath towels")
                .quantity(2)
                .priority("HIGH")
                .build();

        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenReturn(sampleRequest);

        ServiceRequestResponse response = guestExperienceService.createServiceRequest(request);

        assertNotNull(response);
        assertEquals(RequestType.EXTRA_TOWELS, response.getRequestType());
        verify(serviceRequestRepository, times(1)).save(any(ServiceRequest.class));
        verify(serviceRequestHistoryRepository, times(1)).save(any(ServiceRequestHistory.class));
        verify(eventPublisher, times(1)).publishServiceRequestCreated(any(ServiceRequest.class));
    }

    @Test
    @DisplayName("Should submit feedback successfully")
    void testSubmitFeedback_Success() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder()
                .guestId(50L)
                .reservationId(100L)
                .roomId(201L)
                .rating(5)
                .comments("Exceptional hospitality and room service!")
                .build();

        Feedback sampleFeedback = Feedback.builder()
                .id(10L)
                .guestId(50L)
                .reservationId(100L)
                .roomId(201L)
                .rating(5)
                .comments("Exceptional hospitality and room service!")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(sampleFeedback);

        FeedbackResponse response = guestExperienceService.submitFeedback(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should create complaint and update status to RESOLVED")
    void testComplaintLifecycle_Success() {
        CreateComplaintRequest request = CreateComplaintRequest.builder()
                .guestId(50L)
                .reservationId(100L)
                .roomId(201L)
                .category("Noise")
                .description("Loud AC noise")
                .build();

        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        ComplaintResponse response = guestExperienceService.createComplaint(request);
        assertNotNull(response);
        assertEquals(ComplaintStatus.OPEN, response.getStatus());

        // Update to resolved
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(sampleComplaint));
        UpdateComplaintStatusRequest updateReq = UpdateComplaintStatusRequest.builder()
                .status(ComplaintStatus.RESOLVED)
                .resolutionRemarks("AC filter cleaned by technician")
                .changedBy("MANAGER")
                .build();

        ComplaintResponse resolvedResponse = guestExperienceService.updateComplaintStatus(1L, updateReq);
        assertEquals(ComplaintStatus.RESOLVED, resolvedResponse.getStatus());
        verify(eventPublisher, times(1)).publishComplaintResolved(any(Complaint.class));
    }

    @Test
    @DisplayName("Should earn and redeem loyalty points correctly")
    void testLoyaltyEarnAndRedeem_Success() {
        EarnLoyaltyRequest earnReq = EarnLoyaltyRequest.builder()
                .guestId(50L)
                .amount(new BigDecimal("500.00")) // 50 points
                .description("Restaurant dining spend")
                .build();

        when(loyaltyAccountRepository.findByGuestId(50L)).thenReturn(Optional.of(sampleLoyaltyAccount));
        when(loyaltyAccountRepository.save(any(LoyaltyAccount.class))).thenReturn(sampleLoyaltyAccount);

        LoyaltyAccountResponse earnResponse = guestExperienceService.earnPoints(earnReq);
        assertNotNull(earnResponse);
        verify(loyaltyTransactionRepository, times(1)).save(any(LoyaltyTransaction.class));

        // Redeem points
        RedeemLoyaltyRequest redeemReq = RedeemLoyaltyRequest.builder()
                .guestId(50L)
                .points(50)
                .description("Spa discount")
                .build();

        LoyaltyAccountResponse redeemResponse = guestExperienceService.redeemPoints(redeemReq);
        assertNotNull(redeemResponse);
        verify(loyaltyTransactionRepository, times(2)).save(any(LoyaltyTransaction.class));
    }

    @Test
    @DisplayName("Should fail redemption when points balance is insufficient")
    void testRedeemPoints_Insufficient() {
        RedeemLoyaltyRequest redeemReq = RedeemLoyaltyRequest.builder()
                .guestId(50L)
                .points(5000)
                .description("Excessive redemption")
                .build();

        when(loyaltyAccountRepository.findByGuestId(50L)).thenReturn(Optional.of(sampleLoyaltyAccount));

        assertThrows(BusinessRuleException.class, () -> guestExperienceService.redeemPoints(redeemReq));
    }
}
