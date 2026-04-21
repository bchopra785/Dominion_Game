package edu.brandeis.cosi103a.groupb.rating.optimization.data_classes;

import edu.brandeis.cosi.atg.cards.Card;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Configuration class for individual card weights in WeightedPlayer2.
 * Stores a weight for each card type and supports mutation.
 */
public class CardWeights {
    
    private static final Random random = new Random();
    
    // Individual card weights
    public final Map<Card.Type, Float> cardWeights;
    
    /**
     * Create a card weight config with all weights set to a value.
     */
    public CardWeights(Map<Card.Type, Float> cardWeights) {
        this.cardWeights = new HashMap<>(cardWeights);
    }
    
    /**
     * Create default card weight config (optimized weights from medium optimization - 73.3% win rate).
     * Discovered through 300-game evolutionary optimization (4 generations × 5 configs × 15 games).
     */
    public static CardWeights createDefault() {
        Map<Card.Type, Float> weights = new HashMap<>();
        
        // Circulation cards (optimized)
        weights.put(Card.Type.DAILY_SCRUM, 3.62f);
        weights.put(Card.Type.BACKLOG, 3.376064f);
        weights.put(Card.Type.PARALLELIZATION, 2.3897896f);
        
        // Bridging cards (optimized)
        weights.put(Card.Type.IPO, 2.93f);
        weights.put(Card.Type.CODE_REVIEW, 2.9575925f);
        weights.put(Card.Type.SPRINT_PLANNING, 2.56f);
        weights.put(Card.Type.DEPLOYMENT_PIPELINE, 2.001893f);
        weights.put(Card.Type.TECH_DEBT, 1.47f);
        weights.put(Card.Type.UNIT_TEST, 1.4571187f);
        
        // Money cards (optimized)
        weights.put(Card.Type.BITCOIN, 1.936853f);
        weights.put(Card.Type.ETHEREUM, 2.4113789f);
        weights.put(Card.Type.DOGECOIN, 2.3845406f);
        
        // Attack cards (optimized)
        weights.put(Card.Type.EVERGREEN_TEST, 2.1362183f);
        weights.put(Card.Type.HACK, 2.6675334f);
        weights.put(Card.Type.RANSOMWARE, 2.057161f);
        
        // Defense cards (optimized)
        weights.put(Card.Type.MONITORING, 1.67f);
        weights.put(Card.Type.MERGE_CONFLICT, 1.9406875f);
        weights.put(Card.Type.REFACTOR, 1.8754771f);
        
        // Point cards (optimized)
        weights.put(Card.Type.FRAMEWORK, 0.9208751f);
        weights.put(Card.Type.MODULE, 0.5901168f);
        weights.put(Card.Type.METHOD, 0.36834785f);
        
        return new CardWeights(weights);
    }
    
    /**
     * Mutate all card weights with given mutation rate.
     * Returns a new CardWeightConfig with mutated weights.
     */
    public CardWeights mutate(float mutationRate) {
        Map<Card.Type, Float> mutated = new HashMap<>();
        
        for (Map.Entry<Card.Type, Float> entry : cardWeights.entrySet()) {
            if (random.nextFloat() < mutationRate) {
                // Mutate this weight
                float variance = 0.2f;  // 20% variance
                float factor = 1.0f + (random.nextFloat() - 0.5f) * 2 * variance;
                float newWeight = Math.max(0.1f, entry.getValue() * factor);  // Min weight 0.1
                mutated.put(entry.getKey(), newWeight);
            } else {
                // Keep original weight
                mutated.put(entry.getKey(), entry.getValue());
            }
        }
        
        return new CardWeights(mutated);
    }
    
    /**
     * Print all card weights for debugging.
     */
    public void printWeights() {
        System.out.println("\n  Circulation:");
        System.out.println("    DAILY_SCRUM: " + cardWeights.get(Card.Type.DAILY_SCRUM));
        System.out.println("    BACKLOG: " + cardWeights.get(Card.Type.BACKLOG));
        System.out.println("    PARALLELIZATION: " + cardWeights.get(Card.Type.PARALLELIZATION));
        
        System.out.println("\n  Bridging:");
        System.out.println("    IPO: " + cardWeights.get(Card.Type.IPO));
        System.out.println("    CODE_REVIEW: " + cardWeights.get(Card.Type.CODE_REVIEW));
        System.out.println("    SPRINT_PLANNING: " + cardWeights.get(Card.Type.SPRINT_PLANNING));
        System.out.println("    DEPLOYMENT_PIPELINE: " + cardWeights.get(Card.Type.DEPLOYMENT_PIPELINE));
        System.out.println("    TECH_DEBT: " + cardWeights.get(Card.Type.TECH_DEBT));
        System.out.println("    UNIT_TEST: " + cardWeights.get(Card.Type.UNIT_TEST));
        
        System.out.println("\n  Money:");
        System.out.println("    BITCOIN: " + cardWeights.get(Card.Type.BITCOIN));
        System.out.println("    ETHEREUM: " + cardWeights.get(Card.Type.ETHEREUM));
        System.out.println("    DOGECOIN: " + cardWeights.get(Card.Type.DOGECOIN));
        
        System.out.println("\n  Attack:");
        System.out.println("    EVERGREEN_TEST: " + cardWeights.get(Card.Type.EVERGREEN_TEST));
        System.out.println("    HACK: " + cardWeights.get(Card.Type.HACK));
        System.out.println("    RANSOMWARE: " + cardWeights.get(Card.Type.RANSOMWARE));
        
        System.out.println("\n  Defense:");
        System.out.println("    MONITORING: " + cardWeights.get(Card.Type.MONITORING));
        System.out.println("    MERGE_CONFLICT: " + cardWeights.get(Card.Type.MERGE_CONFLICT));
        System.out.println("    REFACTOR: " + cardWeights.get(Card.Type.REFACTOR));
        
        System.out.println("\n  Points:");
        System.out.println("    FRAMEWORK: " + cardWeights.get(Card.Type.FRAMEWORK));
        System.out.println("    MODULE: " + cardWeights.get(Card.Type.MODULE));
        System.out.println("    METHOD: " + cardWeights.get(Card.Type.METHOD));
    }
}
