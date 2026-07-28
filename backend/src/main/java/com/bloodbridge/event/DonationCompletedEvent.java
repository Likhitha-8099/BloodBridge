package com.bloodbridge.event;

import com.bloodbridge.entity.Donation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a donation is completed.
 */
@Getter
public class DonationCompletedEvent extends ApplicationEvent {

    private final Donation donation;

    /**
     * Constructs a new DonationCompletedEvent.
     *
     * @param source   the publishing source
     * @param donation the donation entity
     */
    public DonationCompletedEvent(Object source, Donation donation) {
        super(source);
        this.donation = donation;
    }
}
