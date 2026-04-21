package edu.brandeis.cosi103a.groupb.rating;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.StrategyPlayer;
import edu.brandeis.cosi103a.groupb.V2StrategyPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer2;
import edu.brandeis.cosi103a.groupb.WeightedPlayer3;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Milestone 3 Story 1 harness.
 *
 * Responsibilities covered in this class:
 * - Select which automated players to include
 * - Support selecting more than 4 players
 * - Orchestrate matchup scheduling and game simulation
 * - Collect raw per-game results
 */
public class PlayerRatingHarness {

    private static final int MIN_SELECTED_PLAYERS = 2;
    private static final int DEFAULT_GAMES_PER_MATCHUP = 50;

    public static void main(String[] args) throws PlayerViolationException {
        try (Scanner scanner = new Scanner(System.in)) {
            List<Template> templates = defaultTemplates();
            List<SelectedPlayer> selected = selectPlayers(scanner, System.out, templates);
            int gamesPerMatchup = readPositiveInt(
                scanner,
                System.out,
                "How many games per matchup pod? (recommended 50+)",
                DEFAULT_GAMES_PER_MATCHUP
            );

            PlayerRatingHarness harness = new PlayerRatingHarness();
            List<GameRecord> rawResults = harness.runTournament(selected, gamesPerMatchup);
            printRawSummary(rawResults, System.out);
            List<PlayerPerformance> ratings = harness.computePerformanceRatings(rawResults);
            printPerformanceSummary(ratings, System.out);
        }
    }

    public List<GameRecord> runTournament(List<SelectedPlayer> selected, int gamesPerMatchup)
        throws PlayerViolationException {

        if (selected == null || selected.size() < MIN_SELECTED_PLAYERS) {
            throw new IllegalArgumentException("Select at least 2 players");
        }
        if (gamesPerMatchup <= 0) {
            throw new IllegalArgumentException("gamesPerMatchup must be > 0");
        }

        TournamentScheduler scheduler = new TournamentScheduler();
        List<List<SelectedPlayer>> matchups = scheduler.schedule(selected);
        List<GameRecord> allResults = new ArrayList<>();

        int matchupNumber = 1;
        for (List<SelectedPlayer> matchup : matchups) {
            for (int gameNumber = 1; gameNumber <= gamesPerMatchup; gameNumber++) {
                allResults.add(runSingleGame(matchupNumber, gameNumber, matchup));
            }
            matchupNumber++;
        }

        return allResults;
    }

    private GameRecord runSingleGame(int matchupNumber, int gameNumber, List<SelectedPlayer> matchup)
        throws PlayerViolationException {

        List<ParentPlayer> players = new ArrayList<>();
        for (SelectedPlayer selected : matchup) {
            players.add(selected.newInstance());
        }

        Engine engine = new Engine(players);
        GameResult gameResult = engine.play();

        List<GameRecord.PlayerRecord> rawPlayers = toPlayerRecords(gameResult.playerResults());
        List<String> matchupPlayers = new ArrayList<>();
        for (SelectedPlayer selected : matchup) {
            matchupPlayers.add(selected.slotLabel());
        }

        return new GameRecord(matchupNumber, gameNumber, matchupPlayers, rawPlayers);
    }

    private List<GameRecord.PlayerRecord> toPlayerRecords(List<PlayerResult> sortedResults) {
        List<GameRecord.PlayerRecord> raw = new ArrayList<>();
        if (sortedResults.isEmpty()) {
            return raw;
        }

        int currentRank = 0;
        Integer previousScore = null;
        for (int i = 0; i < sortedResults.size(); i++) {
            PlayerResult pr = sortedResults.get(i);
            if (previousScore == null || pr.score() < previousScore) {
                currentRank = i + 1;
            }
            boolean winner = currentRank == 1;
            raw.add(new GameRecord.PlayerRecord(pr.playerName(), pr.score(), currentRank, winner));
            previousScore = pr.score();
        }

        return raw;
    }

    public static List<SelectedPlayer> selectPlayers(Scanner scanner, PrintStream out, List<Template> templates) {
        out.println("=== Player Rating Harness ===");
        out.println("Available automated player templates:");
        for (int i = 0; i < templates.size(); i++) {
            Template t = templates.get(i);
            out.println("[" + (i + 1) + "] " + t.name() + " - " + t.description());
        }

        int slots = readPositiveInt(scanner, out, "How many player slots to include? (2+)", 4);
        while (slots < MIN_SELECTED_PLAYERS) {
            out.println("Please choose at least 2 slots.");
            slots = readPositiveInt(scanner, out, "How many player slots to include? (2+)", 4);
        }

        Map<String, Integer> templateCounter = new HashMap<>();
        List<SelectedPlayer> selected = new ArrayList<>();
        for (int slot = 1; slot <= slots; slot++) {
            int choice = readBoundedInt(
                scanner,
                out,
                "Select template for slot " + slot + " (1-" + templates.size() + ")",
                1,
                templates.size()
            );
            Template template = templates.get(choice - 1);
            int count = templateCounter.getOrDefault(template.name(), 0) + 1;
            templateCounter.put(template.name(), count);
            String label = template.name() + "#" + count;
            selected.add(new SelectedPlayer(label, template.name(), template.factory()));
        }

        out.println("Selected slots:");
        for (SelectedPlayer player : selected) {
            out.println("- " + player.slotLabel() + " (" + player.templateName() + ")");
        }

        return selected;
    }

    private static int readPositiveInt(Scanner scanner, PrintStream out, String prompt, int defaultValue) {
        while (true) {
            out.print(prompt + " [default=" + defaultValue + "]: ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Keep looping until valid input.
            }
            out.println("Please enter a positive integer.");
        }
    }

    private static int readBoundedInt(Scanner scanner, PrintStream out, String prompt, int min, int max) {
        while (true) {
            out.print(prompt + ": ");
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Keep looping until valid input.
            }
            out.println("Please enter an integer from " + min + " to " + max + ".");
        }
    }

    private static void printRawSummary(List<GameRecord> rawResults, PrintStream out) {
        out.println();
        out.println("=== Raw Tournament Summary ===");
        out.println("Games simulated: " + rawResults.size());

        // Additional context for the new metrics
        out.println("=== Additional Metrics ===");

        Map<String, Integer> wins = new LinkedHashMap<>();
        Map<String, Integer> moneyTotals = new LinkedHashMap<>();
        Map<String, Integer> actionCardTotals = new LinkedHashMap<>();
        Map<String, Integer> gameCounts = new LinkedHashMap<>();
        
        for (GameRecord game : rawResults) {
            for (GameRecord.PlayerRecord playerResult : game.playerResults()) {
                String name = playerResult.playerName();
                wins.putIfAbsent(name, 0);
                moneyTotals.putIfAbsent(name, 0);
                actionCardTotals.putIfAbsent(name, 0);
                gameCounts.putIfAbsent(name, 0);
                
                if (playerResult.winner()) {
                    wins.put(name, wins.get(name) + 1);
                }
                gameCounts.put(name, gameCounts.get(name) + 1);
            }
        }

        out.println("Raw win counts (ties count as win for each top-ranked player):");
        List<Map.Entry<String, Integer>> sortedWins = wins.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .collect(Collectors.toList());
        for (Map.Entry<String, Integer> entry : sortedWins) {
            out.println("- " + entry.getKey() + ": " + entry.getValue());
        }
        
        out.println();
        out.println("=== Average Metrics Per Game ===");
        for (Map.Entry<String, Integer> entry : sortedWins) {
            String playerName = entry.getKey();
            int gameCount = gameCounts.get(playerName);
            double avgMoney = gameCount > 0 ? (double) moneyTotals.get(playerName) / gameCount : 0;
            double avgActionCards = gameCount > 0 ? (double) actionCardTotals.get(playerName) / gameCount : 0;
            out.printf("- %s: %.1f money, %.1f action cards%n", playerName, avgMoney, avgActionCards);
        }
        out.println();
    }

    /**
     * Computes advanced rating metrics using only raw GameRecord data.
     *
     * Notes:
     * - This method does not change simulation behavior.
     * - Ties are handled by rank values as already encoded in GameRecord.
     * - Head-to-head records compare each player against every opponent per game.
     */
    public List<PlayerPerformance> computePerformanceRatings(List<GameRecord> rawResults) {
        if (rawResults == null || rawResults.isEmpty()) {
            return List.of();
        }

        Map<String, MutablePlayerStats> statsByPlayer = new LinkedHashMap<>();
        int maxPlayersInAnyGame = 0;

        // Pass 1: Aggregate all per-player totals directly from raw game records.
        for (GameRecord game : rawResults) {
            List<GameRecord.PlayerRecord> results = game.playerResults();
            if (results == null || results.isEmpty()) {
                continue;
            }

            int gameSize = results.size();
            if (gameSize > maxPlayersInAnyGame) {
                maxPlayersInAnyGame = gameSize;
            }

            int worstRank = 1;
            for (GameRecord.PlayerRecord playerRecord : results) {
                if (playerRecord.rank() > worstRank) {
                    worstRank = playerRecord.rank();
                }
            }

            for (GameRecord.PlayerRecord playerRecord : results) {
                MutablePlayerStats stats = statsByPlayer.computeIfAbsent(
                    playerRecord.playerName(),
                    ignored -> new MutablePlayerStats(playerRecord.playerName())
                );

                stats.gamesPlayed++;
                stats.ranks.add(playerRecord.rank());
                stats.scoreSum += playerRecord.score();

                if (playerRecord.rank() == 1) {
                    stats.totalWins++;
                    stats.firstPlaceCount++;
                }
                if (playerRecord.rank() <= 2) {
                    stats.topTwoCount++;
                }
                if (playerRecord.rank() == worstRank) {
                    stats.lastPlaceCount++;
                }

                // Matchup key is the sorted list of opponents in the same game.
                String matchupKey = buildMatchupKey(game.matchupPlayers(), playerRecord.playerName());
                MutableMatchupStats matchupStats = stats.matchupStats.computeIfAbsent(
                    matchupKey,
                    ignored -> new MutableMatchupStats()
                );
                matchupStats.games++;
                if (playerRecord.rank() == 1) {
                    matchupStats.wins++;
                }
            }

            int topRankCount = 0;
            for (GameRecord.PlayerRecord playerRecord : results) {
                if (playerRecord.rank() == 1) {
                    topRankCount++;
                }
            }
            boolean sharedWin = topRankCount > 1;
            for (GameRecord.PlayerRecord playerRecord : results) {
                if (playerRecord.rank() == 1) {
                    MutablePlayerStats stats = statsByPlayer.get(playerRecord.playerName());
                    if (sharedWin) {
                        stats.sharedWins++;
                    } else {
                        stats.outrightWins++;
                    }
                }
            }

            // Pairwise head-to-head records: compare every player against every other player in the game.
            for (int i = 0; i < results.size(); i++) {
                GameRecord.PlayerRecord left = results.get(i);
                MutablePlayerStats leftStats = statsByPlayer.get(left.playerName());
                for (int j = i + 1; j < results.size(); j++) {
                    GameRecord.PlayerRecord right = results.get(j);
                    MutablePlayerStats rightStats = statsByPlayer.get(right.playerName());

                    MutableHeadToHeadStats leftVsRight = leftStats.headToHeadStats.computeIfAbsent(
                        right.playerName(),
                        ignored -> new MutableHeadToHeadStats()
                    );
                    MutableHeadToHeadStats rightVsLeft = rightStats.headToHeadStats.computeIfAbsent(
                        left.playerName(),
                        ignored -> new MutableHeadToHeadStats()
                    );

                    if (left.rank() < right.rank()) {
                        leftVsRight.wins++;
                        rightVsLeft.losses++;
                    } else if (left.rank() > right.rank()) {
                        leftVsRight.losses++;
                        rightVsLeft.wins++;
                    } else {
                        leftVsRight.ties++;
                        rightVsLeft.ties++;
                    }
                }
            }
        }

        if (statsByPlayer.isEmpty()) {
            return List.of();
        }

        // Pass 2: Find score bounds for cross-player average score normalization.
        double minAverageScore = Double.MAX_VALUE;
        double maxAverageScore = -Double.MAX_VALUE;
        for (MutablePlayerStats stats : statsByPlayer.values()) {
            if (stats.gamesPlayed == 0) {
                continue;
            }
            double avgScore = stats.scoreSum / stats.gamesPlayed;
            if (avgScore < minAverageScore) {
                minAverageScore = avgScore;
            }
            if (avgScore > maxAverageScore) {
                maxAverageScore = avgScore;
            }
        }

        if (minAverageScore == Double.MAX_VALUE) {
            minAverageScore = 0.0;
            maxAverageScore = 0.0;
        }

        // Pass 3: Compute final metrics, normalized values, and weighted CPR.
        List<PlayerPerformance> performances = new ArrayList<>();
        for (MutablePlayerStats stats : statsByPlayer.values()) {
            if (stats.gamesPlayed == 0) {
                continue;
            }

            double games = stats.gamesPlayed;
            double totalWinRate = stats.totalWins / games;
            double outrightWinRate = stats.outrightWins / games;
            double sharedWinRate = stats.sharedWins / games;
            double topTwoRate = stats.topTwoCount / games;
            double medianRank = median(stats.ranks);
            double averageScore = stats.scoreSum / games;
            double lastPlaceRate = stats.lastPlaceCount / games;
            double firstPlaceRate = stats.firstPlaceCount / games;

            // Dominion-aware "variance" proxy: frequent extremes (first OR last) => riskier profile.
            double extremeRankRate = (stats.firstPlaceCount + stats.lastPlaceCount) / games;

            // Median rank normalization: rank 1 is best, so lower median rank should score higher.
            double normalizedMedianRank;
            if (maxPlayersInAnyGame <= 1) {
                normalizedMedianRank = 1.0;
            } else {
                normalizedMedianRank = 1.0 - ((medianRank - 1.0) / (maxPlayersInAnyGame - 1.0));
                normalizedMedianRank = clamp01(normalizedMedianRank);
            }

            // Average score normalization across all players.
            double normalizedAverageScore;
            if (maxAverageScore == minAverageScore) {
                normalizedAverageScore = 1.0;
            } else {
                normalizedAverageScore = (averageScore - minAverageScore) / (maxAverageScore - minAverageScore);
                normalizedAverageScore = clamp01(normalizedAverageScore);
            }

            double nonLastRate = 1.0 - lastPlaceRate;

            // CPR is the final weighted score used to rank players across all raw game records.
            // It blends winning, consistency, score quality, rank quality, and staying out of last place.
            double cpr =
                (0.35 * totalWinRate)
                + (0.25 * topTwoRate)
                + (0.20 * normalizedAverageScore)
                + (0.15 * normalizedMedianRank)
                + (0.05 * nonLastRate);

            Map<String, MatchupWinRate> matchupWinRates = new TreeMap<>();
            for (Map.Entry<String, MutableMatchupStats> entry : stats.matchupStats.entrySet()) {
                MutableMatchupStats ms = entry.getValue();
                double matchupRate = ms.games == 0 ? 0.0 : (double) ms.wins / ms.games;
                matchupWinRates.put(entry.getKey(), new MatchupWinRate(ms.games, ms.wins, matchupRate));
            }

            Map<String, HeadToHeadRecord> headToHeadRecords = new TreeMap<>();
            for (Map.Entry<String, MutableHeadToHeadStats> entry : stats.headToHeadStats.entrySet()) {
                MutableHeadToHeadStats h2h = entry.getValue();
                int totalMatchups = h2h.wins + h2h.losses + h2h.ties;
                double h2hWinRate = totalMatchups == 0 ? 0.0 : (double) h2h.wins / totalMatchups;
                headToHeadRecords.put(entry.getKey(), new HeadToHeadRecord(h2h.wins, h2h.losses, h2h.ties, h2hWinRate));
            }

            performances.add(new PlayerPerformance(
                stats.playerName,
                stats.gamesPlayed,
                stats.totalWins,
                stats.outrightWins,
                stats.sharedWins,
                totalWinRate,
                outrightWinRate,
                sharedWinRate,
                topTwoRate,
                medianRank,
                averageScore,
                lastPlaceRate,
                Collections.unmodifiableMap(matchupWinRates),
                Collections.unmodifiableMap(headToHeadRecords),
                firstPlaceRate,
                extremeRankRate,
                normalizedMedianRank,
                normalizedAverageScore,
                cpr
            ));
        }

        // Final ordering: CPR desc, then total win rate desc, then median rank asc, then average score desc.
        performances.sort(
            Comparator
                .comparingDouble(PlayerPerformance::compositePerformanceRating).reversed()
                .thenComparing(Comparator.comparingDouble(PlayerPerformance::totalWinRate).reversed())
                .thenComparingDouble(PlayerPerformance::medianRank)
                .thenComparing(Comparator.comparingDouble(PlayerPerformance::averageScore).reversed())
        );

        return performances;
    }

    private static String buildMatchupKey(List<String> matchupPlayers, String playerName) {
        if (matchupPlayers == null || matchupPlayers.isEmpty()) {
            return "(unknown matchup)";
        }

        List<String> opponents = new ArrayList<>();
        for (String slot : matchupPlayers) {
            if (!slot.equals(playerName)) {
                opponents.add(slot);
            }
        }

        if (opponents.isEmpty()) {
            return "(solo)";
        }

        opponents.sort(String::compareTo);
        return String.join(" | ", opponents);
    }

    private static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    static void printPerformanceSummary(List<PlayerPerformance> performances, PrintStream out) {
        out.println();
        out.println("=== Composite Player Rating Summary ===");
        if (performances == null || performances.isEmpty()) {
            out.println("No player metrics available.");
            return;
        }

        for (PlayerPerformance player : performances) {
            out.println();
            out.println("=== " + player.playerName() + " ===");

            out.println();
            out.println("[Volume]");
            printMetric(out, "Games Played", String.valueOf(player.gamesPlayed()));

            out.println();
            out.println("[Win Profile]");
            printMetric(out, "Total Wins", String.valueOf(player.totalWins()));
            printMetric(out, "Total Win Rate", formatPercent(player.totalWinRate()));
            printMetric(
                out,
                "Outright Wins",
                player.outrightWins() + " (" + formatPercent(player.outrightWinRate()) + ")"
            );
            printMetric(
                out,
                "Shared Wins",
                player.sharedWins() + " (" + formatPercent(player.sharedWinRate()) + ")"
            );

            out.println();
            out.println("[Placement Profile]");
            printMetric(out, "Top-Two Rate", formatPercent(player.topTwoRate()));
            printMetric(out, "Median Rank", formatDouble(player.medianRank(), 3));
            printMetric(out, "Average Score", formatDouble(player.averageScore(), 3));
            printMetric(out, "First Place Rate", formatPercent(player.firstPlaceRate()));
            printMetric(out, "Last Place Rate", formatPercent(player.lastPlaceRate()));

            out.println();
            out.println("[Direct Comparisons]");
            out.println("Head-to-Head Record:");
            if (player.headToHeadRecords().isEmpty()) {
                out.println("- (none)");
            } else {
                for (Map.Entry<String, HeadToHeadRecord> entry : player.headToHeadRecords().entrySet()) {
                    HeadToHeadRecord record = entry.getValue();
                    out.println(
                        "- vs [" + entry.getKey() + "]: "
                        + record.wins() + "-" + record.losses() + "-" + record.ties()
                        + " (win rate " + formatPercent(record.winRate()) + ")"
                    );
                }
            }

            out.println();
            out.println("Matchup Win Rate:");
            if (player.matchupWinRates().isEmpty()) {
                out.println("- (none)");
            } else {
                for (Map.Entry<String, MatchupWinRate> entry : player.matchupWinRates().entrySet()) {
                    MatchupWinRate rate = entry.getValue();
                    out.println(
                        "- vs [" + entry.getKey() + "]: "
                        + formatPercent(rate.winRate())
                        + " (" + rate.wins() + "/" + rate.games() + ")"
                    );
                }
            }

            out.println();
            out.println("[Composite Rating]");
            out.println();
            printMetric(out, "Composite Performance Rating (CPR)", formatDouble(player.compositePerformanceRating(), 4));
        }

        out.println();
        out.println("=== Interpretation Guide ===");
        printGuideLine(out, "High Total Win Rate", "strong ability to win");
        printGuideLine(out, "High Outright Win Rate", "wins without sharing first place");
        printGuideLine(out, "High Shared Win Rate", "often tied for first");
        printGuideLine(out, "High Top-Two Rate", "consistent strategy across games");
        printGuideLine(out, "Low Median Rank", "better finish position (Rank 1 is best)");
        printGuideLine(out, "High Average Score", "strong performance even in losses");
        printGuideLine(out, "Low Last Place Rate", "stable, lower-risk profile");
        printGuideLine(out, "Head-to-Head W-L-T", "Wins-Losses-Ties against one opponent across shared games");
        printGuideLine(out, "CPR scale", "0.0000 to 1.0000, where 1.0000 is the best possible score");
        printGuideLine(out, "CPR meaning", "weighted overall strength across wins, consistency, score, rank, and not-last finishes");
    }

    private static String formatPercent(double value) {
        return String.format(Locale.US, "%.2f%%", value * 100.0);
    }

    private static String formatDouble(double value, int decimals) {
        return String.format(Locale.US, "%1$." + decimals + "f", value);
    }

    private static void printMetric(PrintStream out, String label, String value) {
        out.println(String.format(Locale.US, "%-32s : %s", label, value));
    }

    private static void printGuideLine(PrintStream out, String metric, String meaning) {
        out.println(String.format(Locale.US, "%-26s -> %s", metric, meaning));
    }

    /**
     * Immutable final output for one player's computed rating profile.
     */
    public record PlayerPerformance(
        String playerName,
        int gamesPlayed,
        int totalWins,
        int outrightWins,
        int sharedWins,
        double totalWinRate,
        double outrightWinRate,
        double sharedWinRate,
        double topTwoRate,
        double medianRank,
        double averageScore,
        double lastPlaceRate,
        Map<String, MatchupWinRate> matchupWinRates,
        Map<String, HeadToHeadRecord> headToHeadRecords,
        double firstPlaceRate,
        double extremeRankRate,
        double normalizedMedianRank,
        double normalizedAverageScore,
        double compositePerformanceRating
    ) {}

    public record MatchupWinRate(int games, int wins, double winRate) {}

    public record HeadToHeadRecord(int wins, int losses, int ties, double winRate) {}

    /**
     * Mutable accumulator used only during aggregation.
     */
    private static final class MutablePlayerStats {
        private final String playerName;
        private int gamesPlayed;
        private int totalWins;
        private int outrightWins;
        private int sharedWins;
        private int topTwoCount;
        private int firstPlaceCount;
        private int lastPlaceCount;
        private double scoreSum;
        private final List<Integer> ranks;
        private final Map<String, MutableMatchupStats> matchupStats;
        private final Map<String, MutableHeadToHeadStats> headToHeadStats;

        private MutablePlayerStats(String playerName) {
            this.playerName = playerName;
            this.ranks = new ArrayList<>();
            this.matchupStats = new LinkedHashMap<>();
            this.headToHeadStats = new LinkedHashMap<>();
        }
    }

    private static final class MutableMatchupStats {
        private int games;
        private int wins;
    }

    private static final class MutableHeadToHeadStats {
        private int wins;
        private int losses;
        private int ties;
    }

    private static double median(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        List<Integer> sorted = new ArrayList<>(values);
        sorted.sort(Integer::compareTo);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }
        return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
    }

    private static List<Template> defaultTemplates() {
        List<Template> templates = new ArrayList<>();
        templates.add(new Template(
            "BigMoney",
            "Baseline money-focused strategy",
            BigMoneyPlayer::new
        ));
        templates.add(new Template(
            "Strategy",
            "Existing action-aware strategy player",
            StrategyPlayer::new
        ));
        templates.add(new Template(
            "Weighted-V1",
            "Category-based weights (original version)",
            WeightedPlayer::new
        ));
        templates.add(new Template(
            "Weighted-V2",
            "Individual card weights (new version)",
            WeightedPlayer2::new
        ));
        templates.add(new Template(
            "Weighted-V3",
            "Deck-aware weights (board-specific optimization)",
            WeightedPlayer3::new
        ));
        templates.add(new Template(
            "Strategy-V2", 
            "Version 2 of Strategy Player",
            V2StrategyPlayer::new
        ));

        return templates;
    }

    public record Template(
        String name,
        String description,
        Function<String, ParentPlayer> factory
    ) {}
}
