package com.bloodbridge.event;

import com.bloodbridge.entity.BloodRequest;
import com.bloodbridge.entity.DonorProfile;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Spring Application Event fired when an Emergency Blood Request is created
 * and matched with eligible nearby donors.
 */
@Getter
public class EmergencyCreatedEvent extends ApplicationEvent {

    private final BloodRequest bloodRequest;
    private final List<DonorProfile> matchedDonors;

    public EmergencyCreatedEvent(Object source, BloodRequest bloodRequest, List<DonorProfile> matchedDonors) {
        super(source);
        this.bloodRequest = bloodRequest;
        this.matchedDonors = matchedDonors;
    }
}
