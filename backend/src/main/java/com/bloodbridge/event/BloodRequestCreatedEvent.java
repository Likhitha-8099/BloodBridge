package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new blood request is created.
 */
@Getter
public class BloodRequestCreatedEvent extends ApplicationEvent {

    private final BloodRequest bloodRequest;

    /**
     * Constructs a new BloodRequestCreatedEvent.
     *
     * @param source       the publishing source
     * @param bloodRequest the created blood request entity
     */
    public BloodRequestCreatedEvent(Object source, BloodRequest bloodRequest) {
        super(source);
        this.bloodRequest = bloodRequest;
    }
}
