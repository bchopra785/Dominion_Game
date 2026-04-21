package edu.brandeis.cosi103a.groupb.rating.optimization.optimizers;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer2;
import edu.brandeis.cosi103a.groupb.WeightedPlayer2CardWeightAdapter;
import edu.brandeis.cosi103a.groupb.engine.Engine;
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CardWeights;

import java.util.*;

/**
 * Automated optimization framework for individual card weights in WeightedPlayer2.
 * Optimizes per-card weights rather than category weights.
 * Runs multiple generations of card weight configurations competing against each other.
 */
public class V2Optimizer {
    
    private final int generationCount;
    private final int gamesPerMatchup;
    private final int configsPerGeneration;
    private final float mutationRate;
    
    public V2Optimizer(int generationCount, int gamesPerMatchup, int configsPerGeneration, float mutationRate) {
        this.generationCount = generationCount;
        this.gamesPerMatchup = gamesPerMatchup;
        this.configsPerGeneration = configsPerGeneration;
        this.mutationRate = mutationRate;
    }
    
    /**
     * Run the optimization process for individual card weights.
     */
    public void optimize() throws PlayerViolationException {
        System.err.println("[CARD OPTIMIZER] Starting individual card weight optimization...");
        System.err.flush();
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  WeightedPlayer2 - Individual Card Weight Optimization");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("Generations: " + generationCount);
        System.out.println("Configs per generation: " + configsPerGeneration);
        System.out.println("Games per matchup: " + gamesPerMatchup);
        System.out.println("Mutation rate: " + mutationRate + "\n");
        System.out.flush();
        
        CardWeights baseConfig = CardWeights.createDefault();
        List<CardConfigRating> currentGeneration = new ArrayList<>();
        
        // Generation 0: Create initial population from default
        System.out.println("Generation 0: Creating " + configsPerGeneration + " variants from default config...");
        for (int i = 0; i < configsPerGeneration; i++) {
            CardWeights variant = (i == 0) ? baseConfig : baseConfig.mutate(mutationRate);
            currentGeneration.add(new CardConfigRating(variant, i));
        }
        
        // Run generations
        for (int gen = 0; gen < generationCount; gen++) {
            System.out.println("\n--- Generation " + gen + " ---");
            
            // Test all configs against each other
            runTournament(currentGeneration, gen);
            
            // Sort by win rate
            currentGeneration.sort(Comparator.comparingDouble(CardConfigRating::getWinRate).reversed());
            
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
     */
    private void runTournament(List<CardConfigRating> generation, int generationNumber) throws PlayerViolationException {
        for (CardConfigRating config : generation) {
            // Create players: one with this config vs baseline
            List<SelectedPlayer> matchup = new ArrayList<>();
            matchup.add(new SelectedPlayer("CardWeightOpt-" + config.configId, 
                () -> new WeightedPlayer2CardWeightAdapter("CardWeightOpt-" + config.configId, config.config)));
            matchup.add(new SelectedPlayer("baseline", 
                () -> new WeightedPlayer2("baseline")));
            
            System.out.print("Testing config " + config.configId + "... ");
            
            int gamesThisConfig = gamesPerMatchup;
            for (int game = 0; game < gamesThisConfig; game++) {
                try {
                    GameRecord record = runSingleGame(matchup);
                    
                    // Check if our config won
                    for (String winner : record.winners) {
                        if (winner.startsWith("CardWeightOpt-" + config.configId)) {
                            config.wins++;
                            break;
                        }
                    }
                    config.gamesPlayed++;
                } catch (Exception e) {
                    System.err.println("Error in game: " + e.getMessage());
                }
            }
            
            System.out.println("Result: " + config.wins + "/" + config.gamesPlayed + " wins (" + 
                String.format("%.1f%%", config.getWinRate() * 100) + ")");
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
    private List<CardConfigRating> evolveNextGeneration(List<CardConfigRating> current) {
        List<CardConfigRating> nextGen = new ArrayList<>();
        
        // Keep top 25% of configs
        int keepCount = Math.max(2, configsPerGeneration / 4);
        for (int i = 0; i < keepCount && i < current.size(); i++) {
            // Keep top config as-is
            if (i == 0) {
                CardConfigRating elite = current.get(i);
                nextGen.add(new CardConfigRating(elite.config, nextGen.size()));
            } else {
                // Mutate other top configs
                CardConfigRating top = current.get(i);
                nextGen.add(new CardConfigRating(top.config.mutate(mutationRate), nextGen.size()));
            }
        }
        
        // Fill rest with mutations of top performer
        CardConfigRating topPerformer = current.get(0);
        while (nextGen.size() < configsPerGeneration) {
            CardWeights mutated = topPerformer.config.mutate(mutationRate * 1.5f);
            nextGen.add(new CardConfigRating(mutated, nextGen.size()));
        }
        
        return nextGen;
    }
    
    /**
     * Print results for a generation.
     */
    private void printGenerationResults(List<CardConfigRating> generation, int generationNumber) {
        System.out.println("\nGeneration " + generationNumber + " Results:");
        System.out.println("Rank | Config ID | Win Rate");
        System.out.println("-----|-----------|----------");
        
        for (int i = 0; i < Math.min(5, generation.size()); i++) {
            CardConfigRating rating = generation.get(i);
            System.out.printf("%4d | %9d | %7.1f%%\n",
                i + 1,
                rating.configId,
                rating.getWinRate() * 100
            );
        }
    }
    
    /**
     * Print final optimization results.
     */
    private void printFinalResults(List<CardConfigRating> topConfigs) {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  WeightedPlayer2 - Individual Card Weight Optimization");
        System.out.println("║  OPTIMIZATION COMPLETE");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        System.out.println("\nTop 5 Evolved Configurations:");
        System.out.println("Rank | Win Rate");
        System.out.println("-----|----------");
        
        for (int i = 0; i < Math.min(5, topConfigs.size()); i++) {
            CardConfigRating rating = topConfigs.get(i);
            System.out.printf("%4d | %7.1f%%\n",
                i + 1,
                rating.getWinRate() * 100
            );
        }
        
        // Print best config in detail
        if (!topConfigs.isEmpty()) {
            CardConfigRating best = topConfigs.get(0);
            System.out.println("\n=== BEST CARD WEIGHT CONFIGURATION ===");
            System.out.println("Win Rate: " + String.format("%.1f%%", best.getWinRate() * 100));
            System.out.println("\nCard Weights:");
            best.config.printWeights();
        }
    }
    
    /**
     * Inner class to track card config ratings.
     */
    static class CardConfigRating {
        CardWeights config;
        int configId;
        int wins = 0;
        int gamesPlayed = 0;
        
        CardConfigRating(CardWeights config, int configId) {
            this.config = config;
            this.configId = configId;
        }
        
        double getWinRate() {
            return gamesPlayed == 0 ? 0 : (double) wins / gamesPlayed;
        }
    }
    
    /**
     * Inner class for selected players in tournament.
     */
    static class SelectedPlayer {
        String name;
        Factory factory;
        
        SelectedPlayer(String name, Factory factory) {
            this.name = name;
            this.factory = factory;
        }
        
        interface Factory {
            ParentPlayer create();
        }
    }
    
    /**
     * Inner class for game records.
     */
    static class GameRecord {
        int wins;
        int losses;
        List<String> winners = new ArrayList<>();
        
        GameRecord(int wins, int losses, List<SelectedPlayer> matchup) {
            this.wins = wins;
            this.losses = losses;
        }
    }
}
