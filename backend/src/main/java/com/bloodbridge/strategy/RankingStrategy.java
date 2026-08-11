package com.bloodbridge.strategy;

import com.bloodbridge.entity.MatchResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy component for sorting and ranking match results.
 */
@Component
public class RankingStrategy {

    private static final int DEFAULT_TOP_N = 20;

    public List<MatchResult> rankAndSelectTopN(List<MatchResult> candidates, Integer limit) {
        int topN = (limit != null && limit > 0) ? limit : DEFAULT_TOP_N;

        List<MatchResult> sorted = candidates.stream()
                .sorted(Comparator.comparing(MatchResult::getMatchScore, Comparator.reverseOrder())
                        .thenComparing(MatchResult::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MatchResult::getDonorScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(topN)
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setRank(i + 1);
        }

        return sorted;
    }
}
