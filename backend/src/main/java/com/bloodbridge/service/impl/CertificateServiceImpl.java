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
import com.bloodbridge.repository.DonorProfileRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.CertificateService;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Premium BloodBridge donation certificate renderer.
 *
 * <p>All business logic (security checks, ownership validation, completion status,
 * certificate-ID auto-assign) is UNCHANGED. Only the visual PDF layout is redesigned.
 *
 * <p>Design features:
 * <ul>
 *   <li>Warm off-white background with subtle diagonal grid watermark</li>
 *   <li>Large curved red+gold decorative arcs on LEFT edge and BOTTOM-RIGHT corner</li>
 *   <li>Triple-layer inner border: gold outer, red middle, gold hairline with corner diamonds</li>
 *   <li>Prominent header: BloodBridge logo + brand name + tagline + header divider</li>
 *   <li>46pt Times-Bold CERTIFICATE title + tracked gold OF APPRECIATION subtitle</li>
 *   <li>34pt italic donor name with red+gold underline + italic appreciation message</li>
 *   <li>Left visual zone: triple blood-drop layers + gold EKG heartbeat + helping hands</li>
 *   <li>6-field information panel with red-dot labels and dark navy values</li>
 *   <li>Authorized signature block (bottom right)</li>
 *   <li>Dark red bottom thank-you banner with gold accent lines</li>
 *   <li>Appreciation seal badge overlay</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;

    // ──────────────────────────────────────────────────────────────────────────
    //  getCertificatePdfForDonor  —  security, ownership & completion checks
    //  ← UNCHANGED from original
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public byte[] getCertificatePdfForDonor(Long donationId, String userEmail) {
        log.info("Requesting blood donation certificate for donation #{} by user {}", donationId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new DonationNotFoundException("Donation record not found for ID: " + donationId));

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

        if (donation.getStatus() != DonationStatus.COMPLETED) {
            log.warn("Certificate requested for non-completed donation #{} (Status: {})", donationId, donation.getStatus());
            throw new InvalidDonationStateException("Certificates are only generated for COMPLETED blood donations.");
        }

        if (donation.getCertificateId() == null || donation.getCertificateId().isBlank()) {
            String certId = "CERT-BB-"
                    + (donation.getDonationDate() != null ? donation.getDonationDate().getYear() : LocalDate.now().getYear())
                    + "-" + String.format("%06d", donation.getId());
            donation.setCertificateId(certId);
            donationRepository.save(donation);
        }

        return generateCertificatePdf(donation);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  generateCertificatePdf  —  premium landscape certificate
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public byte[] generateCertificatePdf(Donation donation) {
        if (donation == null) throw new IllegalArgumentException("Donation entity cannot be null");

        // ── Dynamic data (never hardcoded) ────────────────────────────────
        DonorProfile donor     = donation.getDonor();
        User         donorUser = donor != null ? donor.getUser() : null;

        String donorName = donorUser != null && donorUser.getFullName() != null && !donorUser.getFullName().isBlank()
                ? donorUser.getFullName().trim()
                : (donor != null && donor.getEmail() != null ? donor.getEmail() : "Valued Blood Donor");

        String bloodGroupStr = donor != null && donor.getBloodGroup() != null
                ? donor.getBloodGroup().name().replace("_POSITIVE", "+").replace("_NEGATIVE", "-")
                : (donation.getBloodRequest() != null && donation.getBloodRequest().getBloodGroupNeeded() != null
                   ? donation.getBloodRequest().getBloodGroupNeeded().name()
                            .replace("_POSITIVE", "+").replace("_NEGATIVE", "-") : "O+");

        String donationDateStr = donation.getDonationDate() != null
                ? donation.getDonationDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                : LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String hospitalName = donation.getHospital() != null
                && donation.getHospital().getHospitalName() != null
                && !donation.getHospital().getHospitalName().isBlank()
                ? donation.getHospital().getHospitalName().trim()
                : "BloodBridge Partner Hospital";

        int unitsDonated = donation.getUnitsDonated() != null && donation.getUnitsDonated() > 0
                ? donation.getUnitsDonated() : 1;

        String certificateId = donation.getCertificateId() != null && !donation.getCertificateId().isBlank()
                ? donation.getCertificateId()
                : "CERT-BB-" + LocalDate.now().getYear() + "-" + String.format("%06d", donation.getId());

        String donationIdStr = "DON-" + String.format("%06d", donation.getId());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Page: A4 Landscape ──────────────────────────────────────────
            final float W = 841.89f, H = 595.28f;
            Document  doc    = new Document(new com.lowagie.text.Rectangle(W, H), 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();
            doc.newPage();

            PdfContentByte cb  = writer.getDirectContent();
            PdfContentByte cbU = writer.getDirectContentUnder();

            // ── Color palette ───────────────────────────────────────────────
            final Color BG    = new Color(253, 251, 248);
            final Color RED   = new Color(185,  28,  28);
            final Color DKRED = new Color(140,  20,  20);
            final Color NAVY  = new Color( 15,  23,  42);
            final Color GOLD  = new Color(180, 122,   0);
            final Color LGOLD = new Color(253, 244, 210);
            final Color AGOLD = new Color(212, 160,  23);
            final Color SLATE = new Color( 80,  96, 115);
            final Color WHITE = new Color(255, 255, 255);
            final Color WFADE = new Color(250, 235, 235);

            // ── Base fonts ──────────────────────────────────────────────────
            BaseFont hb  = BaseFont.createFont(BaseFont.HELVETICA_BOLD,   BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont h   = BaseFont.createFont(BaseFont.HELVETICA,        BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont tb  = BaseFont.createFont(BaseFont.TIMES_BOLD,       BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont ti  = BaseFont.createFont(BaseFont.TIMES_ITALIC,     BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont tbi = BaseFont.createFont(BaseFont.TIMES_BOLDITALIC, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // ==============================================================
            //  1. WARM OFF-WHITE BACKGROUND
            // ==============================================================
            cbU.saveState();
            cbU.setColorFill(BG);
            cbU.rectangle(0, 0, W, H);
            cbU.fill();
            cbU.restoreState();

            // ==============================================================
            //  2. SUBTLE DIAGONAL GRID WATERMARK
            // ==============================================================
            cbU.saveState();
            cbU.setColorStroke(new Color(245, 225, 225));
            cbU.setLineWidth(0.30f);
            for (float x = -H; x < W + H; x += 26f) {
                cbU.moveTo(x, 0);
                cbU.lineTo(x + H, H);
            }
            cbU.stroke();
            cbU.restoreState();

            // Centre watermark blood-drop (very faint)
            cbU.saveState();
            cbU.setColorFill(WFADE);
            bloodDrop(cbU, W / 2f, H / 2f + 10f, 72f);
            cbU.fill();
            cbU.restoreState();

            // ==============================================================
            //  3. LARGE CURVED DECORATIVE ARC — LEFT EDGE
            //     Three nested curves create a bold red+gold swoosh
            // ==============================================================
            cbU.saveState();
            // Outer red arc
            cbU.setColorFill(DKRED);
            cbU.moveTo(0, 0);
            cbU.lineTo(0, H);
            cbU.curveTo(0, H,   112f, H * 0.76f, 85f, H * 0.50f);
            cbU.curveTo(85f, H * 0.50f,  112f, H * 0.24f, 0, 0);
            cbU.closePath();
            cbU.fill();
            // Mid gold arc
            cbU.setColorFill(AGOLD);
            cbU.moveTo(0, 0);
            cbU.lineTo(0, H);
            cbU.curveTo(0, H,    66f, H * 0.76f, 48f, H * 0.50f);
            cbU.curveTo(48f, H * 0.50f,  66f, H * 0.24f, 0, 0);
            cbU.closePath();
            cbU.fill();
            // Inner red highlight
            cbU.setColorFill(RED);
            cbU.moveTo(0, 0);
            cbU.lineTo(0, H);
            cbU.curveTo(0, H,    36f, H * 0.78f, 24f, H * 0.50f);
            cbU.curveTo(24f, H * 0.50f,  36f, H * 0.22f, 0, 0);
            cbU.closePath();
            cbU.fill();
            cbU.restoreState();

            // ==============================================================
            //  4. LARGE CURVED DECORATIVE ARC — BOTTOM-RIGHT CORNER
            // ==============================================================
            cbU.saveState();
            // Outer red wedge (bottom)
            cbU.setColorFill(DKRED);
            cbU.moveTo(W, 0);
            cbU.curveTo(W, 0,   W - 85f, 44f, W * 0.66f, 0);
            cbU.closePath();
            cbU.fill();
            // Outer red wedge (right side)
            cbU.moveTo(W, 0);
            cbU.curveTo(W, 0,   W, 88f, W - 44f, H * 0.22f);
            cbU.lineTo(W, H * 0.22f);
            cbU.closePath();
            cbU.fill();
            // Gold accent (bottom)
            cbU.setColorFill(AGOLD);
            cbU.moveTo(W, 0);
            cbU.curveTo(W, 0,   W - 52f, 26f, W * 0.70f, 0);
            cbU.closePath();
            cbU.fill();
            // Gold accent (right)
            cbU.moveTo(W, 0);
            cbU.curveTo(W, 0,   W, 54f, W - 28f, H * 0.14f);
            cbU.lineTo(W, H * 0.14f);
            cbU.closePath();
            cbU.fill();
            cbU.restoreState();

            // ==============================================================
            //  5. TRIPLE-LAYER INNER BORDER
            //     Inset from left arc (starts at x=93) and page edges
            // ==============================================================
            final float BX = 93f, BY = 18f, BW = W - BX - 18f, BH = H - BY * 2f;
            cbU.saveState();
            cbU.setColorStroke(GOLD);  cbU.setLineWidth(2.2f);
            cbU.rectangle(BX, BY, BW, BH); cbU.stroke();
            cbU.setColorStroke(RED);   cbU.setLineWidth(1.0f);
            cbU.rectangle(BX + 5, BY + 5, BW - 10, BH - 10); cbU.stroke();
            cbU.setColorStroke(GOLD);  cbU.setLineWidth(0.4f);
            cbU.rectangle(BX + 9, BY + 9, BW - 18, BH - 18); cbU.stroke();
            // Corner diamond ornaments
            diamond(cbU, BX,      BY,      7f, GOLD);
            diamond(cbU, BX + BW, BY,      7f, GOLD);
            diamond(cbU, BX,      BY + BH, 7f, GOLD);
            diamond(cbU, BX + BW, BY + BH, 7f, GOLD);
            cbU.restoreState();

            // ==============================================================
            //  6. TOP RED ACCENT STRIP (below top border line)
            // ==============================================================
            cb.saveState();
            cb.setColorFill(RED);
            cb.rectangle(BX + 12, H - BY - 16f, BW - 24f, 5.5f); cb.fill();
            cb.restoreState();

            // ==============================================================
            //  7. HEADER — Logo + Brand (center)
            // ==============================================================
            final float HDR_CX   = BX + BW / 2f;
            final float LOGO_TGT = 38f;
            boolean logoOK = false;
            try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                if (is != null) {
                    byte[] lb = is.readAllBytes();
                    if (lb.length > 0) {
                        Image logo = Image.getInstance(lb);
                        float lw = logo.getWidth() * (LOGO_TGT / logo.getHeight());
                        logo.scaleAbsolute(lw, LOGO_TGT);
                        logo.setAbsolutePosition(HDR_CX - lw / 2f, H - BY - 16f - LOGO_TGT - 3f);
                        cb.addImage(logo);
                        logoOK = true;
                    }
                }
            } catch (Exception e) {
                log.warn("[CERT] Logo load failed: {}", e.getMessage());
            }

            float brandY = H - BY - 16f - LOGO_TGT - (logoOK ? 18f : 4f);
            txt(cb, hb, 16f, RED,  "BLOODBRIDGE",                   HDR_CX, brandY,       PdfContentByte.ALIGN_CENTER);
            txt(cb, ti,  8f, GOLD, "BRIDGING LIVES, SHARING HOPE",  HDR_CX, brandY - 13f, PdfContentByte.ALIGN_CENTER);

            // Gold + red double divider below header
            final float DIV_Y = brandY - 22f;
            cb.saveState();
            cb.setColorStroke(GOLD); cb.setLineWidth(1.4f);
            cb.moveTo(BX + 14, DIV_Y); cb.lineTo(BX + BW - 14, DIV_Y); cb.stroke();
            cb.setColorStroke(RED);  cb.setLineWidth(0.6f);
            cb.moveTo(BX + 14, DIV_Y - 4f); cb.lineTo(BX + BW - 14, DIV_Y - 4f); cb.stroke();
            cb.restoreState();

            // Cert-ID small label below divider (left-aligned)
            txt(cb, hb, 6.5f, SLATE, "CERT ID:",    BX + 18f, DIV_Y - 11f, PdfContentByte.ALIGN_LEFT);
            txt(cb, hb, 6.5f, NAVY,  certificateId, BX + 57f, DIV_Y - 11f, PdfContentByte.ALIGN_LEFT);

            // ==============================================================
            //  8. CERTIFICATE TITLE
            // ==============================================================
            final float TITLE_Y = DIV_Y - 52f;

            // Red side bars flanking the title
            cb.saveState();
            cb.setColorFill(RED);
            cb.rectangle(BX + 14,       TITLE_Y + 6f, 90f, 3.5f); cb.fill();
            cb.rectangle(BX + BW - 104, TITLE_Y + 6f, 90f, 3.5f); cb.fill();
            cb.restoreState();

            txt(cb, tb,  46f, NAVY, "CERTIFICATE",                    HDR_CX, TITLE_Y,        PdfContentByte.ALIGN_CENTER);
            txt(cb, hb,  11f, GOLD, "O F   A P P R E C I A T I O N", HDR_CX, TITLE_Y - 20f,  PdfContentByte.ALIGN_CENTER);

            // Diamond ornament row
            final float DOR_Y = TITLE_Y - 36f;
            cb.saveState();
            cb.setColorStroke(GOLD); cb.setLineWidth(0.7f);
            cb.moveTo(HDR_CX - 155f, DOR_Y + 3); cb.lineTo(HDR_CX - 78f, DOR_Y + 3); cb.stroke();
            cb.moveTo(HDR_CX + 78f,  DOR_Y + 3); cb.lineTo(HDR_CX + 155f, DOR_Y + 3); cb.stroke();
            cb.restoreState();
            diamond(cb, HDR_CX - 78f, DOR_Y + 3, 4f,   GOLD);
            diamond(cb, HDR_CX,       DOR_Y + 3, 5.5f,  RED);
            diamond(cb, HDR_CX + 78f, DOR_Y + 3, 4f,   GOLD);

            // ==============================================================
            //  9. DONOR SECTION
            // ==============================================================
            final float PRES_Y = DOR_Y - 15f;
            final float NAME_Y = PRES_Y - 37f;

            txt(cb, hb,  8.5f, GOLD, "P R O U D L Y   P R E S E N T E D   T O", HDR_CX, PRES_Y, PdfContentByte.ALIGN_CENTER);
            txt(cb, tbi, 34f,  NAVY, donorName,                                    HDR_CX, NAME_Y, PdfContentByte.ALIGN_CENTER);

            // Name underline: thick red + thin gold
            float nHalf = Math.min(tbi.getWidthPoint(donorName, 34f) / 2f + 20f, 205f);
            cb.saveState();
            cb.setColorStroke(RED);  cb.setLineWidth(2.2f);
            cb.moveTo(HDR_CX - nHalf,      NAME_Y - 6f); cb.lineTo(HDR_CX + nHalf,      NAME_Y - 6f); cb.stroke();
            cb.setColorStroke(GOLD); cb.setLineWidth(0.8f);
            cb.moveTo(HDR_CX - nHalf - 14, NAME_Y - 9f); cb.lineTo(HDR_CX + nHalf + 14, NAME_Y - 9f); cb.stroke();
            cb.restoreState();

            // Appreciation message
            txt(cb, ti, 9f, SLATE,
                    "In grateful recognition of your generous blood donation and selfless contribution towards saving lives.",
                    HDR_CX, NAME_Y - 24f, PdfContentByte.ALIGN_CENTER);
            txt(cb, ti, 9f, SLATE,
                    "Your act of kindness brings hope and strength to many. Thank you for being a true hero!",
                    HDR_CX, NAME_Y - 37f, PdfContentByte.ALIGN_CENTER);

            // ==============================================================
            // 10. LEFT VISUAL — blood drop + EKG + helping hands
            //     Within left decorative arc zone (x 0–93), on cbU
            // ==============================================================
            final float LV_CX = 46f, LV_CY = H / 2f + 16f;

            cbU.saveState();
            // Outer glow
            cbU.setColorFill(new Color(255, 220, 220));
            bloodDrop(cbU, LV_CX, LV_CY + 30f, 30f); cbU.fill();
            // Mid layer
            cbU.setColorFill(new Color(215, 60, 60));
            bloodDrop(cbU, LV_CX, LV_CY + 30f, 19f); cbU.fill();
            // Core
            cbU.setColorFill(new Color(255, 255, 255, 200));
            bloodDrop(cbU, LV_CX, LV_CY + 30f, 9f); cbU.fill();

            // EKG / heartbeat line
            cbU.setColorStroke(new Color(255, 215, 90)); cbU.setLineWidth(1.8f);
            float ex = LV_CX - 44f, ey = LV_CY - 8f;
            cbU.moveTo(ex,        ey);
            cbU.lineTo(ex + 12f,  ey);
            cbU.lineTo(ex + 19f,  ey + 17f);
            cbU.lineTo(ex + 25f,  ey - 23f);
            cbU.lineTo(ex + 31f,  ey + 13f);
            cbU.lineTo(ex + 37f,  ey);
            cbU.lineTo(ex + 90f,  ey);
            cbU.stroke();

            // Helping hands (two cupped-hand silhouettes, white semi-transparent)
            cbU.setColorFill(new Color(255, 255, 255, 70));
            // Left cupped hand
            cbU.moveTo(LV_CX - 24f, LV_CY - 40f);
            cbU.curveTo(LV_CX - 24f, LV_CY - 52f, LV_CX - 8f, LV_CY - 54f, LV_CX - 3f, LV_CY - 40f);
            cbU.curveTo(LV_CX - 3f,  LV_CY - 32f, LV_CX - 13f, LV_CY - 28f, LV_CX - 24f, LV_CY - 33f);
            cbU.closePath(); cbU.fill();
            // Right cupped hand
            cbU.moveTo(LV_CX + 24f, LV_CY - 40f);
            cbU.curveTo(LV_CX + 24f, LV_CY - 52f, LV_CX + 8f, LV_CY - 54f, LV_CX + 3f, LV_CY - 40f);
            cbU.curveTo(LV_CX + 3f,  LV_CY - 32f, LV_CX + 13f, LV_CY - 28f, LV_CX + 24f, LV_CY - 33f);
            cbU.closePath(); cbU.fill();
            cbU.restoreState();

            // ==============================================================
            // 11. INFORMATION PANEL — 6 fields, 2 rows x 3 cols
            // ==============================================================
            final float PNL_TOP = 260f, PNL_BOT = 98f;
            final float PNL_X   = BX + 12f, PNL_W = BW - 24f, PNL_H = PNL_TOP - PNL_BOT;

            cb.saveState();
            cb.setColorFill(LGOLD);
            cb.rectangle(PNL_X, PNL_BOT, PNL_W, PNL_H); cb.fill();
            cb.setColorStroke(GOLD); cb.setLineWidth(0.8f);
            cb.rectangle(PNL_X, PNL_BOT, PNL_W, PNL_H); cb.stroke();
            cb.restoreState();

            // Panel header bar
            final float PH_H = 22f, PH_Y = PNL_TOP - PH_H;
            cb.saveState();
            cb.setColorFill(RED);
            cb.rectangle(PNL_X, PH_Y, PNL_W, PH_H); cb.fill();
            cb.setColorStroke(AGOLD); cb.setLineWidth(1.0f);
            cb.moveTo(PNL_X, PH_Y + PH_H); cb.lineTo(PNL_X + PNL_W, PH_Y + PH_H); cb.stroke();
            cb.restoreState();
            txt(cb, hb, 8f, WHITE, "DONATION  DETAILS", HDR_CX, PH_Y + 8f, PdfContentByte.ALIGN_CENTER);

            // Column separators
            float colW = PNL_W / 3f;
            cb.saveState();
            cb.setColorStroke(new Color(205, 175, 110)); cb.setLineWidth(0.6f);
            cb.moveTo(PNL_X + colW,     PNL_BOT + 8); cb.lineTo(PNL_X + colW,     PH_Y - 2); cb.stroke();
            cb.moveTo(PNL_X + 2 * colW, PNL_BOT + 8); cb.lineTo(PNL_X + 2 * colW, PH_Y - 2); cb.stroke();
            cb.restoreState();

            float cx1 = PNL_X + colW / 2f;
            float cx2 = PNL_X + colW + colW / 2f;
            float cx3 = PNL_X + 2f * colW + colW / 2f;
            float ry1 = PH_Y - 30f;
            float ry2 = PH_Y - 80f;

            infoCell(cb, cx1, ry1, RED, hb, h, "\u25CF  DATE OF DONATION",  donationDateStr);
            infoCell(cb, cx2, ry1, RED, hb, h, "\u25CF  BLOOD GROUP",        bloodGroupStr);
            infoCell(cb, cx3, ry1, RED, hb, h, "\u25CF  DONATION ID",        donationIdStr);
            infoCell(cb, cx1, ry2, RED, hb, h, "\u25CF  DONATION TYPE",      "Whole Blood");
            infoCell(cb, cx2, ry2, RED, hb, h, "\u25CF  DONATION LOCATION",  clip(hospitalName, 26));
            infoCell(cb, cx3, ry2, RED, hb, h, "\u25CF  UNITS DONATED",      unitsDonated + " Unit(s)");

            // ==============================================================
            // 12. AUTHORIZED SIGNATURE (bottom right, above banner)
            // ==============================================================
            final float SIG_X = BX + BW - 22f, SIG_Y = PNL_BOT - 18f;
            txt(cb, tbi, 11f, NAVY,  "BloodBridge Team",                  SIG_X, SIG_Y + 3f,  PdfContentByte.ALIGN_RIGHT);
            cb.saveState();
            cb.setColorStroke(NAVY); cb.setLineWidth(0.7f);
            cb.moveTo(SIG_X - 125f, SIG_Y); cb.lineTo(SIG_X, SIG_Y); cb.stroke();
            cb.restoreState();
            txt(cb, hb, 6.5f, SLATE, "AUTHORIZED SIGNATURE",             SIG_X, SIG_Y - 11f, PdfContentByte.ALIGN_RIGHT);
            txt(cb, h,  6.5f, SLATE, "BloodBridge Smart Health Network", SIG_X, SIG_Y - 20f, PdfContentByte.ALIGN_RIGHT);

            // ==============================================================
            // 13. BOTTOM THANK-YOU BANNER
            // ==============================================================
            final float BNR_Y = BY + 2f, BNR_H = 28f;
            cb.saveState();
            cb.setColorFill(DKRED);
            cb.rectangle(BX + 12, BNR_Y, BW - 24f, BNR_H); cb.fill();
            cb.setColorStroke(AGOLD); cb.setLineWidth(1.4f);
            cb.moveTo(BX + 12, BNR_Y + BNR_H); cb.lineTo(BX + BW - 12, BNR_Y + BNR_H); cb.stroke();
            cb.moveTo(BX + 12, BNR_Y);          cb.lineTo(BX + BW - 12, BNR_Y);          cb.stroke();
            cb.restoreState();
            txt(cb, hb, 10f, WHITE,
                    "THANK YOU FOR MAKING A DIFFERENCE   \u2014   YOU ARE A LIFESAVER",
                    HDR_CX, BNR_Y + 9f, PdfContentByte.ALIGN_CENTER);

            // ==============================================================
            // 14. APPRECIATION SEAL BADGE (overlapping bottom-right area)
            // ==============================================================
            sealBadge(cb, BX + BW - 56f, PNL_BOT - 10f, 36f, RED, GOLD, LGOLD, WHITE, hb);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[CERT] Error generating certificate for donation #{}: {}", donation.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF certificate: " + e.getMessage(), e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  PDF Drawing Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Renders text at absolute position with the given alignment. */
    private void txt(PdfContentByte cb, BaseFont font, float size, Color color,
                     String text, float x, float y, int align) {
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(font, size);
        cb.setColorFill(color);
        cb.showTextAligned(align, text, x, y, 0);
        cb.endText();
        cb.restoreState();
    }

    /** Fills a rotated square (diamond) centred at (x, y) with the given half-size r. */
    private void diamond(PdfContentByte cb, float x, float y, float r, Color color) {
        cb.saveState();
        cb.setColorFill(color);
        cb.moveTo(x,     y + r);
        cb.lineTo(x + r, y);
        cb.lineTo(x,     y - r);
        cb.lineTo(x - r, y);
        cb.closePath();
        cb.fill();
        cb.restoreState();
    }

    /**
     * Appends a teardrop (blood-drop) Bezier path — tip points UP.
     * Caller MUST invoke fill() or stroke() afterwards.
     */
    private void bloodDrop(PdfContentByte cb, float cx, float cy, float r) {
        float ty = cy + r * 1.45f;
        cb.moveTo(cx, ty);
        cb.curveTo(cx + r * 0.72f, cy + r * 0.90f, cx + r,            cy,            cx + r, cy - r * 0.20f);
        cb.curveTo(cx + r,          cy - r * 0.80f, cx + r * 0.60f, cy - r, cx,     cy - r);
        cb.curveTo(cx - r * 0.60f,  cy - r,         cx - r, cy - r * 0.80f, cx - r, cy - r * 0.20f);
        cb.curveTo(cx - r,          cy,              cx - r * 0.72f, cy + r * 0.90f, cx, ty);
        cb.closePath();
    }

    /**
     * Draws a circular appreciation seal: concentric gold/red rings,
     * central blood-drop icon, star accents, and "EVERY DONATION / SAVES LIVES".
     */
    private void sealBadge(PdfContentByte cb, float cx, float cy, float r,
                            Color red, Color gold, Color lgold, Color white, BaseFont bold) {
        cb.saveState();
        cb.setColorFill(lgold);
        cb.circle(cx, cy, r); cb.fill();
        cb.setColorStroke(gold); cb.setLineWidth(2.0f);
        cb.circle(cx, cy, r); cb.stroke();
        cb.setColorStroke(red); cb.setLineWidth(1.0f);
        cb.circle(cx, cy, r - 4f); cb.stroke();
        cb.setColorFill(new Color(255, 248, 248));
        cb.circle(cx, cy, r - 6f); cb.fill();
        cb.setColorFill(red);
        bloodDrop(cb, cx, cy - 6f, 8f); cb.fill();
        cb.beginText();
        cb.setFontAndSize(bold, 5.5f);
        cb.setColorFill(red);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "EVERY DONATION", cx, cy + 13f, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "SAVES LIVES",    cx, cy +  5f, 0);
        cb.setColorFill(gold);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "\u2605 \u2605 \u2605", cx, cy + r - 12f, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "\u2605 \u2605 \u2605", cx, cy - r +  8f, 0);
        cb.endText();
        cb.restoreState();
    }

    /**
     * Draws a two-row info cell: red-dot + label above, large navy bold value below.
     */
    private void infoCell(PdfContentByte cb, float cx, float cy, Color accent,
                           BaseFont bold, BaseFont regular, String label, String value) {
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(bold, 7f);
        cb.setColorFill(accent);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label, cx, cy, 0);
        cb.endText();
        float lw = bold.getWidthPoint(label, 7f) / 2f;
        cb.setColorStroke(accent); cb.setLineWidth(0.35f);
        cb.moveTo(cx - lw, cy - 2f); cb.lineTo(cx + lw, cy - 2f); cb.stroke();
        cb.beginText();
        cb.setFontAndSize(bold, 12f);
        cb.setColorFill(new Color(15, 23, 42));
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, value, cx, cy - 17f, 0);
        cb.endText();
        cb.restoreState();
    }

    /** Clips text to maxChars, appending an ellipsis if truncated. */
    private String clip(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) return text;
        return text.substring(0, maxChars - 1) + "\u2026";
    }
}