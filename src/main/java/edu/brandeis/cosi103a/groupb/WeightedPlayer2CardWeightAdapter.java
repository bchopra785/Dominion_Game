package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CardWeights;

/**
 * Adapter for WeightedPlayer2 that uses CardWeightConfig for individual card weights.
 * This allows WeightedPlayer2 to be optimized with per-card weights instead of category weights.
 */
public class WeightedPlayer2CardWeightAdapter extends WeightedPlayer2 {
    
    private final CardWeights cardWeightConfig;
    
    public WeightedPlayer2CardWeightAdapter(String name, CardWeights cardWeightConfig) {
        super(name);
        this.cardWeightConfig = cardWeightConfig;
    }
    
    /**
     * Override weight retrieval to use individual card weights from the config.
     */
    @Override
    protected float getWeight(Card.Type cardType) {
        if (cardWeightConfig != null && cardWeightConfig.cardWeights.containsKey(cardType)) {
            return cardWeightConfig.cardWeights.get(cardType);
        }
        // Fallback to default weights if not in config
        return super.getWeight(cardType);
    }
}
