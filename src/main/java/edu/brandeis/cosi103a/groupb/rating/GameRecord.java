package edu.brandeis.cosi103a.groupb.rating;

import java.util.List;

/**
 * Per-game output produced by the rating harness.
 * This is intentionally metric-agnostic so a separate module can compute rankings.
 */
public record GameRecord(
    int matchupNumber,
    int gameNumber,
    List<String> matchupPlayers,
    List<PlayerRecord> playerResults
) {

    public record PlayerRecord(
        String playerName,
        int score,
        int rank,
        boolean winner,
        int money,
        int actionCardCount
    ) {}
}
