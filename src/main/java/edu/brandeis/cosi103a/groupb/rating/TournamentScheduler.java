package edu.brandeis.cosi103a.groupb.rating;

import java.util.ArrayList;
import java.util.List;

/**
 * Schedules matchups using round-robin pods.
 *
 * Strategy:
 * - Each game uses at most 4 players because the existing Engine supports up to 4.
 * - If 2-4 players are selected, one pod is scheduled.
 * - If more than 4 players are selected, schedule all unique 4-player combinations.
 */
public final class TournamentScheduler {

    private static final int ENGINE_MAX_PLAYERS = 4;

    public List<List<SelectedPlayer>> schedule(List<SelectedPlayer> selected) {
        if (selected == null || selected.size() < 2) {
            throw new IllegalArgumentException("At least 2 selected players are required");
        }

        int podSize = Math.min(ENGINE_MAX_PLAYERS, selected.size());
        List<List<SelectedPlayer>> result = new ArrayList<>();
        combinations(selected, podSize, 0, new ArrayList<>(), result);
        return result;
    }

    private void combinations(
        List<SelectedPlayer> input,
        int targetSize,
        int index,
        List<SelectedPlayer> current,
        List<List<SelectedPlayer>> out
    ) {
        if (current.size() == targetSize) {
            out.add(List.copyOf(current));
            return;
        }

        int remainingNeeded = targetSize - current.size();
        for (int i = index; i <= input.size() - remainingNeeded; i++) {
            current.add(input.get(i));
            combinations(input, targetSize, i + 1, current, out);
            current.remove(current.size() - 1);
        }
    }
}
