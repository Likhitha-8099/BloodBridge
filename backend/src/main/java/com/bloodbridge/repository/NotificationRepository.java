package com.bloodbridge.repository;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.NotificationCategory;
import com.bloodbridge.enums.NotificationPriority;
import com.bloodbridge.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for {@link Notification} entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByDeliveryChannel(com.bloodbridge.enums.DeliveryChannel deliveryChannel);

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId AND (n.deleted = false OR n.deleted IS NULL) ORDER BY n.createdAt DESC")
    List<Notification> findUserNotifications(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId AND n.readStatus = false AND (n.deleted = false OR n.deleted IS NULL) ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUser.id = :userId AND n.readStatus = false AND (n.deleted = false OR n.deleted IS NULL)")
    long countUnreadByRecipientUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true, n.readAt = :readAt WHERE n.recipientUser.id = :userId AND n.readStatus = false AND (n.deleted = false OR n.deleted IS NULL)")
    void markAllAsReadForUser(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE Notification n SET n.deleted = true WHERE n.id = :id AND n.recipientUser.id = :userId")
    int softDeleteNotification(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId AND (n.deleted = false OR n.deleted IS NULL) AND (:category IS NULL OR n.category = :category) AND (:priority IS NULL OR n.priorityEnum = :priority OR n.priority = :priorityStr) AND (:readStatus IS NULL OR n.readStatus = :readStatus)")
    Page<Notification> findUserNotificationsFiltered(
            @Param("userId") Long userId,
            @Param("category") NotificationCategory category,
            @Param("priority") NotificationPriority priority,
            @Param("priorityStr") String priorityStr,
            @Param("readStatus") Boolean readStatus,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId AND (n.deleted = false OR n.deleted IS NULL) AND (:cursorId IS NULL OR n.id < :cursorId) ORDER BY n.id DESC")
    List<Notification> findUserNotificationsCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE (n.category = 'EMERGENCY' OR n.notificationType = 'EMERGENCY_BLOOD_REQUEST') AND n.status <> 'EXPIRED' AND n.createdAt <= :cutoffTime")
    List<Notification> findExpiredEmergencyNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<Notification> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);

    List<Notification> findByHospitalUserIdOrderByCreatedAtDesc(Long recipientUserId);

    List<Notification> findByHospitalIdAndReadStatusFalseOrderByCreatedAtDesc(Long hospitalId);

    List<Notification> findByHospitalUserIdAndReadStatusFalseOrderByCreatedAtDesc(Long recipientUserId);

    long countByHospitalIdAndReadStatusFalse(Long hospitalId);

    long countByHospitalUserIdAndReadStatusFalse(Long recipientUserId);

    List<Notification> findByRecipientUserIdAndReadStatusOrderByCreatedAtDesc(Long recipientUserId, Boolean readStatus);

    long countByRecipientUserIdAndReadStatus(Long recipientUserId, Boolean readStatus);

    List<Notification> findByStatusAndNextRetryTimeBefore(NotificationStatus status, LocalDateTime time);

    long countByStatus(NotificationStatus status);

    long countByReadStatus(Boolean readStatus);

    long countByDeliveryChannelAndCreatedAtAfter(com.bloodbridge.enums.DeliveryChannel deliveryChannel, LocalDateTime after);

    long countByDeliveryChannelAndStatus(com.bloodbridge.enums.DeliveryChannel deliveryChannel, NotificationStatus status);

    long countByDeliveryChannelAndNotificationType(com.bloodbridge.enums.DeliveryChannel deliveryChannel, com.bloodbridge.enums.NotificationType notificationType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.recipientUser.id = :userId")
    void deleteAllByRecipientUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.donor = null WHERE n.donor.id = :donorId")
    void unlinkDonorProfile(@Param("donorId") Long donorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.hospital = null WHERE n.hospital.id = :hospitalId")
    void unlinkHospitalProfile(@Param("hospitalId") Long hospitalId);
}
