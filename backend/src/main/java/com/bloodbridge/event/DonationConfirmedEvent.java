package com.bloodbridge.event;

import com.bloodbridge.entity.Donation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a hospital confirms a donor.
 */
@Getter
public class DonationConfirmedEvent extends ApplicationEvent {

    private final Donation donation;

    /**
     * Constructs a new DonationConfirmedEvent.
     *
     * @param source   the publishing source
     * @param donation the donation entity
     */
    public DonationConfirmedEvent(Object source, Donation donation) {
        super(source);
        this.donation = donation;
    }
}
