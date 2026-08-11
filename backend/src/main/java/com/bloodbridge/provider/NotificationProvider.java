package com.bloodbridge.provider;

import com.bloodbridge.entity.Notification;
import com.bloodbridge.enums.DeliveryChannel;

/**
 * Strategy interface for pluggable Notification Providers.
 */
public interface NotificationProvider {

    /**
     * Checks if this provider handles the given delivery channel.
     *
     * @param channel target delivery channel
     * @return true if supported, false otherwise
     */
    boolean supports(DeliveryChannel channel);

    /**
     * Dispatches notification via provider strategy.
     *
     * @param notification notification entity to dispatch
     */
    void send(Notification notification);
}
