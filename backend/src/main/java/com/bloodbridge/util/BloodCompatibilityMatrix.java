package com.bloodbridge.util;

import com.bloodbridge.enums.BloodGroup;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Enterprise utility providing strict medical blood compatibility mapping (Stage 2).
 * Medical Blood Compatibility Rule: Never notify medically incompatible donors.
 */
public class BloodCompatibilityMatrix {

    private static final Map<BloodGroup, List<BloodGroup>> COMPATIBILITY_MAP = Map.of(
            BloodGroup.O_NEGATIVE, List.of(BloodGroup.O_NEGATIVE),
            BloodGroup.O_POSITIVE, List.of(BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE),
            BloodGroup.A_POSITIVE, List.of(BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE),
            BloodGroup.A_NEGATIVE, List.of(BloodGroup.A_NEGATIVE, BloodGroup.O_NEGATIVE),
            BloodGroup.B_POSITIVE, List.of(BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE),
            BloodGroup.B_NEGATIVE, List.of(BloodGroup.B_NEGATIVE, BloodGroup.O_NEGATIVE),
            BloodGroup.AB_POSITIVE, List.of(
                    BloodGroup.AB_POSITIVE, BloodGroup.AB_NEGATIVE,
                    BloodGroup.A_POSITIVE, BloodGroup.A_NEGATIVE,
                    BloodGroup.B_POSITIVE, BloodGroup.B_NEGATIVE,
                    BloodGroup.O_POSITIVE, BloodGroup.O_NEGATIVE
            ),
            BloodGroup.AB_NEGATIVE, List.of(BloodGroup.AB_NEGATIVE, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE, BloodGroup.O_NEGATIVE)
    );

    /**
     * Retrieves the exact list of medically compatible donor blood groups for a required recipient blood group.
     *
     * @param recipientGroup the recipient's required blood group
     * @return unmodifiable list of compatible donor blood groups
     */
    public static List<BloodGroup> getCompatibleDonorGroups(BloodGroup recipientGroup) {
        if (recipientGroup == null) return Collections.emptyList();
        return COMPATIBILITY_MAP.getOrDefault(recipientGroup, Collections.emptyList());
    }

    /**
     * Determines whether a donor's blood group is medically compatible with the recipient's needed blood group.
     *
     * @param donorGroup donor blood group
     * @param recipientGroup recipient needed blood group
     * @return true if medically compatible, false otherwise
     */
    public static boolean isCompatible(BloodGroup donorGroup, BloodGroup recipientGroup) {
        if (donorGroup == null || recipientGroup == null) return false;
        List<BloodGroup> compatibleList = COMPATIBILITY_MAP.get(recipientGroup);
        return compatibleList != null && compatibleList.contains(donorGroup);
    }
}
