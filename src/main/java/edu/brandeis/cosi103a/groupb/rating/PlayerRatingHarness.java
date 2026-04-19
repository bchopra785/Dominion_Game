package edu.brandeis.cosi103a.groupb.rating;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.BigMoneyPlayer;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.StrategyPlayer;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        Map<String, Integer> wins = new LinkedHashMap<>();
        for (GameRecord game : rawResults) {
            for (GameRecord.PlayerRecord playerResult : game.playerResults()) {
                wins.putIfAbsent(playerResult.playerName(), 0);
                if (playerResult.winner()) {
                    wins.put(playerResult.playerName(), wins.get(playerResult.playerName()) + 1);
                }
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
        out.println("Raw game records are available in-memory for metric computation.");
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
        return templates;
    }

    public record Template(
        String name,
        String description,
        Function<String, ParentPlayer> factory
    ) {}
}
