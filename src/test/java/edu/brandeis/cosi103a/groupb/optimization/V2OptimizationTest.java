package edu.brandeis.cosi103a.groupb.optimization;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.rating.optimization.optimizers.V2Optimizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * Individual card weight optimization test for WeightedPlayer2.
 * 
 * This test optimizes the weight of each individual card rather than category weights.
 * Runs automated optimization to discover optimal per-card weight configurations.
 * 
 * NOTE: This is computationally intensive - runs many games. Start with small
 * generation counts for testing, larger for production optimization.
 */
public class V2OptimizationTest {
    
    /**
     * Quick optimization run for individual card weights (3 generations, 2 configs, small games).
     * Good for testing the optimization framework.
     * NOTE: Output suppression is disabled so you can see optimization results.
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=WeightedPlayer2OptimizationTest#testQuickCardWeightOptimization
    @Test
    public void testQuickCardWeightOptimization() throws PlayerViolationException {
        // Output NOT suppressed for quick test so results are visible
        try {
            V2Optimizer optimizer = new V2Optimizer(
                3,              // generations
                5,              // games per matchup
                2,              // configs per generation (small)
                0.15f           // mutation rate (15% variation)
            );
            
            optimizer.optimize();
        } catch (Exception e) {
            System.err.println(">>> ERROR in testQuickCardWeightOptimization: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Medium optimization run for individual card weights (4 generations, 5 configs, moderate games).
     * Good for exploring the weight space with better convergence than quick.
     * Total games: ~300 (balanced speed and robustness).
     */
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=WeightedPlayer2OptimizationTest#testMediumCardWeightOptimization
    @Test
    public void testMediumCardWeightOptimization() throws PlayerViolationException {
        try {
            V2Optimizer optimizer = new V2Optimizer(
                4,              // generations
                15,             // games per matchup
                5,              // configs per generation
                0.20f           // mutation rate (20% variation)
            );
            
            optimizer.optimize();
        } catch (Exception e) {
            System.err.println(">>> ERROR in testMediumCardWeightOptimization: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Full optimization run for individual card weights (8 generations, 5 configs, thorough games).
     * Takes reasonable time and explores weight space thoroughly.
     * Total games: ~1,000.
     */
    //
    @Disabled("Optimization tests are computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=WeightedPlayer2OptimizationTest#testFullCardWeightOptimization
    @Test
    public void testFullCardWeightOptimization() throws PlayerViolationException {
        try {
            V2Optimizer optimizer = new V2Optimizer(
                8,              // generations
                25,             // games per matchup
                5,              // configs per generation
                0.15f           // mutation rate
            );
            
            optimizer.optimize();
        } catch (Exception e) {
            System.err.println(">>> ERROR in testFullCardWeightOptimization: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
