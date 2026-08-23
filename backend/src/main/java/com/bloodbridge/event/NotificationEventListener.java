package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.MatchResult;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.service.CertificateService;
import com.bloodbridge.service.EmailService;
import com.bloodbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CertificateService certificateService;

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
        String title = "Emergency Blood Request Verified";
        String message = String.format(
                "Your emergency blood request for %s (%d units) has been verified and matching has commenced.",
                request.getBloodGroupNeeded(), request.getUnitsRequired()
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
     * Handles {@link DonorMatchedEvent} by notifying the matched donor.
     *
     * @param event the match event
     */
    @EventListener
    public void handleDonorMatched(DonorMatchedEvent event) {
        MatchResult match = event.getMatchResult();
        User donorUser = match.getDonor().getUser();
        String title = "Compatible Blood Match Found";
        String message = String.format(
                "You have been matched for a blood request at %s requiring %s blood.",
                match.getBloodRequest().getHospital().getHospitalName(),
                match.getBloodRequest().getBloodGroupNeeded()
        );
        notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONOR_MATCHED);

        if (donorUser != null && donorUser.getEmail() != null && !donorUser.getEmail().isBlank() && emailService != null) {
            try {
                String hospName = (match.getBloodRequest() != null && match.getBloodRequest().getHospital() != null)
                        ? match.getBloodRequest().getHospital().getHospitalName()
                        : "Partner Hospital";
                String bgNeeded = (match.getBloodRequest() != null && match.getBloodRequest().getBloodGroupNeeded() != null)
                        ? match.getBloodRequest().getBloodGroupNeeded().name()
                        : "ANY";
                emailService.sendMatchNotificationEmail(donorUser.getEmail(), bgNeeded, hospName);
            } catch (Exception e) {
                log.error("[DONOR-MATCHED-EMAIL-ERROR] Failed to send match email to donor {}: {}", donorUser.getEmail(), e.getMessage());
            }
        }
    }

    /**
     * Handles {@link DonationAcceptedEvent} by notifying the hospital.
     *
     * @param event the acceptance event
     */
    @EventListener
    public void handleDonationAccepted(DonationAcceptedEvent event) {
        if (event == null || event.getDonation() == null) return;
        Donation donation = event.getDonation();
        if (donation.getHospital() == null) return;
        User hospitalUser = donation.getHospital().getUser();
        String donorName = (donation.getDonor() != null && donation.getDonor().getUser() != null)
                ? donation.getDonor().getUser().getFullName()
                : "Valued Donor";
        String title = "Donor Accepted Match Request";
        String message = String.format(
                "Donor %s has accepted the match for donation record #%d.",
                donorName, donation.getId()
        );
        if (hospitalUser != null) {
            notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.DONATION_ACCEPTED);
        }

        String hospitalEmail = (hospitalUser != null && hospitalUser.getEmail() != null && !hospitalUser.getEmail().isBlank())
                ? hospitalUser.getEmail()
                : donation.getHospital().getEmail();

        if (hospitalEmail != null && !hospitalEmail.isBlank() && emailService != null) {
            try {
                String bgStr = (donation.getBloodRequest() != null && donation.getBloodRequest().getBloodGroupNeeded() != null)
                        ? donation.getBloodRequest().getBloodGroupNeeded().name()
                        : "ANY";
                Long reqId = donation.getBloodRequest() != null ? donation.getBloodRequest().getId() : donation.getId();
                Integer units = donation.getUnitsDonated() != null ? donation.getUnitsDonated() : 1;
                String acceptedAtStr = java.time.LocalDateTime.now().toString();

                emailService.sendDonorAcceptanceEmailToHospital(
                        hospitalEmail,
                        donation.getHospital().getHospitalName(),
                        donorName,
                        bgStr,
                        reqId,
                        units,
                        0.0,
                        acceptedAtStr
                );
            } catch (Exception e) {
                log.error("[HOSPITAL-ACCEPTANCE-EMAIL-ERROR] Failed to send acceptance email to hospital {}: {}", hospitalEmail, e.getMessage());
            }
        }
    }

    /**
     * Handles {@link DonorAcceptedEvent} by notifying the hospital via in-app notification & email.
     *
     * @param event the donor acceptance event
     */
    @EventListener
    public void handleDonorAccepted(DonorAcceptedEvent event) {
        if (event == null || event.getResponse() == null) return;
        com.bloodbridge.entity.EmergencyResponse response = event.getResponse();
        BloodRequest req = response.getBloodRequest();
        com.bloodbridge.entity.DonorProfile donor = response.getDonor();
        if (req == null || donor == null) return;
        com.bloodbridge.entity.Hospital hospital = req.getHospital();
        if (hospital == null) return;

        User donorUser = donor.getUser();
        String donorName = (donorUser != null && donorUser.getFullName() != null) ? donorUser.getFullName() : "A Donor";

        // In-app notification to hospital user
        User hospitalUser = hospital.getUser();
        if (hospitalUser != null) {
            String title = "Donor Accepted Emergency Request";
            String message = String.format("Donor %s accepted emergency blood request #%d for %s.",
                    donorName, req.getId(), req.getBloodGroupNeeded());
            notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.DONATION_ACCEPTED);
        }

        // Email to hospital administrator
        String hospitalEmail = (hospitalUser != null && hospitalUser.getEmail() != null && !hospitalUser.getEmail().isBlank())
                ? hospitalUser.getEmail()
                : hospital.getEmail();

        if (hospitalEmail != null && !hospitalEmail.isBlank() && emailService != null) {
            String bgStr = req.getBloodGroupNeeded() != null ? req.getBloodGroupNeeded().name() : "ANY";
            String acceptedAtStr = response.getAcceptedAt() != null ? response.getAcceptedAt().toString() : java.time.LocalDateTime.now().toString();
            emailService.sendDonorAcceptanceEmailToHospital(
                    hospitalEmail,
                    hospital.getHospitalName(),
                    donorName,
                    bgStr,
                    req.getId(),
                    req.getUnitsRequired(),
                    response.getDistanceKm(),
                    acceptedAtStr
            );
        }
    }

    /**
     * Handles {@link DonationConfirmedEvent} by notifying the donor.
     *
     * @param event the confirmation event
     */
    @EventListener
    public void handleDonationConfirmed(DonationConfirmedEvent event) {
        Donation donation = event.getDonation();
        User donorUser = donation.getDonor().getUser();
        String title = "Donation Match Confirmed";
        String message = String.format(
                "Hospital %s has confirmed your donation schedule for record #%d.",
                donation.getHospital().getHospitalName(), donation.getId()
        );
        notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONATION_CONFIRMED);
    }

    /**
     * Handles {@link DonationCompletedEvent} by notifying the donor, patient, and hospital.
     *
     * @param event the completion event
     */
    @EventListener
    public void handleDonationCompleted(DonationCompletedEvent event) {
        Donation donation = event.getDonation();
        User donorUser = donation.getDonor() != null ? donation.getDonor().getUser() : null;
        User patientUser = donation.getPatient() != null ? donation.getPatient().getUser() : null;
        User hospitalUser = donation.getHospital() != null ? donation.getHospital().getUser() : null;

        String title = "Donation Transaction Completed";
        String message = String.format(
                "A donation of %d units of %s has been completed successfully at %s.",
                donation.getUnitsDonated() != null ? donation.getUnitsDonated() : 1,
                donation.getDonor() != null && donation.getDonor().getBloodGroup() != null ? donation.getDonor().getBloodGroup() : "N/A",
                donation.getHospital() != null ? donation.getHospital().getHospitalName() : "Partner Hospital"
        );

        if (donorUser != null) notificationService.triggerNotificationEvent(donorUser, title, message, NotificationType.DONATION_COMPLETED);
        if (patientUser != null) notificationService.triggerNotificationEvent(patientUser, title, message, NotificationType.DONATION_COMPLETED);
        if (hospitalUser != null) notificationService.triggerNotificationEvent(hospitalUser, title, message, NotificationType.DONATION_COMPLETED);

        // Dispatch Donation Certificate PDF via Email
        if (donorUser != null && donorUser.getEmail() != null && !donorUser.getEmail().isBlank() && emailService != null && certificateService != null) {
            try {
                byte[] pdfBytes = certificateService.generateCertificatePdf(donation);
                String donorFullName = donorUser.getFullName();
                String hospName = donation.getHospital() != null ? donation.getHospital().getHospitalName() : "Partner Hospital";
                String bgStr = donation.getDonor() != null && donation.getDonor().getBloodGroup() != null ? donation.getDonor().getBloodGroup().name() : "N/A";
                String donDateStr = donation.getDonationDate() != null ? donation.getDonationDate().toString() : java.time.LocalDate.now().toString();
                emailService.sendDonationCertificateEmail(
                        donorUser.getEmail(), donorFullName, hospName, bgStr, donation.getUnitsDonated(), donDateStr, donation.getCertificateId(), pdfBytes
                );
            } catch (Exception e) {
                log.error("[EMAIL-CERTIFICATE-EVENT-ERROR] Error triggering certificate email from event listener for donation #{}: {}", donation.getId(), e.getMessage());
            }
        }
    }
}
