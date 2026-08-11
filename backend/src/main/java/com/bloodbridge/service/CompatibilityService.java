package com.bloodbridge.service;

import com.bloodbridge.enums.BloodGroup;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service managing blood compatibility rules.
 */
@Service
public class CompatibilityService {

    /**
     * Retrieves the list of compatible donor blood groups for a given recipient blood group.
     *
     * @param recipientGroup the recipient's blood group
     * @return list of compatible donor blood groups
     */
    public List<BloodGroup> getCompatibleBloodGroups(BloodGroup recipientGroup) {
        if (recipientGroup == null) {
            return Collections.emptyList();
        }

        return switch (recipientGroup) {
            case O_NEGATIVE -> List.of(BloodGroup.O_NEGATIVE);
            case O_POSITIVE -> List.of(BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
            case A_NEGATIVE -> List.of(BloodGroup.A_NEGATIVE, BloodGroup.O_NEGATIVE);
            case A_POSITIVE -> List.of(BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
            case B_NEGATIVE -> List.of(BloodGroup.B_NEGATIVE, BloodGroup.O_NEGATIVE);
            case B_POSITIVE -> List.of(BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE);
            case AB_NEGATIVE -> List.of(BloodGroup.AB_NEGATIVE, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_NEGATIVE);
            case AB_POSITIVE -> List.of(
                    BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE,
                    BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE,
                    BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE,
                    BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE
            );
        };
    }

    /**
     * Retrieves the list of compatible recipient blood groups that a donor with donorGroup can donate to.
     *
     * @param donorGroup the donor's blood group
     * @return list of compatible recipient blood groups
     */
    public List<BloodGroup> getRecipientBloodGroupsForDonor(BloodGroup donorGroup) {
        if (donorGroup == null) {
            return Collections.emptyList();
        }

        return switch (donorGroup) {
            case O_NEGATIVE -> List.of(
                    BloodGroup.O_NEGATIVE, BloodGroup.O_POSITIVE,
                    BloodGroup.A_NEGATIVE, BloodGroup.A_POSITIVE,
                    BloodGroup.B_NEGATIVE, BloodGroup.B_POSITIVE,
                    BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE
            );
            case O_POSITIVE -> List.of(BloodGroup.O_POSITIVE, BloodGroup.A_POSITIVE, BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE);
            case A_NEGATIVE -> List.of(BloodGroup.A_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE);
            case A_POSITIVE -> List.of(BloodGroup.A_POSITIVE, BloodGroup.AB_POSITIVE);
            case B_NEGATIVE -> List.of(BloodGroup.B_NEGATIVE, BloodGroup.B_POSITIVE, BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE);
            case B_POSITIVE -> List.of(BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE);
            case AB_NEGATIVE -> List.of(BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE);
            case AB_POSITIVE -> List.of(BloodGroup.AB_POSITIVE);
        };
    }

    /**
     * Checks if a donor's blood group is compatible with a recipient's blood group.
     *
     * @param donorGroup     the donor's blood group
     * @param recipientGroup the recipient's blood group
     * @return true if compatible, false otherwise
     */
    public boolean isCompatible(BloodGroup donorGroup, BloodGroup recipientGroup) {
        if (donorGroup == null || recipientGroup == null) {
            return false;
        }
        return getCompatibleBloodGroups(recipientGroup).contains(donorGroup);
    }
}
