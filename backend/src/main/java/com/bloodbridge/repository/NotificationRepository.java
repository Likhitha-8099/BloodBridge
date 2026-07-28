package com.bloodbridge.repository;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Notification} entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds notifications sent to a specific user.
     *
     * @param recipientUserId the recipient user's ID
     * @return a list of notifications
     */
    List<Notification> findByRecipientUserId(Long recipientUserId);

    /**
     * Finds notifications sent to a specific user with a specific read status.
     *
     * @param recipientUserId the recipient user's ID
     * @param readStatus      the read status (true for read, false for unread)
     * @return a list of notifications
     */
    List<Notification> findByRecipientUserIdAndReadStatus(Long recipientUserId, Boolean readStatus);

    /**
     * Finds notifications with a specific read status across the system.
     *
     * @param readStatus the read status
     * @return a list of notifications
     */
    List<Notification> findByReadStatus(Boolean readStatus);

    /**
     * Finds notifications with a specific delivery status.
     *
     * @param status the delivery status
     * @return a list of notifications
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Counts unread/read notifications for a specific user.
     *
     * @param recipientUserId the recipient user's ID
     * @param readStatus      the read status
     * @return the count of notifications
     */
    long countByRecipientUserIdAndReadStatus(Long recipientUserId, Boolean readStatus);

    /**
     * Counts notifications across the system with a specific read status.
     *
     * @param readStatus the read status
     * @return the count of notifications
     */
    long countByReadStatus(Boolean readStatus);

    /**
     * Counts notifications across the system with a specific delivery status.
     *
     * @param status the delivery status
     * @return the count of notifications
     */
    long countByStatus(NotificationStatus status);
}
