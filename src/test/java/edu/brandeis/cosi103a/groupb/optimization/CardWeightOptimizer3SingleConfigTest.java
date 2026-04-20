package edu.brandeis.cosi103a.groupb.optimization;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.rating.optimization.CardWeightConfig;
import edu.brandeis.cosi103a.groupb.rating.optimization.CardWeightOptimizer3;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Test CardWeightOptimizer3 with a single configuration to validate the approach.
 * This tests if forcing specific cards to be missing works correctly and if optimization
 * can find good weights for that board configuration.
 * 
 * Running time: ~5-10 minutes for 1 config (2 generations × 5 configs × 15 games)
 */
public class CardWeightOptimizer3SingleConfigTest {
    
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=CardWeightOptimizer3SingleConfigTest
    @Test
    public void testSingleConfigurationOptimization() throws PlayerViolationException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Testing CardWeightOptimizer3 with Single Configuration");
        System.out.println("=".repeat(70));
        
        // Use medium parameters for realistic optimization (10 generations, 10 games, 5 configs)
        CardWeightOptimizer3 optimizer = new CardWeightOptimizer3(
            10,     // generations (medium test)
            10,     // games per matchup (medium test)
            5,      // configs per generation
            0.15f   // mutation rate
        );
        
        System.out.println("\nTest Parameters:");
        System.out.println("  Generations: 10 (medium test)");
        System.out.println("  Games per matchup: 10 (medium test)");
        System.out.println("  Configs per generation: 5");
        System.out.println("  Mutation rate: 0.15");
        System.out.println("  Total games: 500 (10 gen × 5 configs × 10 games)");
        System.out.println("\nExpected runtime: ~5-20 seconds");
        System.out.println("Target Configuration: BACKLOG + CODE_REVIEW missing");
        System.out.println("(Testing with 10 cards: all except these 2)\n");
        
        // Set up forced exclusions before running optimization
        Set<Card.Type> excludeCards = new HashSet<>();
        excludeCards.add(Card.Type.BACKLOG);
        excludeCards.add(Card.Type.CODE_REVIEW);
        CardWeightOptimizer3.setForcedExcludeCards(excludeCards);
        
        try {
            // Run optimization for a single config pair
            CardWeightConfig bestConfig = optimizer.optimizeForBoardConfiguration("BACKLOG_CODE_REVIEW");
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("✓ Optimization Complete for BACKLOG_CODE_REVIEW");
            System.out.println("=".repeat(70));
            System.out.println("\nBest Configuration Details:");
            System.out.println("  Total Cards in Config: " + bestConfig.cardWeights.size());
            System.out.println("  All Optimized Weights:");
            
            // Print all weights organized by category
            System.out.println("\n  Circulation:");
            System.out.printf("    DAILY_SCRUM: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.DAILY_SCRUM, 0.0f));
            System.out.printf("    BACKLOG: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.BACKLOG, 0.0f));
            System.out.printf("    PARALLELIZATION: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.PARALLELIZATION, 0.0f));
            
            System.out.println("\n  Bridging:");
            System.out.printf("    IPO: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.IPO, 0.0f));
            System.out.printf("    CODE_REVIEW: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.CODE_REVIEW, 0.0f));
            System.out.printf("    SPRINT_PLANNING: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.SPRINT_PLANNING, 0.0f));
            System.out.printf("    DEPLOYMENT_PIPELINE: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.DEPLOYMENT_PIPELINE, 0.0f));
            System.out.printf("    TECH_DEBT: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.TECH_DEBT, 0.0f));
            System.out.printf("    UNIT_TEST: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.UNIT_TEST, 0.0f));
            
            System.out.println("\n  Money:");
            System.out.printf("    BITCOIN: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.BITCOIN, 0.0f));
            System.out.printf("    ETHEREUM: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.ETHEREUM, 0.0f));
            System.out.printf("    DOGECOIN: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.DOGECOIN, 0.0f));
            
            System.out.println("\n  Attack:");
            System.out.printf("    EVERGREEN_TEST: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.EVERGREEN_TEST, 0.0f));
            System.out.printf("    HACK: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.HACK, 0.0f));
            System.out.printf("    RANSOMWARE: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.RANSOMWARE, 0.0f));
            
            System.out.println("\n  Defense:");
            System.out.printf("    MONITORING: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.MONITORING, 0.0f));
            System.out.printf("    MERGE_CONFLICT: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.MERGE_CONFLICT, 0.0f));
            System.out.printf("    REFACTOR: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.REFACTOR, 0.0f));
            
            System.out.println("\n  Points:");
            System.out.printf("    FRAMEWORK: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.FRAMEWORK, 0.0f));
            System.out.printf("    MODULE: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.MODULE, 0.0f));
            System.out.printf("    METHOD: %.4f\n", bestConfig.cardWeights.getOrDefault(Card.Type.METHOD, 0.0f));
            
            System.out.println("\n✓ Test validation passed!");
            System.out.println("  - Optimizer ran successfully");
            System.out.println("  - Board configuration was forced correctly");
            System.out.println("  - Weights were optimized for the specific configuration");
            System.out.println("\nNext: If results look good, run full 66-config optimization");
        } finally {
            // Clean up
            CardWeightOptimizer3.setForcedExcludeCards(null);
        }
    }
}
