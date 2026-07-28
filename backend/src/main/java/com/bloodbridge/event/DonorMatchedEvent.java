package com.bloodbridge.event;

import com.bloodbridge.entity.MatchResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when matching donors are found and registered for a verified request.
 */
@Getter
public class DonorMatchedEvent extends ApplicationEvent {

    private final MatchResult matchResult;

    /**
     * Constructs a new DonorMatchedEvent.
     *
     * @param source      the publishing source
     * @param matchResult the match result entity
     */
    public DonorMatchedEvent(Object source, MatchResult matchResult) {
        super(source);
        this.matchResult = matchResult;
    }
}
