package com.bloodbridge.service.impl;

import com.bloodbridge.entity.Donation;
import com.bloodbridge.entity.DonorProfile;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DonationStatus;
import com.bloodbridge.enums.Role;
import com.bloodbridge.exception.DonationNotFoundException;
import com.bloodbridge.exception.InvalidDonationStateException;
import com.bloodbridge.exception.UnauthorizedDonationAccessException;
import com.bloodbridge.exception.UserNotFoundException;
import com.bloodbridge.repository.DonationRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.CertificateService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link CertificateService} providing PDF certificate rendering & ownership validation.
 */
import com.bloodbridge.repository.DonorProfileRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;

    @Override
    @Transactional
    public byte[] getCertificatePdfForDonor(Long donationId, String userEmail) {
        log.info("Requesting blood donation certificate for donation #{} by user {}", donationId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + donationId));

        // Security Ownership Check (Requirement 6 & 7)
        boolean isOwner = false;
        if (donation.getDonor() != null) {
            if (donation.getDonor().getUser() != null && donation.getDonor().getUser().getId().equals(user.getId())) {
                isOwner = true;
            } else if (donation.getDonor().getEmail() != null && donation.getDonor().getEmail().equalsIgnoreCase(user.getEmail())) {
                isOwner = true;
            } else if (donorProfileRepository != null) {
                DonorProfile loggedInDonor = donorProfileRepository.findByUserId(user.getId()).orElse(null);
                if (loggedInDonor != null && loggedInDonor.getId().equals(donation.getDonor().getId())) {
                    isOwner = true;
                }
            }
        }
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            log.warn("[SECURITY-VIOLATION] User {} attempted unauthorized certificate download for donation #{}", userEmail, donationId);
            throw new UnauthorizedDonationAccessException("You are not authorized to view or download this donation certificate.");
        }

        // Completion Status Check (Requirement 2)
        if (donation.getStatus() != DonationStatus.COMPLETED) {
            log.warn("Certificate requested for non-completed donation #{} (Status: {})", donationId, donation.getStatus());
            throw new InvalidDonationStateException("Certificates are only generated for COMPLETED blood donations.");
        }

        // Auto-assign certificate ID if missing
        if (donation.getCertificateId() == null || donation.getCertificateId().isBlank()) {
            String certId = "CERT-BB-" + (donation.getDonationDate() != null ? donation.getDonationDate().getYear() : LocalDate.now().getYear())
                    + "-" + String.format("%06d", donation.getId());
            donation.setCertificateId(certId);
            donationRepository.save(donation);
        }

        return generateCertificatePdf(donation);
    }

    @Override
    public byte[] generateCertificatePdf(Donation donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation entity cannot be null");
        }

        DonorProfile donor = donation.getDonor();
        User donorUser = donor != null ? donor.getUser() : null;

        String donorName = donorUser != null ? donorUser.getFullName() : "Valued Blood Donor";
        String bloodGroupStr = donor != null && donor.getBloodGroup() != null
                ? donor.getBloodGroup().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-")
                : (donation.getBloodRequest() != null && donation.getBloodRequest().getBloodGroupNeeded() != null
                ? donation.getBloodRequest().getBloodGroupNeeded().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "N/A");

        String donationDateStr = donation.getDonationDate() != null
                ? donation.getDonationDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String hospitalName = donation.getHospital() != null ? donation.getHospital().getHospitalName() : "BloodBridge Partner Hospital";
        int unitsDonated = donation.getUnitsDonated() != null ? donation.getUnitsDonated() : 1;
        String certificateId = donation.getCertificateId() != null ? donation.getCertificateId() : "CERT-BB-2026-" + String.format("%06d", donation.getId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Color Palette
            Color primaryRed = new Color(220, 38, 38);
            Color darkSlate = new Color(15, 23, 42);
            Color neutralGray = new Color(71, 85, 105);
            Color borderGold = new Color(217, 119, 6);

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, primaryRed);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, darkSlate);
            Font certTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, borderGold);
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, darkSlate);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, neutralGray);
            Font boldValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, darkSlate);
            Font certIdFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, borderGold);

            // Header Banner
            Paragraph brand = new Paragraph("BloodBridge", headerFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);

            Paragraph title = new Paragraph("CERTIFICATE OF BLOOD DONATION", subHeaderFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            Paragraph textLine1 = new Paragraph("This certificate is proudly presented to", certTitleFont);
            textLine1.setAlignment(Element.ALIGN_CENTER);
            textLine1.setSpacingAfter(10);
            document.add(textLine1);

            Paragraph namePara = new Paragraph(donorName, nameFont);
            namePara.setAlignment(Element.ALIGN_CENTER);
            namePara.setSpacingAfter(15);
            document.add(namePara);

            Paragraph textLine2 = new Paragraph(
                    String.format("In deep appreciation for your noble voluntary blood donation of %d unit(s) of %s blood at %s on %s.",
                            unitsDonated, bloodGroupStr, hospitalName, donationDateStr),
                    bodyFont
            );
            textLine2.setAlignment(Element.ALIGN_CENTER);
            textLine2.setSpacingAfter(25);
            document.add(textLine2);

            // Details Grid Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(90);
            table.setSpacingBefore(10);
            table.setSpacingAfter(25);

            addTableCell(table, "Blood Group", bloodGroupStr, bodyFont, boldValueFont);
            addTableCell(table, "Units Donated", String.valueOf(unitsDonated) + " Unit(s)", bodyFont, boldValueFont);
            addTableCell(table, "Donation Date", donationDateStr, bodyFont, boldValueFont);
            addTableCell(table, "Center / Hospital", hospitalName, bodyFont, boldValueFont);

            document.add(table);

            // Certificate Verification ID & Footer
            Paragraph certIdPara = new Paragraph("Certificate ID: " + certificateId, certIdFont);
            certIdPara.setAlignment(Element.ALIGN_CENTER);
            certIdPara.setSpacingAfter(10);
            document.add(certIdPara);

            Paragraph footer = new Paragraph("Verified & Issued by BloodBridge Smart Health Network • Thank you for saving lives!", bodyFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error rendering PDF donation certificate for donation #{}: {}", donation.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF certificate: " + e.getMessage(), e);
        }
    }

    private void addTableCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBackgroundColor(new Color(248, 250, 252));
        cell.setBorderColor(new Color(226, 232, 240));

        Paragraph pLabel = new Paragraph(label, labelFont);
        Paragraph pVal = new Paragraph(value, valueFont);

        cell.addElement(pLabel);
        cell.addElement(pVal);
        table.addCell(cell);
    }
}
