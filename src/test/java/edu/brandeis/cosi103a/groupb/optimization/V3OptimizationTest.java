package edu.brandeis.cosi103a.groupb.optimization;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.WeightedPlayer;
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CategoryWeights;
import edu.brandeis.cosi103a.groupb.rating.optimization.optimizers.V1Optimizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Weight optimization test for WeightedPlayer.
 * 
 * Runs automated optimization to discover optimal weight configurations.
 * This test creates multiple weight variants and evolves them over generations,
 * keeping track of which weights perform best in tournament play.
 *
 * NOTE: This is computationally intensive - runs many games. Start with small
 * generation counts for testing, larger for production optimization.
 */


public class V3OptimizationTest {
    
    /**
     * Simple test to verify System.out works in test harness.
     */
    @Test
    public void testSystemOutWorks() {
        System.out.println("\n>>> testSystemOutWorks is running");
        System.out.println(">>> This test just verifies console output works");
        System.out.flush();
        assert true;
    }
    
    /**
     * Quick optimization run (3 generations, 2 configs, small games).
     * Good for testing the optimization framework.
     * NOTE: Increased from 2 to 5 games per matchup to account for random card selection variance.
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    
    // mvn test -Dtest=WeightedPlayerOptimizationTest#testQuickWeightOptimization
    
    @Test
    public void testQuickWeightOptimization() throws PlayerViolationException {
        System.out.println("\n>>> Starting testQuickWeightOptimization");
        System.out.flush();
        
        try {
            V1Optimizer optimizer = new V1Optimizer(
                3,              // generations
                5,              // games per matchup (increased from 2 for random card variance)
                2,              // configs per generation (small)
                0.15f           // mutation rate (15% variation)
            );
            
            System.out.println(">>> Calling optimizer.optimize()");
            System.out.flush();
            optimizer.optimize();
            System.out.println(">>> optimize() completed successfully");
            System.out.flush();
        } catch (Exception e) {
            System.err.println(">>> ERROR in testQuickWeightOptimization: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Medium optimization run (7 generations, 6 configs, moderate games).
     * Good for exploring the weight space with better convergence than quick.
     * NOTE: Increased from 5 gen/5 configs/20 games to reduce variance with random card selection.
     * Total games: ~1,050 (manageable but more robust than smaller settings).
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=WeightedPlayerOptimizationTest#testMediumWeightOptimization
    @Test
    public void testMediumWeightOptimization() throws PlayerViolationException {
        V1Optimizer optimizer = new V1Optimizer(
            7,              // generations (increased from 5)
            25,             // games per matchup (increased from 20 for better noise reduction)
            6,              // configs per generation (increased from 5 for more diversity)
            0.20f           // mutation rate (20% variation)
        );
        
        optimizer.optimize();
    }
    
    /**
     * Full optimization run (8 generations, 7 configs, thorough games).
     * Takes reasonable time and explores weight space thoroughly.
     * NOTE: More thorough than medium (1,400 games vs 1,050) but still manageable.
     * Total games: ~1,400.
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=WeightedPlayerOptimizationTest#testFullWeightOptimization
    @Test
    public void testFullWeightOptimization() throws PlayerViolationException {
        V1Optimizer optimizer = new V1Optimizer(
            8,              // generations (more than medium's 7)
            25,             // games per matchup (same as medium)
            7,              // configs per generation (more than medium's 6)
            0.15f           // mutation rate
        );
        
        optimizer.optimize();
    }
    
    /**
     * Verify that WeightedPlayer can be instantiated with a custom WeightConfig.
     */
    @Test
    public void testWeightedPlayerWithCustomConfig() {
        CategoryWeights config = CategoryWeights.createDefault();
        config.circulation = 5.0f;  // Boost circulation cards
        config.money = 1.5f;        // Reduce money priority
        
        WeightedPlayer player = new WeightedPlayer("TestPlayer", config);
        
        // Verify player was created
        assert player.getName().equals("TestPlayer");
    }
    
    /**
     * Verify weight config mutation creates variations.
     */
    @Test
    public void testWeightConfigMutation() {
        CategoryWeights original = CategoryWeights.createDefault();
        float originalCirculation = original.circulation;
        
        // Mutate with 25% variation rate
        CategoryWeights mutated = original.mutate(0.25f);
        
        // Verify weights changed (probabilistically should be different)
        // Note: with randomness, could theoretically get same values, but very unlikely
        boolean hasChange = 
            Math.abs(mutated.circulation - originalCirculation) > 0.01f ||
            Math.abs(mutated.bridging - original.bridging) > 0.01f ||
            Math.abs(mutated.attack - original.attack) > 0.01f;
        
        assert hasChange : "Mutation should change at least one weight";
    }
}
