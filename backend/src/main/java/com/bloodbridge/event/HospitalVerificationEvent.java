package com.bloodbridge.event;

import com.bloodbridge.dto.response.HospitalResponse;
import com.bloodbridge.entity.Hospital;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a hospital registration is verified or rejected by an administrator.
 */
@Getter
public class HospitalVerificationEvent extends ApplicationEvent {

    private final Hospital hospital;
    private final String status;
    private final String remarks;
    private final String adminEmail;
    private final boolean approved;
    private final HospitalResponse response;

    public HospitalVerificationEvent(
            Object source,
            Hospital hospital,
            String status,
            String remarks,
            String adminEmail,
            boolean approved,
            HospitalResponse response
    ) {
        super(source);
        this.hospital = hospital;
        this.status = status;
        this.remarks = remarks;
        this.adminEmail = adminEmail;
        this.approved = approved;
        this.response = response;
    }
}
