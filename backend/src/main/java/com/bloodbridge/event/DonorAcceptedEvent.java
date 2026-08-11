package com.bloodbridge.event;

import com.bloodbridge.entity.EmergencyResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event fired when a donor accepts an emergency blood request.
 */
@Getter
public class DonorAcceptedEvent extends ApplicationEvent {

    private final EmergencyResponse response;

    public DonorAcceptedEvent(Object source, EmergencyResponse response) {
        super(source);
        this.response = response;
    }
}
