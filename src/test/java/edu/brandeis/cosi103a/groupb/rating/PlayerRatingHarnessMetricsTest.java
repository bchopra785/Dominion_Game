package edu.brandeis.cosi103a.groupb.rating;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerRatingHarnessMetricsTest {

    @Test
    public void computePerformanceRatings_withNullInput_returnsEmptyList() {
        PlayerRatingHarness harness = new PlayerRatingHarness();
        List<PlayerRatingHarness.PlayerPerformance> ratings = harness.computePerformanceRatings(null);
        assertTrue(ratings.isEmpty());
    }

    @Test
    public void computePerformanceRatings_withEmptyInput_returnsEmptyList() {
        PlayerRatingHarness harness = new PlayerRatingHarness();
        List<PlayerRatingHarness.PlayerPerformance> ratings = harness.computePerformanceRatings(List.of());
        assertTrue(ratings.isEmpty());
    }

    @Test
    public void computePerformanceRatings_computesMetricsAndOrdersByCpr() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("BigMoney#1", "Strategy#1", "Strategy#2"),
                List.of(
                    new GameRecord.PlayerRecord("BigMoney#1", 30, 1, true),
                    new GameRecord.PlayerRecord("Strategy#1", 25, 2, false),
                    new GameRecord.PlayerRecord("Strategy#2", 20, 3, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("BigMoney#1", "Strategy#1", "Strategy#2"),
                List.of(
                    new GameRecord.PlayerRecord("Strategy#1", 28, 1, true),
                    new GameRecord.PlayerRecord("BigMoney#1", 24, 2, false),
                    new GameRecord.PlayerRecord("Strategy#2", 18, 3, false)
                )
            )
        );

        List<PlayerRatingHarness.PlayerPerformance> ratings = harness.computePerformanceRatings(rawResults);

        assertEquals(3, ratings.size());

        // CPR sort should put BigMoney#1 ahead of Strategy#1 due to higher normalized average score.
        assertEquals("BigMoney#1", ratings.get(0).playerName());
        assertEquals("Strategy#1", ratings.get(1).playerName());
        assertEquals("Strategy#2", ratings.get(2).playerName());

        PlayerRatingHarness.PlayerPerformance top = ratings.get(0);
        assertEquals(2, top.gamesPlayed());
        assertEquals(1, top.totalWins());
        assertEquals(1, top.outrightWins());
        assertEquals(0, top.sharedWins());
        assertEquals(0.5, top.totalWinRate(), 1e-9);
        assertEquals(0.5, top.outrightWinRate(), 1e-9);
        assertEquals(0.0, top.sharedWinRate(), 1e-9);
        assertEquals(1.0, top.topTwoRate(), 1e-9);        assertEquals(1.5, top.medianRank(), 1e-9);
        assertEquals(27.0, top.averageScore(), 1e-9);
        assertEquals(0.0, top.lastPlaceRate(), 1e-9);
        assertEquals(0.75, top.normalizedMedianRank(), 1e-9);
        assertEquals(1.0, top.normalizedAverageScore(), 1e-9);
        assertEquals(0.7875, top.compositePerformanceRating(), 1e-9);

        PlayerRatingHarness.HeadToHeadRecord vsStrategy1 = top.headToHeadRecords().get("Strategy#1");
        assertEquals(1, vsStrategy1.wins());
        assertEquals(1, vsStrategy1.losses());
        assertEquals(0, vsStrategy1.ties());

        PlayerRatingHarness.MatchupWinRate matchupRate =
            top.matchupWinRates().get("Strategy#1 | Strategy#2");
        assertEquals(2, matchupRate.games());
        assertEquals(1, matchupRate.wins());
        assertEquals(0.5, matchupRate.winRate(), 1e-9);

        PlayerRatingHarness.PlayerPerformance bottom = ratings.get(2);
        assertEquals(1.0, bottom.lastPlaceRate(), 1e-9);
        assertEquals(0.0, bottom.compositePerformanceRating(), 1e-9);
    }

    @Test
    public void printPerformanceSummary_printsExpectedFieldsWithoutGameLength() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("BigMoney#1", "Strategy#1"),
                List.of(
                    new GameRecord.PlayerRecord("BigMoney#1", 32, 1, true),
                    new GameRecord.PlayerRecord("Strategy#1", 20, 2, false)
                )
            )
        );

        List<PlayerRatingHarness.PlayerPerformance> ratings = harness.computePerformanceRatings(rawResults);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PlayerRatingHarness.printPerformanceSummary(ratings, out);
        String printed = bytes.toString(StandardCharsets.UTF_8);

        assertTrue(printed.contains("=== Composite Player Rating Summary ==="));
        assertTrue(printed.contains("[Volume]"));
        assertTrue(printed.contains("[Win Profile]"));
        assertTrue(printed.contains("[Placement Profile]"));
        assertTrue(printed.contains("[Direct Comparisons]"));
        assertTrue(printed.contains("[Composite Rating]"));
        assertTrue(printed.contains("Games Played"));
        assertTrue(printed.contains("Total Wins"));
        assertTrue(printed.contains("Total Win Rate"));
        assertTrue(printed.contains("Outright Wins"));
        assertTrue(printed.contains("Shared Wins"));
        assertTrue(printed.contains("Top-Two Rate"));
        assertTrue(printed.contains("Median Rank"));
        assertTrue(printed.contains("Average Score"));
        assertTrue(printed.contains("First Place Rate"));
        assertTrue(printed.contains("Last Place Rate"));
        assertTrue(printed.contains("Head-to-Head Record:"));
        assertTrue(printed.contains("Matchup Win Rate:"));
        assertTrue(printed.contains("Composite Performance Rating (CPR)"));
        assertTrue(printed.contains("=== Interpretation Guide ==="));

        assertFalse(printed.contains("Game Length:"));
    }

    @Test
    public void computePerformanceRatings_handlesWinnerAndLastPlaceTiesCorrectly() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B", "C", "D"),
                List.of(
                    new GameRecord.PlayerRecord("A", 30, 1, true),
                    new GameRecord.PlayerRecord("B", 30, 1, true),
                    new GameRecord.PlayerRecord("C", 20, 3, false),
                    new GameRecord.PlayerRecord("D", 20, 3, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.PlayerPerformance a = byName.get("A");
        PlayerRatingHarness.PlayerPerformance b = byName.get("B");
        PlayerRatingHarness.PlayerPerformance c = byName.get("C");
        PlayerRatingHarness.PlayerPerformance d = byName.get("D");

        assertEquals(1.0, a.totalWinRate(), 1e-9);
        assertEquals(1.0, b.totalWinRate(), 1e-9);
        assertEquals(0.0, a.outrightWinRate(), 1e-9);
        assertEquals(1.0, a.sharedWinRate(), 1e-9);
        assertEquals(0.0, b.outrightWinRate(), 1e-9);
        assertEquals(1.0, b.sharedWinRate(), 1e-9);
        assertEquals(1.0, c.lastPlaceRate(), 1e-9);
        assertEquals(1.0, d.lastPlaceRate(), 1e-9);
        assertEquals(1.0, a.topTwoRate(), 1e-9);
        assertEquals(0.0, c.topTwoRate(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_whenAllAverageScoresEqual_normalizesScoresToOne() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 10, 2, false),
                    new GameRecord.PlayerRecord("B", 20, 1, true)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 20, 1, true),
                    new GameRecord.PlayerRecord("B", 10, 2, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        assertEquals(1.0, byName.get("A").normalizedAverageScore(), 1e-9);
        assertEquals(1.0, byName.get("B").normalizedAverageScore(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_usesRawScoresDirectly_evenWhenScoreIsLow() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("BigMoney#1", "Strategy#1"),
                List.of(
                    new GameRecord.PlayerRecord("BigMoney#1", 3, 2, false),
                    new GameRecord.PlayerRecord("Strategy#1", 20, 1, true)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("BigMoney#1", "Strategy#1"),
                List.of(
                    new GameRecord.PlayerRecord("BigMoney#1", 3, 2, false),
                    new GameRecord.PlayerRecord("Strategy#1", 18, 1, true)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        assertEquals(3.0, byName.get("BigMoney#1").averageScore(), 1e-9);
        assertEquals(0.0, byName.get("BigMoney#1").totalWinRate(), 1e-9);
        assertEquals(2.0, byName.get("BigMoney#1").medianRank(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_bestCasePlayerCanReachMaximumCprOfOne() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("Winner", "Loser"),
                List.of(
                    new GameRecord.PlayerRecord("Winner", 40, 1, true),
                    new GameRecord.PlayerRecord("Loser", 10, 2, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("Winner", "Loser"),
                List.of(
                    new GameRecord.PlayerRecord("Winner", 38, 1, true),
                    new GameRecord.PlayerRecord("Loser", 12, 2, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.PlayerPerformance winner = byName.get("Winner");
        assertEquals(1.0, winner.totalWinRate(), 1e-9);
        assertEquals(1.0, winner.topTwoRate(), 1e-9);
        assertEquals(1.0, winner.normalizedMedianRank(), 1e-9);
        assertEquals(1.0, winner.normalizedAverageScore(), 1e-9);
        assertEquals(0.0, winner.lastPlaceRate(), 1e-9);
        assertEquals(1.0, winner.compositePerformanceRating(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_buildsHeadToHeadRecords() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 30, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 2, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 18, 2, false),
                    new GameRecord.PlayerRecord("B", 22, 1, true)
                )
            ),
            new GameRecord(
                1,
                3,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 20, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 1, true)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.HeadToHeadRecord aVsB = byName.get("A").headToHeadRecords().get("B");
        assertEquals(1, aVsB.wins());
        assertEquals(1, aVsB.losses());
        assertEquals(1, aVsB.ties());
        assertEquals(1.0 / 3.0, aVsB.winRate(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_separatesOutrightAndSharedWins() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 25, 1, true),
                    new GameRecord.PlayerRecord("B", 25, 1, true)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 30, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 2, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.PlayerPerformance a = byName.get("A");
        PlayerRatingHarness.PlayerPerformance b = byName.get("B");

        assertEquals(2, a.totalWins());
        assertEquals(1, a.outrightWins());
        assertEquals(1, a.sharedWins());
        assertEquals(1, b.totalWins());
        assertEquals(0, b.outrightWins());
        assertEquals(1, b.sharedWins());
        assertEquals(1.0, a.totalWinRate(), 1e-9);
        assertEquals(0.5, a.outrightWinRate(), 1e-9);
        assertEquals(0.5, a.sharedWinRate(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_medianRankIgnoresSingleOutlierGame() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("Stable", "Outlier"),
                List.of(
                    new GameRecord.PlayerRecord("Stable", 30, 1, true),
                    new GameRecord.PlayerRecord("Outlier", 20, 2, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("Stable", "Outlier"),
                List.of(
                    new GameRecord.PlayerRecord("Stable", 29, 1, true),
                    new GameRecord.PlayerRecord("Outlier", 18, 2, false)
                )
            ),
            new GameRecord(
                1,
                3,
                List.of("Stable", "Outlier"),
                List.of(
                    new GameRecord.PlayerRecord("Stable", 1, 2, false),
                    new GameRecord.PlayerRecord("Outlier", 50, 1, true)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        // Stable should still look strong overall because median rank ignores the one bad game.
        assertEquals(1.0, byName.get("Stable").medianRank(), 1e-9);
        assertEquals(2.0, byName.get("Outlier").medianRank(), 1e-9);
        assertEquals(2.0 / 3.0, byName.get("Stable").totalWinRate(), 1e-9);
        assertEquals(1.0 / 3.0, byName.get("Outlier").totalWinRate(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_outlierGameDoesNotBreakMedianRankOrdering() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 40, 1, true),
                    new GameRecord.PlayerRecord("B", 10, 2, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 39, 1, true),
                    new GameRecord.PlayerRecord("B", 11, 2, false)
                )
            ),
            new GameRecord(
                1,
                3,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 1, 2, false),
                    new GameRecord.PlayerRecord("B", 100, 1, true)
                )
            )
        );

        List<PlayerRatingHarness.PlayerPerformance> ratings = harness.computePerformanceRatings(rawResults);

        assertEquals("A", ratings.get(0).playerName());
        assertEquals("B", ratings.get(1).playerName());
        assertEquals(2.0 / 3.0, ratings.get(0).totalWinRate(), 1e-9);
        assertEquals(1.0, ratings.get(0).medianRank(), 1e-9);
        assertEquals(2.0, ratings.get(1).medianRank(), 1e-9);
    }

    @Test
    public void computePerformanceRatings_countsSharedWinsAsSeparateFromOutrightWins() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B", "C"),
                List.of(
                    new GameRecord.PlayerRecord("A", 25, 1, true),
                    new GameRecord.PlayerRecord("B", 25, 1, true),
                    new GameRecord.PlayerRecord("C", 10, 3, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B", "C"),
                List.of(
                    new GameRecord.PlayerRecord("A", 26, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 2, false),
                    new GameRecord.PlayerRecord("C", 19, 3, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        assertEquals(2, byName.get("A").totalWins());
        assertEquals(1, byName.get("A").outrightWins());
        assertEquals(1, byName.get("A").sharedWins());
        assertEquals(1, byName.get("B").totalWins());
        assertEquals(0, byName.get("B").outrightWins());
        assertEquals(1, byName.get("B").sharedWins());
    }

    @Test
    public void computePerformanceRatings_headToHeadTiesAreTracked() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 20, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 1, true)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.HeadToHeadRecord aVsB = byName.get("A").headToHeadRecords().get("B");
        PlayerRatingHarness.HeadToHeadRecord bVsA = byName.get("B").headToHeadRecords().get("A");

        assertEquals(0, aVsB.wins());
        assertEquals(0, aVsB.losses());
        assertEquals(1, aVsB.ties());
        assertEquals(0, bVsA.wins());
        assertEquals(0, bVsA.losses());
        assertEquals(1, bVsA.ties());
    }

    @Test
    public void printPerformanceSummary_includesHeadToHeadAndOutrightDetails() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 30, 1, true),
                    new GameRecord.PlayerRecord("B", 30, 1, true)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 28, 1, true),
                    new GameRecord.PlayerRecord("B", 12, 2, false)
                )
            )
        );

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PlayerRatingHarness.printPerformanceSummary(harness.computePerformanceRatings(rawResults), out);
        String printed = bytes.toString(StandardCharsets.UTF_8);

        assertTrue(printed.contains("Outright Wins"));
        assertTrue(printed.contains("Shared Wins"));
        assertTrue(printed.contains("Head-to-Head Record:"));
        assertTrue(printed.contains("- vs [B]:"));
        assertTrue(printed.contains("- vs [A]:"));
    }

    @Test
    public void computePerformanceRatings_buildsSortedOpponentMatchupKey() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("C", "A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 25, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 2, false),
                    new GameRecord.PlayerRecord("C", 15, 3, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        PlayerRatingHarness.PlayerPerformance a = byName.get("A");
        assertTrue(a.matchupWinRates().containsKey("B | C"));
        PlayerRatingHarness.MatchupWinRate rate = a.matchupWinRates().get("B | C");
        assertNotNull(rate);
        assertEquals(1, rate.games());
        assertEquals(1, rate.wins());
    }

    @Test
    public void computePerformanceRatings_withNullMatchupPlayers_usesUnknownMatchupKey() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                null,
                List.of(
                    new GameRecord.PlayerRecord("A", 25, 1, true),
                    new GameRecord.PlayerRecord("B", 20, 2, false)
                )
            )
        );

        Map<String, PlayerRatingHarness.PlayerPerformance> byName = indexByName(
            harness.computePerformanceRatings(rawResults)
        );

        assertTrue(byName.get("A").matchupWinRates().containsKey("(unknown matchup)"));
        assertTrue(byName.get("B").matchupWinRates().containsKey("(unknown matchup)"));
    }

    @Test
    public void computePerformanceRatings_exposesUnmodifiableMatchupMap() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 10, 1, true),
                    new GameRecord.PlayerRecord("B", 9, 2, false)
                )
            )
        );

        PlayerRatingHarness.PlayerPerformance first = harness.computePerformanceRatings(rawResults).get(0);
        assertThrows(
            UnsupportedOperationException.class,
            () -> first.matchupWinRates().put("X", new PlayerRatingHarness.MatchupWinRate(1, 1, 1.0))
        );
    }

    @Test
    public void printPerformanceSummary_withEmptyList_printsNoMetricsMessage() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        PlayerRatingHarness.printPerformanceSummary(List.of(), out);
        String printed = bytes.toString(StandardCharsets.UTF_8);

        assertTrue(printed.contains("=== Composite Player Rating Summary ==="));
        assertTrue(printed.contains("No player metrics available."));
    }

    @Test
    public void printPerformanceSummary_includesCprScaleExplanation() {
        PlayerRatingHarness harness = new PlayerRatingHarness();

        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 10, 1, true),
                    new GameRecord.PlayerRecord("B", 5, 2, false)
                )
            )
        );

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        PlayerRatingHarness.printPerformanceSummary(harness.computePerformanceRatings(rawResults), out);

        String printed = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("CPR scale"));
        assertTrue(printed.contains("0.0000 to 1.0000, where 1.0000 is the best possible score"));
    }

    @Test
    public void printRawSummary_usesRecordedScoreAndRankTotals() throws Exception {
        List<GameRecord> rawResults = List.of(
            new GameRecord(
                1,
                1,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 10, 1, true),
                    new GameRecord.PlayerRecord("B", 4, 2, false)
                )
            ),
            new GameRecord(
                1,
                2,
                List.of("A", "B"),
                List.of(
                    new GameRecord.PlayerRecord("A", 6, 2, false),
                    new GameRecord.PlayerRecord("B", 8, 1, true)
                )
            )
        );

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        Method method = PlayerRatingHarness.class.getDeclaredMethod("printRawSummary", List.class, PrintStream.class);
        method.setAccessible(true);
        method.invoke(null, rawResults, out);

        String printed = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("=== Average Metrics Per Game ==="));
        assertTrue(printed.contains("- A: 8.0 average score, 1.5 average rank"));
        assertTrue(printed.contains("- B: 6.0 average score, 1.5 average rank"));
    }

    private static Map<String, PlayerRatingHarness.PlayerPerformance> indexByName(
        List<PlayerRatingHarness.PlayerPerformance> ratings
    ) {
        return ratings.stream().collect(Collectors.toMap(PlayerRatingHarness.PlayerPerformance::playerName, r -> r));
    }
}
