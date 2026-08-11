package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event fired when an emergency blood request requirement is fully met
 * and automatically completed.
 */
@Getter
public class EmergencyCompletedEvent extends ApplicationEvent {

    private final BloodRequest bloodRequest;

    public EmergencyCompletedEvent(Object source, BloodRequest bloodRequest) {
        super(source);
        this.bloodRequest = bloodRequest;
    }
}
