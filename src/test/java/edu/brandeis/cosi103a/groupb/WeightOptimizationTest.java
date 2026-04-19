package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.rating.WeightConfig;
import edu.brandeis.cosi103a.groupb.rating.WeightedPlayerOptimizer;
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


public class WeightOptimizationTest {
    
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
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    @Test
    public void testQuickWeightOptimization() throws PlayerViolationException {
        System.out.println("\n>>> Starting testQuickWeightOptimization");
        System.out.flush();
        
        try {
            WeightedPlayerOptimizer optimizer = new WeightedPlayerOptimizer(
                3,              // generations
                2,              // games per matchup (small for quick test)
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
     * Medium optimization run (5 generations, 5 configs, moderate games).
     * Good for initial exploring of the weight space.
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    @Test
    public void testMediumWeightOptimization() throws PlayerViolationException {
        WeightedPlayerOptimizer optimizer = new WeightedPlayerOptimizer(
            5,              // generations
            10,             // games per matchup
            5,              // configs per generation
            0.20f           // mutation rate (20% variation)
        );
        
        optimizer.optimize();
    }
    
    /**
     * Full optimization run (10 generations, 8 configs, thorough games).
     * Takes significant time but may find well-optimized weights.
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    @Test
    public void testFullWeightOptimization() throws PlayerViolationException {
        WeightedPlayerOptimizer optimizer = new WeightedPlayerOptimizer(
            10,             // generations
            25,             // games per matchup
            8,              // configs per generation
            0.15f           // mutation rate
        );
        
        optimizer.optimize();
    }
    
    /**
     * Verify that WeightedPlayer can be instantiated with a custom WeightConfig.
     */
    @Test
    public void testWeightedPlayerWithCustomConfig() {
        WeightConfig config = WeightConfig.createDefault();
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
        WeightConfig original = WeightConfig.createDefault();
        float originalCirculation = original.circulation;
        
        // Mutate with 25% variation rate
        WeightConfig mutated = original.mutate(0.25f);
        
        // Verify weights changed (probabilistically should be different)
        // Note: with randomness, could theoretically get same values, but very unlikely
        boolean hasChange = 
            Math.abs(mutated.circulation - originalCirculation) > 0.01f ||
            Math.abs(mutated.bridging - original.bridging) > 0.01f ||
            Math.abs(mutated.attack - original.attack) > 0.01f;
        
        assert hasChange : "Mutation should change at least one weight";
    }
}
