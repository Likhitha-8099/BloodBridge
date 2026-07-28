package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener class that maps Spring ApplicationEvents to notification dispatches.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Handles {@link BloodRequestCreatedEvent} by notifying the assigned hospital.
     *
     * @param event the creation event
     */
    @EventListener
    public void handleBloodRequestCreated(BloodRequestCreatedEvent event) {
        BloodRequest request = event.getBloodRequest();
        User hospitalUser = request.getHospital().getUser();
        String title = "New Blood Request Assigned";
        String message = String.format(
                "A new blood request for %s has been assigned to your hospital needing %d units.",
                request.getBloodGroupNeeded(), request.getUnitsRequired()
        );
        notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.BLOOD_REQUEST_CREATED);
    }

    /**
     * Handles {@link RequestVerifiedEvent} by notifying the requesting patient.
     *
     * @param event the verification event
     */
    @EventListener
    public void handleRequestVerified(RequestVerifiedEvent event) {
        BloodRequest request = event.getBloodRequest();
        User patientUser = request.getPatient().getUser();
        String title = "Blood Request Verified";
        String message = String.format(
                "Your blood request for %s was successfully verified by %s.",
                request.getBloodGroupNeeded(), request.getHospital().getHospitalName()
        );
        notificationService.triggerNotificationEvent(patientUser, title, message, NotificationType.REQUEST_VERIFIED);
    }

    /**
     * Handles {@link RequestRejectedEvent} by notifying the patient.
     *
     * @param event the rejection event
     */
    @EventListener
    public void handleRequestRejected(RequestRejectedEvent event) {
        BloodRequest request = event.getBloodRequest();
        User patientUser = request.getPatient().getUser();
        String title = "Blood Request Rejected";
        String message = String.format(
                "Your blood request for %s was rejected by %s.",
                request.getBloodGroupNeeded(), request.getHospital().getHospitalName()
        );
        notificationService.triggerNotificationEvent(patientUser, title, message, NotificationType.REQUEST_REJECTED);
    }

    /**
     * Handles {@link DonorMatchedEvent} by notifying the eligible matched donor.
     *
     * @param event the match event
     */
    @EventListener
    public void handleDonorMatched(DonorMatchedEvent event) {
        MatchResult match = event.getMatchResult();
        User donorUser = match.getDonor().getUser();
        String title = "Eligible Blood Request Match";
        String message = String.format(
                "You have been matched as an eligible donor for a request at %s needing %s.",
                match.getBloodRequest().getHospital().getHospitalName(),
                match.getBloodRequest().getBloodGroupNeeded()
        );
        notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONOR_MATCHED);
    }

    /**
     * Handles {@link DonationAcceptedEvent} by notifying the assigned hospital.
     *
     * @param event the acceptance event
     */
    @EventListener
    public void handleDonationAccepted(DonationAcceptedEvent event) {
        Donation donation = event.getDonation();
        User hospitalUser = donation.getHospital().getUser();
        String title = "Donation Match Accepted";
        String message = String.format(
                "Donor %s has accepted the matching request for patient %s. Please confirm the donation details.",
                donation.getDonor().getUser().getFullName(),
                donation.getPatient().getUser().getFullName()
        );
        notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.DONATION_ACCEPTED);
    }

    /**
     * Handles {@link DonationConfirmedEvent} by notifying both the donor and the patient.
     *
     * @param event the confirmation event
     */
    @EventListener
    public void handleDonationConfirmed(DonationConfirmedEvent event) {
        Donation donation = event.getDonation();
        User donorUser = donation.getDonor().getUser();
        User patientUser = donation.getPatient().getUser();

        String title = "Donation Details Confirmed";
        String message = String.format(
                "The donation schedule at %s has been confirmed by the hospital.",
                donation.getHospital().getHospitalName()
        );

        notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONATION_CONFIRMED);
        notificationService.triggerNotificationEvent(patientUser, title, message, NotificationType.DONATION_CONFIRMED);
    }

    /**
     * Handles {@link DonationCompletedEvent} by notifying the donor, patient, and hospital.
     *
     * @param event the completion event
     */
    @EventListener
    public void handleDonationCompleted(DonationCompletedEvent event) {
        Donation donation = event.getDonation();
        User donorUser = donation.getDonor().getUser();
        User patientUser = donation.getPatient().getUser();
        User hospitalUser = donation.getHospital().getUser();

        String title = "Donation Transaction Completed";
        String message = String.format(
                "A donation of %d units of %s has been completed successfully at %s.",
                donation.getUnitsDonated(),
                donation.getDonor().getBloodGroup(),
                donation.getHospital().getHospitalName()
        );

        notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONATION_COMPLETED);
        notificationService.triggerNotificationEvent(patientUser, title, message, NotificationType.DONATION_COMPLETED);
        notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.DONATION_COMPLETED);
    }
}
