package com.bloodbridge.event;

import com.bloodbridge.entity.EmergencyResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring Application Event fired when a donor rejects an emergency blood request.
 */
@Getter
public class DonorRejectedEvent extends ApplicationEvent {

    private final EmergencyResponse response;

    public DonorRejectedEvent(Object source, EmergencyResponse response) {
        super(source);
        this.response = response;
    }
}
