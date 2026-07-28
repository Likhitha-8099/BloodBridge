package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a blood request is rejected by a hospital.
 */
@Getter
public class RequestRejectedEvent extends ApplicationEvent {

    private final BloodRequest bloodRequest;

    /**
     * Constructs a new RequestRejectedEvent.
     *
     * @param source       the publishing source
     * @param bloodRequest the rejected blood request entity
     */
    public RequestRejectedEvent(Object source, BloodRequest bloodRequest) {
        super(source);
        this.bloodRequest = bloodRequest;
    }
}
