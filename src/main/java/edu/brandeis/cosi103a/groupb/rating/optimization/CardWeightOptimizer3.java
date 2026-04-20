package edu.brandeis.cosi103a.groupb.rating.optimization;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.WeightedPlayer3;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Specialized optimization framework for WeightedPlayer3 deck-aware weights.
 * 
 * Optimizes 66 different weight configurations, one for each possible pair of
 * missing action cards (C(12,2) = 66 combinations).
 * 
 * Forces the board to specifically exclude the target card pair for each optimization run,
 * ensuring weights are evolved for that exact board configuration.
 */
public class CardWeightOptimizer3 {
    
    private final int generationCount;
    private final int gamesPerMatchup;
    private final int configsPerGeneration;
    private final float mutationRate;
    
    // Static field to control which cards BoardCards will exclude
    private static Set<Card.Type> forcedExcludeCards = null;
    
    public CardWeightOptimizer3(int generationCount, int gamesPerMatchup, int configsPerGeneration, float mutationRate) {
        this.generationCount = generationCount;
        this.gamesPerMatchup = gamesPerMatchup;
        this.configsPerGeneration = configsPerGeneration;
        this.mutationRate = mutationRate;
    }
    
    /**
     * Optimize weights for all 66 possible board configurations (all card pairs).
     */
    public Map<String, CardWeightConfig> optimizeAllConfigurations() throws PlayerViolationException {
        System.err.println("[OPTIMIZER3] Starting deck-aware weight optimization for all 66 configurations...");
        System.err.flush();
        
        Map<String, CardWeightConfig> allResults = new HashMap<>();
        
        // List of all 12 action cards we consider
        Card.Type[] actionCards = {
            Card.Type.BACKLOG, Card.Type.CODE_REVIEW, Card.Type.SPRINT_PLANNING, Card.Type.UNIT_TEST,
            Card.Type.HACK, Card.Type.REFACTOR, Card.Type.PARALLELIZATION, Card.Type.MONITORING,
            Card.Type.IPO, Card.Type.EVERGREEN_TEST, Card.Type.DAILY_SCRUM, Card.Type.RANSOMWARE
        };
        
        int totalConfigs = 66;
        int configIndex = 0;
        
        // Iterate through all combinations of 2 cards
        for (int i = 0; i < actionCards.length; i++) {
            for (int j = i + 1; j < actionCards.length; j++) {
                configIndex++;
                Card.Type card1 = actionCards[i];
                Card.Type card2 = actionCards[j];
                
                // Create sorted key (alphabetically)
                String key = card1.toString().compareTo(card2.toString()) < 0 
                    ? card1 + "_" + card2 
                    : card2 + "_" + card1;
                
                System.out.println("\n" + "=".repeat(70));
                System.out.println("Optimizing Configuration " + configIndex + "/" + totalConfigs);
                System.out.println("Missing Cards: " + key);
                System.out.println("=".repeat(70));
                
                // Set forced exclusion
                forcedExcludeCards = new HashSet<>();
                forcedExcludeCards.add(card1);
                forcedExcludeCards.add(card2);
                
                // Run optimization for this specific configuration
                CardWeightConfig bestConfig = optimizeForBoardConfiguration(key);
                allResults.put(key, bestConfig);
                
                // Clear forced exclusion
                forcedExcludeCards = null;
            }
        }
        
        return allResults;
    }
    
    /**
     * Optimize weights for a single board configuration.
     * Assumes forcedExcludeCards is already set.
     */
    public CardWeightConfig optimizeForBoardConfiguration(String configKey) throws PlayerViolationException {
        // System.out.println("\nOptimization Parameters:");
        // System.out.println("  Generations: " + generationCount);
        // System.out.println("  Configs per generation: " + configsPerGeneration);
        // System.out.println("  Games per matchup: " + gamesPerMatchup);
        // System.out.println("  Mutation rate: " + mutationRate);
        
        CardWeightConfig baseConfig = CardWeightConfig.createDefault();
        List<CardConfigRating> currentGeneration = new ArrayList<>();
        
        // Generation 0: Create initial population
        // System.out.println("\nGeneration 0: Creating " + configsPerGeneration + " variants from default...");
        for (int i = 0; i < configsPerGeneration; i++) {
            CardWeightConfig variant = (i == 0) ? baseConfig : baseConfig.mutate(mutationRate);
            currentGeneration.add(new CardConfigRating(variant, i));
        }
        
        // Run generations
        for (int gen = 0; gen < generationCount; gen++) {
            // System.out.println("\n--- Generation " + gen + " ---");
            
            // Test all configs against each other
            runTournament(currentGeneration, configKey);
            
            // Sort by win rate
            currentGeneration.sort(Comparator.comparingDouble(CardConfigRating::getWinRate).reversed());
            
            // Print results
            // printGenerationResults(currentGeneration, gen);
            
            // Prepare next generation
            if (gen < generationCount - 1) {
                currentGeneration = evolveNextGeneration(currentGeneration);
            }
        }
        
        // Return best config
        CardConfigRating best = currentGeneration.get(0);
        System.out.println("\n✓ Best config for " + configKey + ": " + String.format("%.1f%%", best.getWinRate() * 100) + " win rate");
        
        // Print card weights
        // System.out.println("Card Weights:");
        // for (Map.Entry<Card.Type, Float> entry : best.config.cardWeights.entrySet()) {
        //     System.out.println("  " + entry.getKey() + ": " + String.format("%.3f", entry.getValue()));
        // }
        
        return best.config;
    }
    
    /**
     * Run tournament for configs with forced board configuration.
     */
    private void runTournament(List<CardConfigRating> generation, String configKey) throws PlayerViolationException {
        for (CardConfigRating config : generation) {
            // System.out.print("  Config " + config.configId + "... ");
            
            int wins = 0;
            int gamesPlayed = 0;
            
            for (int game = 0; game < gamesPerMatchup; game++) {
                try {
                    // Run game with forced board configuration - silent mode for optimization
                    List<ParentPlayer> players = new ArrayList<>();
                    players.add(new WeightedPlayer3("WeightedPlayer3-" + config.configId, false));
                    players.add(new WeightedPlayer3("baseline", false));
                    
                    GameRecord record = runSingleGameWithForcedBoard(players);
                    
                    if (record.winners != null) {
                        for (String winner : record.winners) {
                            if (winner.contains("WeightedPlayer3-" + config.configId)) {
                                wins++;
                                break;
                            }
                        }
                    }
                    gamesPlayed++;
                } catch (Exception e) {
                    System.err.println("\nError in game: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            config.wins = wins;
            config.gamesPlayed = gamesPlayed;
            // System.out.println(wins + "/" + gamesPlayed + " (" + String.format("%.1f%%", config.getWinRate() * 100) + ")");
        }
    }
    
    /**
     * Run a single game with the forced board configuration.
     */
    private GameRecord runSingleGameWithForcedBoard(List<ParentPlayer> players) throws PlayerViolationException {
        // Use the modified Engine that respects forcedExcludeCards
        Engine engine = new Engine(players, forcedExcludeCards);
        GameResult result = engine.play();
        
        GameRecord record = new GameRecord(1, 1, null);
        if (result != null && result.playerResults() != null) {
            int maxScore = Integer.MIN_VALUE;
            for (PlayerResult pr : result.playerResults()) {
                if (pr.score() > maxScore) {
                    maxScore = pr.score();
                }
            }
            
            for (PlayerResult pr : result.playerResults()) {
                if (pr.score() == maxScore) {
                    record.winners.add(pr.playerName());
                }
            }
        }
        
        return record;
    }
    
    /**
     * Evolve next generation.
     */
    private List<CardConfigRating> evolveNextGeneration(List<CardConfigRating> current) {
        List<CardConfigRating> nextGen = new ArrayList<>();
        
        int keepCount = Math.max(2, configsPerGeneration / 4);
        for (int i = 0; i < keepCount && i < current.size(); i++) {
            if (i == 0) {
                CardConfigRating elite = current.get(i);
                nextGen.add(new CardConfigRating(elite.config, nextGen.size()));
            } else {
                CardConfigRating top = current.get(i);
                nextGen.add(new CardConfigRating(top.config.mutate(mutationRate), nextGen.size()));
            }
        }
        
        CardConfigRating topPerformer = current.get(0);
        while (nextGen.size() < configsPerGeneration) {
            CardWeightConfig mutated = topPerformer.config.mutate(mutationRate * 1.5f);
            nextGen.add(new CardConfigRating(mutated, nextGen.size()));
        }
        
        return nextGen;
    }
    
    /**
     * Print generation results.
     */
    private void printGenerationResults(List<CardConfigRating> generation, int generationNumber) {
        System.out.println("Results:");
        System.out.println("Rank | Config | Win Rate");
        System.out.println("-----|--------|----------");
        
        for (int i = 0; i < Math.min(5, generation.size()); i++) {
            CardConfigRating rating = generation.get(i);
            System.out.printf("%4d | %6d | %7.1f%%\n",
                i + 1,
                rating.configId,
                rating.getWinRate() * 100
            );
        }
    }
    
    /**
     * Save all optimized configurations to a file in pipe-delimited format.
     * Format: CONFIG_KEY|CARD1:WEIGHT1|CARD2:WEIGHT2|...
     */
    public static void saveConfigsToFile(Map<String, CardWeightConfig> configs, String filename) throws IOException {
        Path filePath = Paths.get(filename);
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (Map.Entry<String, CardWeightConfig> entry : configs.entrySet()) {
                String configKey = entry.getKey();
                CardWeightConfig config = entry.getValue();
                
                StringBuilder line = new StringBuilder(configKey);
                
                // Add all card weights in sorted order for consistency
                config.cardWeights.entrySet().stream()
                    .sorted((a, b) -> a.getKey().toString().compareTo(b.getKey().toString()))
                    .forEach(e -> line.append("|").append(e.getKey()).append(":").append(String.format("%.4f", e.getValue())));
                
                writer.write(line.toString());
                writer.newLine();
            }
        }
        
        System.out.println("✓ Saved " + configs.size() + " configurations to: " + filename);
    }
    
    /**
     * Load all configurations from file.
     * Returns a map: configKey -> CardWeightConfig
     */
    public static Map<String, CardWeightConfig> loadConfigsFromFile(String filename) throws IOException {
        Map<String, CardWeightConfig> configs = new HashMap<>();
        Path filePath = Paths.get(filename);
        
        if (!Files.exists(filePath)) {
            System.out.println("[WARNING] Config file not found: " + filename);
            return configs;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    continue;
                }
                
                String configKey = parts[0];
                Map<Card.Type, Float> weights = new HashMap<>();
                
                // Parse card:weight pairs
                for (int i = 1; i < parts.length; i++) {
                    String[] cardWeight = parts[i].split(":");
                    if (cardWeight.length == 2) {
                        try {
                            Card.Type cardType = Card.Type.valueOf(cardWeight[0]);
                            Float weight = Float.parseFloat(cardWeight[1]);
                            weights.put(cardType, weight);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid card types
                        }
                    }
                }
                
                if (!weights.isEmpty()) {
                    configs.put(configKey, new CardWeightConfig(weights));
                }
            }
        }
        
        System.out.println("✓ Loaded " + configs.size() + " configurations from: " + filename);
        return configs;
    }
    
    /**
     * Internal class for tracking config ratings.
     */
    private static class CardConfigRating {
        CardWeightConfig config;
        int configId;
        int wins = 0;
        int gamesPlayed = 0;
        
        CardConfigRating(CardWeightConfig config, int id) {
            this.config = config;
            this.configId = id;
        }
        
        double getWinRate() {
            return gamesPlayed > 0 ? (double) wins / gamesPlayed : 0.0;
        }
    }
    
    /**
     * Internal class for game records.
     */
    private static class GameRecord {
        int gameCount;
        int playerCount;
        List<String> winners = new ArrayList<>();
        
        GameRecord(int gameCount, int playerCount, Object unused) {
            this.gameCount = gameCount;
            this.playerCount = playerCount;
        }
    }
    
    /**
     * Static method to get the forced exclusions (used by Engine and BoardCards).
     */
    public static Set<Card.Type> getForcedExcludeCards() {
        return forcedExcludeCards;
    }
    
    /**
     * Static method to set forced exclusions (used for testing).
     */
    public static void setForcedExcludeCards(Set<Card.Type> cards) {
        forcedExcludeCards = cards;
    }
}
