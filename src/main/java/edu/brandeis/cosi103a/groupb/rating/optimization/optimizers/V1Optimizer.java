package edu.brandeis.cosi103a.groupb.rating.optimization.optimizers;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer2;
import edu.brandeis.cosi103a.groupb.engine.Engine;
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CategoryWeights;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Automated optimization framework for WeightedPlayer.
 * Runs multiple generations of weight configurations competing against each other,
 * automatically evolving toward optimal weights through tournament play.
 */
public class V1Optimizer {
    
    private final int generationCount;
    private final int gamesPerMatchup;
    private final int configsPerGeneration;
    private final float mutationRate;
    private final PlayerVersion playerVersion;  // Toggle between WeightedPlayer and WeightedPlayer2
    
    /**
     * Player version selector for optimization
     */
    public enum PlayerVersion {
        WEIGHTED_PLAYER_V1("WeightedPlayer (category-based weights)"),
        WEIGHTED_PLAYER_V2("WeightedPlayer2 (individual card weights)");
        
        private final String description;
        
        PlayerVersion(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public V1Optimizer(int generationCount, int gamesPerMatchup, int configsPerGeneration, float mutationRate) {
        this(generationCount, gamesPerMatchup, configsPerGeneration, mutationRate, PlayerVersion.WEIGHTED_PLAYER_V1);
    }
    
    public V1Optimizer(int generationCount, int gamesPerMatchup, int configsPerGeneration, float mutationRate, PlayerVersion playerVersion) {
        this.generationCount = generationCount;
        this.gamesPerMatchup = gamesPerMatchup;
        this.configsPerGeneration = configsPerGeneration;
        this.mutationRate = mutationRate;
        this.playerVersion = playerVersion;
    }
    
    /**
     * Run the optimization process, evolving weights over multiple generations.
     */
    public void optimize() throws PlayerViolationException {
        System.err.println("[OPTIMIZER] Starting optimization...");
        System.err.flush();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  " + playerVersion.getDescription());
        System.out.println("║  Weight Optimization");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Generations: " + generationCount);
        System.out.println("Configs per generation: " + configsPerGeneration);
        System.out.println("Games per matchup: " + gamesPerMatchup);
        System.out.println("Mutation rate: " + mutationRate);
        System.out.println("Player Version: " + playerVersion.name() + "\n");
        System.out.flush();
        
        CategoryWeights baseConfig = CategoryWeights.createDefault();
        List<ConfigRating> currentGeneration = new ArrayList<>();
        
        // Generation 0: Create initial population from default
        System.out.println("Generation 0: Creating " + configsPerGeneration + " variants from default config...");
        for (int i = 0; i < configsPerGeneration; i++) {
            CategoryWeights variant = (i == 0) ? baseConfig : baseConfig.mutate(mutationRate);
            currentGeneration.add(new ConfigRating(variant, i));
        }
        
        // Run generations
        for (int gen = 0; gen < generationCount; gen++) {
            System.out.println("\n--- Generation " + gen + " ---");
            
            // Test all configs against each other
            runTournament(currentGeneration, gen);
            
            // Sort by win rate
            currentGeneration.sort(Comparator.comparingDouble(ConfigRating::getWinRate).reversed());
            
            // Print results
            printGenerationResults(currentGeneration, gen);
            
            // Prepare next generation (keep top configs, mutate them)
            if (gen < generationCount - 1) {
                currentGeneration = evolveNextGeneration(currentGeneration);
            }
        }
        
        // Final summary
        printFinalResults(currentGeneration);
    }
    
    /**
     * Run tournament for all configs in current generation.
     * NOTE: With random card selection (10 of 15 action cards per game),
     * each game has different available cards. Running more games allows
     * the optimizer to find weights that work across different card combinations.
     */
    private void runTournament(List<ConfigRating> generation, int generationNumber) throws PlayerViolationException {
        for (ConfigRating config : generation) {
            // Create players: one with this config vs two standard baselines
            List<SelectedPlayer> matchup = new ArrayList<>();
            
            // Use appropriate player class based on version toggle
            if (playerVersion == PlayerVersion.WEIGHTED_PLAYER_V2) {
                matchup.add(new SelectedPlayer("WeightedOpt-" + config.configId, () -> new WeightedPlayer2("WeightedOpt-" + config.configId, config.config)));
                matchup.add(new SelectedPlayer("BigMoney-baseline", () -> new WeightedPlayer2("BigMoney-baseline")));
            } else {
                matchup.add(new SelectedPlayer("WeightedOpt-" + config.configId, () -> new WeightedPlayer("WeightedOpt-" + config.configId, config.config)));
                matchup.add(new SelectedPlayer("BigMoney-baseline", () -> new WeightedPlayer("BigMoney-baseline")));
            }
            
            System.out.print("Testing config " + config.configId + "... ");
            
            // Run full games per matchup for more robust evaluation
            // With random card selection, variance is higher so we need more samples
            int gamesThisConfig = gamesPerMatchup;
            for (int game = 0; game < gamesThisConfig; game++) {
                try {
                    GameRecord record = runSingleGame(matchup);
                    
                    // Check if our config won
                    for (String winner : record.winners) {
                        if (winner.startsWith("WeightedOpt-" + config.configId)) {
                            config.wins++;
                            break;
                        }
                    }
                    config.gamesPlayed++;
                } catch (Exception e) {
                    System.err.println("Error in game: " + e.getMessage());
                }
            }
            
            System.out.println("Result: " + config.wins + "/" + config.gamesPlayed + " wins (" + String.format("%.1f%%", config.getWinRate() * 100) + ")");
        }
    }
    
    /**
     * Run a single game and return the record.
     */
    private GameRecord runSingleGame(List<SelectedPlayer> matchup) throws PlayerViolationException {
        // Create players from the selected templates
        List<ParentPlayer> players = new ArrayList<>();
        for (SelectedPlayer selected : matchup) {
            players.add(selected.factory.create());
        }
        
        // Run the game through the Engine
        Engine engine = new Engine(players);
        GameResult result = engine.play();
        
        // Extract winners from game result
        GameRecord record = new GameRecord(1, 1, matchup);
        if (result != null && result.playerResults() != null) {
            // Find the highest score(s)
            int maxScore = Integer.MIN_VALUE;
            for (PlayerResult pr : result.playerResults()) {
                if (pr.score() > maxScore) {
                    maxScore = pr.score();
                }
            }
            
            // Add all players with max score as winners (handles ties)
            for (PlayerResult pr : result.playerResults()) {
                if (pr.score() == maxScore) {
                    record.winners.add(pr.playerName());
                }
            }
        }
        
        return record;
    }
    
    /**
     * Evolve next generation from top performers.
     */
    private List<ConfigRating> evolveNextGeneration(List<ConfigRating> current) {
        List<ConfigRating> nextGen = new ArrayList<>();
        
        // Keep top 25% of configs
        int keepCount = Math.max(2, configsPerGeneration / 4);
        for (int i = 0; i < keepCount && i < current.size(); i++) {
            // Keep top config as-is
            if (i == 0) {
                ConfigRating elite = current.get(i);
                nextGen.add(new ConfigRating(elite.config, nextGen.size()));
            } else {
                // Mutate other top configs
                ConfigRating top = current.get(i);
                nextGen.add(new ConfigRating(top.config.mutate(mutationRate), nextGen.size()));
            }
        }
        
        // Fill rest with mutations of top performer
        ConfigRating topPerformer = current.get(0);
        while (nextGen.size() < configsPerGeneration) {
            CategoryWeights mutated = topPerformer.config.mutate(mutationRate * 1.5f);  // Larger mutations for exploration
            nextGen.add(new ConfigRating(mutated, nextGen.size()));
        }
        
        return nextGen;
    }
    
    /**
     * Print results for a generation.
     */
    private void printGenerationResults(List<ConfigRating> generation, int generationNumber) {
        System.out.println("\n[" + playerVersion.name() + "] Generation " + generationNumber + " Results:");
        System.out.println("Rank | Config ID | Win Rate | Details");
        System.out.println("-----|-----------|----------|----------------------------------------");
        
        for (int i = 0; i < Math.min(5, generation.size()); i++) {
            ConfigRating rating = generation.get(i);
            System.out.printf("%4d | %9d | %7.1f%% | %s\n",
                i + 1,
                rating.configId,
                rating.getWinRate() * 100,
                rating.config
            );
        }
    }
    
    /**
     * Print final optimization results.
     */
    private void printFinalResults(List<ConfigRating> topConfigs) {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  " + playerVersion.getDescription());
        System.out.println("║  OPTIMIZATION COMPLETE");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\nTop 5 Evolved Configurations:");
        System.out.println("Rank | Win Rate | Configuration");
        System.out.println("-----|----------|---------------------------------------------");
        
        for (int i = 0; i < Math.min(5, topConfigs.size()); i++) {
            ConfigRating rating = topConfigs.get(i);
            System.out.printf("%4d | %7.1f%% | %s\n",
                i + 1,
                rating.getWinRate() * 100,
                rating.config
            );
        }
        
        // Print best config in detail
        if (!topConfigs.isEmpty()) {
            ConfigRating best = topConfigs.get(0);
            System.out.println("\n=== BEST CONFIGURATION ===");
            System.out.println("Win Rate: " + String.format("%.1f%%", best.getWinRate() * 100));
            System.out.println("Category Weights:");
            System.out.println("  Circulation: " + best.config.circulation);
            System.out.println("  Bridging: " + best.config.bridging);
            System.out.println("  Attack: " + best.config.attack);
            System.out.println("  Money: " + best.config.money);
            System.out.println("  Defense: " + best.config.defense);
            System.out.println("  Points: " + best.config.points);
            System.out.println("\nThreat Thresholds:");
            System.out.println("  Minor: " + best.config.threatMinorThreshold);
            System.out.println("  Moderate: " + best.config.threatModerateThreshold);
        }
    }
    
    /**
     * Inner class tracking a configuration and its performance.
     */
    private static class ConfigRating {
        CategoryWeights config;
        int configId;
        int wins = 0;
        int gamesPlayed = 0;
        
        ConfigRating(CategoryWeights config, int configId) {
            this.config = config;
            this.configId = configId;
        }
        
        double getWinRate() {
            return gamesPlayed > 0 ? (double) wins / gamesPlayed : 0.0;
        }
    }
    
    /**
     * Inner class for tournament results (simplified).
     */
    private static class GameRecord {
        List<String> winners = new ArrayList<>();
        
        GameRecord(int matchupNumber, int gameNumber, List<SelectedPlayer> matchup) {
            // Stores matchup info for reference if needed in future extensions
        }
    }
    
    @FunctionalInterface
    public interface PlayerFactory {
        ParentPlayer create();
    }
    
    /**
     * Selected player wrapper.
     */
    public static class SelectedPlayer {
        String name;
        PlayerFactory factory;
        
        public SelectedPlayer(String name, PlayerFactory factory) {
            this.name = name;
            this.factory = factory;
        }
    }
}
