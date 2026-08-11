package com.bloodbridge.repository;

import com.bloodbridge.entity.DeviceToken;
import com.bloodbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link DeviceToken} — Firebase device token management.
 * Phase 3B.1 — Device Registration module.
 */
@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * Find a device token record by the raw FCM token string.
     * Used for upsert: check existence before deciding insert vs update.
     */
    Optional<DeviceToken> findByFcmToken(String fcmToken);

    /**
     * Find a token by its FCM token string AND owning user.
     * Ownership validation before any mutation.
     */
    Optional<DeviceToken> findByFcmTokenAndUser(String fcmToken, User user);

    /**
     * All device tokens registered by a user (active + inactive).
     */
    List<DeviceToken> findAllByUser(User user);

    /**
     * Only the currently active tokens for a user — used for push targeting.
     */
    List<DeviceToken> findAllByUserAndIsActiveTrue(User user);

    /**
     * Hard-delete a token row by FCM token string.
     * Used on explicit logout/revocation.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceToken dt WHERE dt.fcmToken = :fcmToken")
    void deleteByFcmToken(@Param("fcmToken") String fcmToken);

    /**
     * Soft-deactivate all tokens for a user (e.g. account deactivation).
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.user = :user")
    void deactivateAllByUser(@Param("user") User user);

    /**
     * Update lastSeen timestamp for a specific token — used as heartbeat.
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.lastSeen = :now WHERE dt.fcmToken = :fcmToken")
    void updateLastSeen(@Param("fcmToken") String fcmToken, @Param("now") LocalDateTime now);

    /**
     * Replace old FCM token with a new one (Firebase token rotation).
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken dt SET dt.fcmToken = :newToken, dt.lastSeen = :now " +
           "WHERE dt.fcmToken = :oldToken AND dt.user = :user")
    int replaceToken(@Param("oldToken") String oldToken,
                     @Param("newToken") String newToken,
                     @Param("user") User user,
                     @Param("now") LocalDateTime now);

    /** Existence check — avoids fetching the full entity just to check presence. */
    boolean existsByFcmToken(String fcmToken);
}
