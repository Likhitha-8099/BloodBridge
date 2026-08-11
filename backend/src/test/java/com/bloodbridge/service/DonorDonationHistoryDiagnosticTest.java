package com.bloodbridge.service;

import com.bloodbridge.dto.DonationSummaryResponse;
import com.bloodbridge.entity.*;
import com.bloodbridge.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DonorDonationHistoryDiagnosticTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorProfileRepository donorProfileRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private MatchedEmergencyDonorRepository matchedDonorRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private DonationService donationService;

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void diagnoseDatabaseAndApiForLikhithaSriSai() {
        System.out.println("================================================================================");
        System.out.println("DIAGNOSTIC TEST REPORT: LIKHITHA MARKONDA & SRISAI HOSPITAL");
        System.out.println("================================================================================");

        List<User> allUsers = userRepository.findAll();
        System.out.println("--> TOTAL USERS IN DB: " + allUsers.size());
        for (User u : allUsers) {
            System.out.println("   [USER] ID=" + u.getId() + " | Email=" + u.getEmail() + " | Name=" + u.getFullName() + " | Role=" + u.getRole());
        }

        List<DonorProfile> allDonors = donorProfileRepository.findAll();
        System.out.println("--> TOTAL DONOR PROFILES IN DB: " + allDonors.size());
        for (DonorProfile dp : allDonors) {
            System.out.println("   [DONOR_PROFILE] ID=" + dp.getId() + " | UserId=" + (dp.getUser() != null ? dp.getUser().getId() : "NULL")
                    + " | Email=" + dp.getEmail() + " | BloodGroup=" + dp.getBloodGroup() + " | Status=" + dp.getStatus());
        }

        List<Hospital> allHospitals = hospitalRepository.findAll();
        System.out.println("--> TOTAL HOSPITALS IN DB: " + allHospitals.size());
        for (Hospital h : allHospitals) {
            System.out.println("   [HOSPITAL] ID=" + h.getId() + " | Name=" + h.getHospitalName() + " | Email=" + h.getEmail());
        }

        List<BloodRequest> allRequests = bloodRequestRepository.findAll();
        System.out.println("--> TOTAL BLOOD REQUESTS IN DB: " + allRequests.size());
        for (BloodRequest br : allRequests) {
            System.out.println("   [BLOOD_REQUEST] ID=" + br.getId() + " | Hospital=" + (br.getHospital() != null ? br.getHospital().getHospitalName() : "NULL")
                    + " | Status=" + br.getStatus() + " | Urgency=" + br.getUrgencyLevel());
        }

        List<MatchedEmergencyDonor> allMatched = matchedDonorRepository.findAll();
        System.out.println("--> TOTAL MATCHED EMERGENCY DONORS IN DB: " + allMatched.size());
        for (MatchedEmergencyDonor med : allMatched) {
            System.out.println("   [MATCHED_EMERGENCY_DONOR] ID=" + med.getId() + " | DonorId=" + (med.getDonor() != null ? med.getDonor().getId() : "NULL")
                    + " | RequestId=" + (med.getBloodRequest() != null ? med.getBloodRequest().getId() : "NULL")
                    + " | Status=" + med.getStatus() + " | Fulfillment=" + med.getFulfillmentStatus());
        }

        List<Donation> allDonations = donationRepository.findAll();
        System.out.println("--> TOTAL DONATIONS IN DB: " + allDonations.size());
        for (Donation d : allDonations) {
            System.out.println("   [DONATION] ID=" + d.getId()
                    + " | DonorProfileId=" + (d.getDonor() != null ? d.getDonor().getId() : "NULL")
                    + " | DonorUserId=" + (d.getDonor() != null && d.getDonor().getUser() != null ? d.getDonor().getUser().getId() : "NULL")
                    + " | DonorEmail=" + (d.getDonor() != null ? d.getDonor().getEmail() : "NULL")
                    + " | Status=" + d.getStatus()
                    + " | HospitalId=" + (d.getHospital() != null ? d.getHospital().getId() : "NULL")
                    + " | HospitalName=" + (d.getHospital() != null ? d.getHospital().getHospitalName() : "NULL")
                    + " | BloodRequestId=" + (d.getBloodRequest() != null ? d.getBloodRequest().getId() : "NULL")
                    + " | completedAt=" + d.getCompletedAt()
                    + " | certificateId=" + d.getCertificateId());
        }

        User likhithaUser = allUsers.stream()
                .filter(u -> u.getEmail().toLowerCase().contains("likhitha") || u.getFullName().toLowerCase().contains("likhitha"))
                .findFirst()
                .orElse(null);

        if (likhithaUser != null) {
            System.out.println("--> CALLING getMyDonations FOR LIKHITHA EMAIL: " + likhithaUser.getEmail());
            List<DonationSummaryResponse> history = donationService.getMyDonations(likhithaUser.getEmail());
            System.out.println("--> API RESPONSE SIZE: " + history.size());
            for (DonationSummaryResponse res : history) {
                System.out.println("   [RESPONSE_ITEM] ID=" + res.getId()
                        + " | Hospital=" + res.getHospitalName()
                        + " | Donor=" + res.getDonorName()
                        + " | BloodGroup=" + res.getBloodGroup()
                        + " | Status=" + res.getStatus()
                        + " | CertAvail=" + res.getCertificateAvailable()
                        + " | CertId=" + res.getCertificateId());
            }
        } else {
            System.out.println("--> NO LIKHITHA USER FOUND IN DB YET.");
        }
        System.out.println("================================================================================");
    }
}
