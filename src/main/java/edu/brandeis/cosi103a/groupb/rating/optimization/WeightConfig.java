package edu.brandeis.cosi103a.groupb.rating.optimization;

/**
 * Encapsulates all tunable weights for WeightedPlayer strategy.
 * Used by WeightedPlayerOptimizer to evolve optimal parameters.
 */
public class WeightConfig {
    
    // Category weights
    public float circulation;
    public float bridging;
    public float attack;
    public float money;
    public float defense;
    public float points;
    
    // Threat thresholds
    public int threatMinorThreshold;
    public int threatModerateThreshold;
    
    // Defense weights at different threat levels
    public float defenseWeightNoThreat;
    public float defenseWeightMinor;
    public float defenseWeightModerate;
    public float defenseWeightHigh;
    
    // Action card values (sample key ones)
    public float actionCardDailyScrum;
    public float actionCardBacklog;
    public float actionCardIpo;
    
    /**
     * Create a config with default values (current known-good settings)
     */
    public static WeightConfig createDefault() {
        WeightConfig config = new WeightConfig();
        
        // Default category weights (from current WeightedPlayer)
        config.circulation = 4.0f;
        config.bridging = 3.5f;
        config.attack = 2.5f;
        config.money = 2.0f;
        config.defense = 1.5f;
        config.points = 1.0f;
        
        // Default threat settings
        config.threatMinorThreshold = 2;
        config.threatModerateThreshold = 5;
        
        // Default defense weights
        config.defenseWeightNoThreat = 0.0f;
        config.defenseWeightMinor = 1.0f;
        config.defenseWeightModerate = 2.0f;
        config.defenseWeightHigh = 3.0f;
        
        // Default action card values
        config.actionCardDailyScrum = 5.0f;
        config.actionCardBacklog = 5.0f;
        config.actionCardIpo = 4.5f;
        
        return config;
    }
    
    /**
     * Create a mutated copy with small random adjustments
     */
    public WeightConfig mutate(float mutationRate) {
        WeightConfig mutated = new WeightConfig();
        
        // Mutate category weights (±20% variation)
        mutated.circulation = circulation * randomVariation(mutationRate);
        mutated.bridging = bridging * randomVariation(mutationRate);
        mutated.attack = attack * randomVariation(mutationRate);
        mutated.money = money * randomVariation(mutationRate);
        mutated.defense = defense * randomVariation(mutationRate);
        mutated.points = points * randomVariation(mutationRate);
        
        // Mutate threat settings slightly
        mutated.threatMinorThreshold = Math.max(1, threatMinorThreshold + randomInt(-1, 1));
        mutated.threatModerateThreshold = Math.max(threatMinorThreshold + 1, threatModerateThreshold + randomInt(-1, 2));
        
        // Mutate defense weights
        mutated.defenseWeightNoThreat = Math.max(0, defenseWeightNoThreat + randomFloat(-0.5f, 0.5f));
        mutated.defenseWeightMinor = Math.max(0, defenseWeightMinor + randomFloat(-0.5f, 0.5f));
        mutated.defenseWeightModerate = Math.max(0, defenseWeightModerate + randomFloat(-0.5f, 0.5f));
        mutated.defenseWeightHigh = Math.max(0, defenseWeightHigh + randomFloat(-0.5f, 0.5f));
        
        // Mutate action card values
        mutated.actionCardDailyScrum = Math.max(0, actionCardDailyScrum + randomFloat(-1.0f, 1.0f));
        mutated.actionCardBacklog = Math.max(0, actionCardBacklog + randomFloat(-1.0f, 1.0f));
        mutated.actionCardIpo = Math.max(0, actionCardIpo + randomFloat(-1.0f, 1.0f));
        
        return mutated;
    }
    
    private static float randomVariation(float rate) {
        // Returns value between (1 - rate) and (1 + rate)
        return 1.0f + (float)(Math.random() * 2 * rate - rate);
    }
    
    private static int randomInt(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
    }
    
    private static float randomFloat(float min, float max) {
        return min + (float)Math.random() * (max - min);
    }
    
    @Override
    public String toString() {
        return String.format(
            "WeightConfig{circulation=%.2f, bridging=%.2f, attack=%.2f, money=%.2f, defense=%.2f, points=%.2f}",
            circulation, bridging, attack, money, defense, points
        );
    }
}
