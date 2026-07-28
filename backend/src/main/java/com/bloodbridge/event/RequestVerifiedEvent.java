package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a blood request is verified by a hospital.
 */
@Getter
public class RequestVerifiedEvent extends ApplicationEvent {

    private final BloodRequest bloodRequest;

    /**
     * Constructs a new RequestVerifiedEvent.
     *
     * @param source       the publishing source
     * @param bloodRequest the verified blood request entity
     */
    public RequestVerifiedEvent(Object source, BloodRequest bloodRequest) {
        super(source);
        this.bloodRequest = bloodRequest;
    }
}
