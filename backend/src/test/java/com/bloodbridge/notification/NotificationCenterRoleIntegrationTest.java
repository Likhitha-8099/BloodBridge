package com.bloodbridge.notification;

import com.bloodbridge.dto.request.SendNotificationRequest;
import com.bloodbridge.dto.response.ApiResponse;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.entity.Hospital;
import com.bloodbridge.entity.User;
import com.bloodbridge.enums.DeliveryChannel;
import com.bloodbridge.enums.NotificationType;
import com.bloodbridge.enums.Role;
import com.bloodbridge.repository.HospitalRepository;
import com.bloodbridge.repository.UserRepository;
import com.bloodbridge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class NotificationCenterRoleIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private NotificationService notificationService;

    private User donorUser;
    private User hospitalUser;
    private User adminUser;
    @SuppressWarnings("unused")
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        donorUser = userRepository.save(User.builder()
                .fullName("Test Donor User")
                .email("donor.test.notif@example.com")
                .password("Password123!")
                .phoneNumber("08011112222")
                .role(Role.DONOR)
                .active(true)
                .build());

        hospitalUser = userRepository.save(User.builder()
                .fullName("Test Hospital User")
                .email("hospital.test.notif@example.com")
                .password("Password123!")
                .phoneNumber("08033334444")
                .role(Role.HOSPITAL)
                .active(true)
                .build());

        hospital = hospitalRepository.save(Hospital.builder()
                .user(hospitalUser)
                .hospitalName("Test Hospital Center")
                .email(hospitalUser.getEmail())
                .registrationNumber("REG-TEST-NOTIF-01")
                .phoneNumber("08033334444")
                .address("123 Hospital Way")
                .city("Hyderabad")
                .state("Telangana")
                .verified(true)
                .build());

        adminUser = userRepository.save(User.builder()
                .fullName("Test Admin User")
                .email("admin.test.notif@example.com")
                .password("Password123!")
                .phoneNumber("08099998888")
                .role(Role.ADMIN)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("Verify Role-based Notification Persistence, Retrieval & Isolation across Donor, Hospital and Admin")
    void testRoleBasedNotificationPersistenceAndRetrieval() {
        // 1. Send Notification to Donor
        SendNotificationRequest donorReq = SendNotificationRequest.builder()
                .recipientUserId(donorUser.getId())
                .title("Donor Blood Match Available")
                .message("You matched an urgent request at Test Hospital Center")
                .type(NotificationType.DONOR_MATCHED)
                .channel(DeliveryChannel.IN_APP)
                .priority("HIGH")
                .build();
        notificationService.sendNotification(donorReq);

        // 2. Send Notification to Hospital
        SendNotificationRequest hospReq = SendNotificationRequest.builder()
                .recipientUserId(hospitalUser.getId())
                .title("Emergency Donor Accepted")
                .message("Donor Test Donor User accepted request #101")
                .type(NotificationType.DONOR_ACCEPTED)
                .channel(DeliveryChannel.IN_APP)
                .priority("CRITICAL")
                .build();
        notificationService.sendNotification(hospReq);

        // 3. Send Notification to Admin
        SendNotificationRequest adminReq = SendNotificationRequest.builder()
                .recipientUserId(adminUser.getId())
                .title("System Health Metric Alert")
                .message("High database connection usage detected")
                .type(NotificationType.SYSTEM_NOTIFICATION)
                .channel(DeliveryChannel.IN_APP)
                .priority("NORMAL")
                .build();
        notificationService.sendNotification(adminReq);

        // 4. Verify Donor Notification Retrieval
        ApiResponse<Map<String, Object>> donorNotifs = notificationService.getNotificationsPaginated(
                donorUser.getEmail(), 0, 20, null, null, null, null);
        assertThat(donorNotifs).isNotNull();
        @SuppressWarnings("unchecked")
        List<NotificationResponse> donorItems = (List<NotificationResponse>) donorNotifs.getData().get("notifications");
        assertThat(donorItems).hasSize(1);
        assertThat(donorItems.get(0).getTitle()).isEqualTo("Donor Blood Match Available");

        // 5. Verify Hospital Notification Retrieval
        ApiResponse<Map<String, Object>> hospNotifs = notificationService.getNotificationsPaginated(
                hospitalUser.getEmail(), 0, 20, null, null, null, null);
        assertThat(hospNotifs).isNotNull();
        @SuppressWarnings("unchecked")
        List<NotificationResponse> hospItems = (List<NotificationResponse>) hospNotifs.getData().get("notifications");
        assertThat(hospItems).hasSize(1);
        assertThat(hospItems.get(0).getTitle()).isEqualTo("Emergency Donor Accepted");

        // 6. Verify Admin Notification Retrieval
        ApiResponse<Map<String, Object>> adminNotifs = notificationService.getNotificationsPaginated(
                adminUser.getEmail(), 0, 20, null, null, null, null);
        assertThat(adminNotifs).isNotNull();
        @SuppressWarnings("unchecked")
        List<NotificationResponse> adminItems = (List<NotificationResponse>) adminNotifs.getData().get("notifications");
        assertThat(adminItems).hasSize(1);
        assertThat(adminItems.get(0).getTitle()).isEqualTo("System Health Metric Alert");

        // 7. Security & Isolation Check: Donor cannot see Hospital or Admin notifications
        assertThat(donorItems.stream().noneMatch(n -> n.getTitle().contains("System") || n.getTitle().contains("Accepted"))).isTrue();

        // 8. Read/Unread State Verification
        NotificationResponse firstNotif = donorItems.get(0);
        notificationService.markAsRead(donorUser.getEmail(), firstNotif.getId());

        ApiResponse<List<NotificationResponse>> unreadResp = notificationService.getUnreadNotifications(donorUser.getEmail());
        assertThat(unreadResp.getData()).isEmpty();
    }
}
