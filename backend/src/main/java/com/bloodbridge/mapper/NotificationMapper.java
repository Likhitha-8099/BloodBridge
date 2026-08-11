package com.bloodbridge.mapper;

import com.bloodbridge.dto.NotificationDTO;
import com.bloodbridge.dto.response.NotificationResponse;
import com.bloodbridge.entity.Notification;
import com.bloodbridge.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for translating notification entities to response DTOs.
 */
@Component
public class NotificationMapper {

    /**
     * Maps a {@link Notification} entity to a detailed {@link NotificationResponse}.
     *
     * @param notification notification entity
     * @return mapped NotificationResponse DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        User recipient = notification.getRecipientUser();
        var donor = notification.getDonor();
        var hospital = notification.getHospital();
        var patient = notification.getPatient();
        var bloodRequest = notification.getBloodRequest();

        String donorName = null;
        String bloodGroupStr = null;
        Long donorId = null;

        if (donor != null) {
            donorId = donor.getId();
            if (donor.getUser() != null) {
                donorName = donor.getUser().getFullName();
            }
            if (donor.getBloodGroup() != null) {
                bloodGroupStr = donor.getBloodGroup().name();
            }
        }

        Long patientId = null;
        String patientName = null;
        if (patient != null) {
            patientId = patient.getId();
            if (patient.getUser() != null) {
                patientName = patient.getUser().getFullName();
            } else {
                patientName = patient.getEmergencyContactName();
            }
        }

        Boolean readFlag = notification.getIsRead();
        Long recipientId = recipient != null ? recipient.getId() : null;
        Long reqId = bloodRequest != null ? bloodRequest.getId() : notification.getRelatedEntityId();

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(recipientId)
                .recipientUserId(recipientId)
                .emergencyRequestId(reqId)
                .recipientRole(notification.getRecipientRole() != null ? notification.getRecipientRole() : (recipient != null && recipient.getRole() != null ? recipient.getRole().name() : null))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .body(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .category(notification.getCategory())
                .deliveryChannel(notification.getDeliveryChannel())
                .channel(notification.getDeliveryChannel())
                .priority(notification.getPriority())
                .priorityEnum(notification.getPriorityEnum())
                .status(notification.getStatus())
                .readStatus(readFlag)
                .isRead(readFlag)
                .donorId(donorId)
                .donorName(donorName)
                .bloodGroup(bloodGroupStr)
                .requestId(reqId)
                .hospitalId(hospital != null ? hospital.getId() : null)
                .hospitalName(hospital != null ? hospital.getHospitalName() : null)
                .patientId(patientId)
                .patientName(patientName)
                .actionUrl(notification.getActionUrl())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .readTime(notification.getReadAt())
                .expiryTime(notification.getExpiryTime())
                .metadataJson(notification.getMetadataJson())
                .createdAt(notification.getCreatedAt())
                .createdTime(notification.getCreatedAt())
                .build();
    }

    public NotificationDTO toDto(Notification notification) {
        if (notification == null) return null;
        NotificationResponse res = toResponse(notification);
        return NotificationDTO.builder()
                .id(res.getId())
                .recipientUserId(res.getRecipientUserId())
                .recipientRole(res.getRecipientRole())
                .title(res.getTitle())
                .message(res.getMessage())
                .notificationType(res.getNotificationType())
                .deliveryChannel(res.getDeliveryChannel())
                .priority(res.getPriority())
                .status(res.getStatus())
                .donorId(res.getDonorId())
                .donorName(res.getDonorName())
                .bloodGroup(res.getBloodGroup())
                .requestId(res.getRequestId())
                .hospitalId(res.getHospitalId())
                .hospitalName(res.getHospitalName())
                .patientId(res.getPatientId())
                .patientName(res.getPatientName())
                .actionUrl(res.getActionUrl())
                .readStatus(res.getReadStatus())
                .isRead(res.getIsRead())
                .relatedEntityType(res.getRelatedEntityType())
                .relatedEntityId(res.getRelatedEntityId())
                .sentAt(res.getSentAt())
                .readAt(res.getReadAt())
                .createdAt(res.getCreatedAt())
                .build();
    }
}
