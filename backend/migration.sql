-- ==============================================================================
-- Migration Script: Phase 3C Enterprise Notification Center & User Preference Engine
-- ==============================================================================

-- Step 1: Safely backfill any existing NULL email values in donor_profiles from associated users
UPDATE donor_profiles dp
JOIN users u ON dp.user_id = u.id
SET dp.email = u.email
WHERE dp.email IS NULL OR dp.email = '';

UPDATE donor_profiles
SET email = CONCAT('donor_', id, '@bloodbridge.org')
WHERE email IS NULL OR email = '';

ALTER TABLE donor_profiles MODIFY COLUMN email VARCHAR(255) NOT NULL;
ALTER TABLE donor_profiles ADD CONSTRAINT uk_donor_profiles_email UNIQUE (email);

-- Step 2: Phase 3C Notifications schema updates
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS category VARCHAR(30);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS priority_enum VARCHAR(20);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS expiry_time DATETIME;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS metadata_json TEXT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_notif_user_unread ON notifications(recipient_user_id, read_status, deleted);
CREATE INDEX IF NOT EXISTS idx_notif_user_created ON notifications(recipient_user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notif_category ON notifications(category);
CREATE INDEX IF NOT EXISTS idx_notif_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_notif_user_cat_prio ON notifications(recipient_user_id, category, priority);

-- Step 3: Notification Preferences table creation
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    web_socket_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    emergency_alerts_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reward_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    admin_messages_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start TIME NULL,
    quiet_hours_end TIME NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    CONSTRAINT fk_notif_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pref_user_id ON notification_preferences(user_id);
