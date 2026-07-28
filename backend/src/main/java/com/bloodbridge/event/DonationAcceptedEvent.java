package com.bloodbridge.event;

import com.bloodbridge.entity.Donation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a donor accepts a matching donation request.
 */
@Getter
public class DonationAcceptedEvent extends ApplicationEvent {

    private final Donation donation;

    /**
     * Constructs a new DonationAcceptedEvent.
     *
     * @param source   the publishing source
     * @param donation the donation entity
     */
    public DonationAcceptedEvent(Object source, Donation donation) {
        super(source);
        this.donation = donation;
    }
}
