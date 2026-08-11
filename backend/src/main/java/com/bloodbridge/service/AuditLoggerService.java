package com.bloodbridge.service;

/**
 * Audit Logger Service interface.
 * Extension point for future audit persistence into database or Kafka/Elasticsearch.
 */
public interface AuditLoggerService {

    /**
     * Logs an audit event.
     *
     * @param action Audit action description (PROFILE_UPDATED, PASSWORD_CHANGED, ACCOUNT_DEACTIVATED, etc.)
     * @param email User email associated with the event
     * @param details Additional context details
     */
    void logEvent(String action, String email, String details);
}
