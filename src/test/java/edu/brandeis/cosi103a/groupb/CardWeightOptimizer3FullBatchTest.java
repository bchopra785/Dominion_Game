package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import edu.brandeis.cosi103a.groupb.rating.optimization.CardWeightOptimizer3;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;

/**
 * Full batch optimization for all 66 board configurations.
 * Runs CardWeightOptimizer3 for all C(12,2) pairs of missing action cards.
 * 
 * Parameters: 20 generations × 20 games/matchup × 5 configs/gen (STRONGER MODE)
 * Estimated runtime: ~15-30 minutes per configuration = ~990-1980 minutes (16.5-33 hours)
 * 
 * Run with: mvn test -Dtest=CardWeightOptimizer3FullBatchTest -q
 */
public class CardWeightOptimizer3FullBatchTest {

    //@Disabled("Full batch optimization is computationally expensive. Run manually when tuning weights.")
    // mvn test -Dtest=CardWeightOptimizer3FullBatchTest -q
    @Test
    public void testOptimizeAllConfigurations() {
        System.out.println("\n" +
            "======================================================================\n" +
            "FULL BATCH OPTIMIZATION: 66 Board Configurations (STRONGER PARAMS)\n" +
            "======================================================================\n");

        CardWeightOptimizer3 optimizer = new CardWeightOptimizer3(
            20,  // generations (increased from 10)
            20,  // games per matchup (increased from 10)
            5,   // configs per generation (same as validation)
            0.15f // mutation rate
        );

        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("Starting optimization for all 66 configurations...");
            System.out.println("Estimated duration: 16-33 hours depending on system performance\n");
            
            // Run optimization for all configurations
            var allConfigs = optimizer.optimizeAllConfigurations();
            
            long endTime = System.currentTimeMillis();
            long elapsedSeconds = (endTime - startTime) / 1000;
            long elapsedMinutes = elapsedSeconds / 60;
            
            System.out.println("\n" +
                "======================================================================\n" +
                "BATCH OPTIMIZATION COMPLETE\n" +
                "======================================================================\n");
            System.out.println("Configurations optimized: " + allConfigs.size() + "/66");
            System.out.println("Elapsed time: " + elapsedMinutes + " minutes (" + elapsedSeconds + " seconds)");
            System.out.println("Average time per config: " + (elapsedSeconds / allConfigs.size()) + " seconds");
            
            // Save results to file
            System.out.println("\nSaving results to file...");
            CardWeightOptimizer3.saveConfigsToFile(allConfigs, "deckaware_weights.txt");
            
            // Verify results
            assertNotNull(allConfigs, "Optimization should return results");
            
            System.out.println("\n✓ Batch optimization successful!");
            System.out.println("  Results saved to: deckaware_weights.txt");
            System.out.println("  WeightedPlayer3 will auto-load these weights on next startup");
            
        } catch (Exception e) {
            System.err.println("ERROR: Batch optimization failed!");
            e.printStackTrace();
            throw new RuntimeException("Batch optimization failed", e);
        }
    }
}
