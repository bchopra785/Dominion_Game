package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.rating.optimization.WeightConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Strategy player for Milestone 3 Story 2.
 *
 */
public class WeightedPlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final Map<Card.Type, Integer> acquiredCards = new HashMap<>();  // Tracks both gained and purchased cards
    private static final Map<String, List<String>> referenceCards = new HashMap<>();  // Reference map
    private static final Map<String, Float> weights = new HashMap<>();  // Reference map (static defaults)
    private final Map<Card.Type, Integer> opponentAttackCards = new HashMap<>();  // Tracks opponent attack card acquisitions
    private int lastProcessedEventCount = 0;  // Track which events we've already processed
    private final String playerName;  // Store player name to identify own events

    // Instance-level configurable weights (override static defaults if set)
    private Map<String, Float> instanceWeights = null;
    private Integer instanceThreatMinorThreshold = null;
    private Integer instanceThreatModerateThreshold = null;
    private Float instanceDefenseWeightNoThreat = null;
    private Float instanceDefenseWeightMinor = null;
    private Float instanceDefenseWeightModerate = null;
    private Float instanceDefenseWeightHigh = null;

    // Threat level thresholds and weights (evolved from optimization)
    private static final int THREAT_MINOR_THRESHOLD = 3;        // Optimized from 2 - higher tolerance for minor threats
    private static final int THREAT_MODERATE_THRESHOLD = 5;     // Back to original - good threshold for moderate threat
    private static final float DEFENSE_WEIGHT_NO_THREAT = 0.0f;
    private static final float DEFENSE_WEIGHT_MINOR = 1.0f;
    private static final float DEFENSE_WEIGHT_MODERATE = 2.0f;
    private static final float DEFENSE_WEIGHT_HIGH = 3.0f;
    
    // Action card effect values (estimate of cards drawn or actions enabled)
    // Used in ACTION phase to prioritize which actions to play
    private static final Map<Card.Type, Float> actionCardValues = new HashMap<>();
    
    // Priority orderings for card acquisition within categories
    private static final Map<Card.Type, Integer> pointsPriority = new HashMap<>();
    private static final Map<Card.Type, Integer> moneyPriority = new HashMap<>();

    // Static initializer block - runs once when class loads
    static {
        referenceCards.put("money", Arrays.asList("BITCOIN", "ETHEREUM", "DOGECOIN"));
        referenceCards.put("points", Arrays.asList("FRAMEWORK", "MODULE", "METHOD"));
        referenceCards.put("bridging", Arrays.asList("IPO", "CODE_REVIEW", "SPRINT_PLANNING", "DEPLOYMENT_PIPELINE", "TECH_DEBT", "UNIT_TEST"));
        referenceCards.put("attack", Arrays.asList("EVERGREEN_TEST", "HACK", "RANSOMWARE"));
        referenceCards.put("defense", Arrays.asList("MONITORING", "MERGE_CONFLICT", "REFACTOR"));
        referenceCards.put("circulation", Arrays.asList("BACKLOG", "DAILY_SCRUM", "PARALLELIZATION"));

        // Weight strategy: evolved from medium optimization with random card selection
        // Achieved 68% win rate (1,050 games across 7 generations, 6 configs, 25 games each)
        // Strategy emphasizes circulation (flexibility) over bridging (reliance on specific cards)
        weights.put("circulation", 5.50f);      // INCREASED - maximize flexibility in random decks
        weights.put("bridging", 1.61f);         // REDUCED - can't rely on expensive draw cards
        weights.put("attack", 1.42f);           // Low - attack cards less effective
        weights.put("money", 1.92f);            // Moderate - resource production important
        weights.put("defense", 0.99f);          // Low - minimal defense needed
        weights.put("points", 0.46f);           // Very low - endgame not priority
        
        // Action card values (estimate of effectiveness for drawing/chaining)
        // Circulation cards (most valuable - enable filtering/cycling)
        actionCardValues.put(Card.Type.DAILY_SCRUM, 5.0f);        // High card draw
        actionCardValues.put(Card.Type.BACKLOG, 5.0f);            // High card draw
        actionCardValues.put(Card.Type.PARALLELIZATION, 4.0f);    // Good card draw
        
        // Bridging cards (very valuable - draw cards and enable chains)
        actionCardValues.put(Card.Type.IPO, 4.5f);               // Card draw + actions
        actionCardValues.put(Card.Type.CODE_REVIEW, 4.0f);       // Card draw
        actionCardValues.put(Card.Type.SPRINT_PLANNING, 3.5f);   // Card draw + actions
        actionCardValues.put(Card.Type.DEPLOYMENT_PIPELINE, 3.0f); // Card draw + tempo
        actionCardValues.put(Card.Type.TECH_DEBT, 2.5f);         // Minor card draw
        actionCardValues.put(Card.Type.UNIT_TEST, 2.5f);         // Minor card draw
        
        // Attack/Defense (situational)
        actionCardValues.put(Card.Type.EVERGREEN_TEST, 2.0f);    // Attack
        actionCardValues.put(Card.Type.HACK, 2.0f);              // Attack
        actionCardValues.put(Card.Type.RANSOMWARE, 2.0f);        // Attack
        actionCardValues.put(Card.Type.MONITORING, 1.5f);        // Defense
        actionCardValues.put(Card.Type.MERGE_CONFLICT, 1.5f);    // Defense
        actionCardValues.put(Card.Type.REFACTOR, 1.5f);          // Defense
        
        // Priority orderings for card acquisition (lower number = higher priority)
        pointsPriority.put(Card.Type.FRAMEWORK, 1);  // Best points card
        pointsPriority.put(Card.Type.MODULE, 2);     // Second best
        pointsPriority.put(Card.Type.METHOD, 3);     // Third best
        
        moneyPriority.put(Card.Type.DOGECOIN, 1);    // Best money card
        moneyPriority.put(Card.Type.ETHEREUM, 2);    // Second best
        moneyPriority.put(Card.Type.BITCOIN, 3);     // Third best
    }


    public WeightedPlayer() {
        super("WeightedPlayer-" + COUNTER.getAndIncrement());
        this.playerName = super.toString();
    }

    public WeightedPlayer(String name) {
        super(name);
        this.playerName = name;
    }

    /**
     * Create a WeightedPlayer with custom weight configuration for optimization.
     */
    public WeightedPlayer(String name, WeightConfig config) {
        super(name);
        this.playerName = name;
        if (config != null) {
            applyWeightConfig(config);
        }
    }

    /**
     * Apply a WeightConfig to this player instance, overriding default weights.
     */
    private void applyWeightConfig(WeightConfig config) {
        // Create instance-level weight maps from config
        this.instanceWeights = new HashMap<>();
        this.instanceWeights.put("circulation", config.circulation);
        this.instanceWeights.put("bridging", config.bridging);
        this.instanceWeights.put("attack", config.attack);
        this.instanceWeights.put("money", config.money);
        this.instanceWeights.put("defense", config.defense);
        this.instanceWeights.put("points", config.points);
        
        // Store threat thresholds
        this.instanceThreatMinorThreshold = config.threatMinorThreshold;
        this.instanceThreatModerateThreshold = config.threatModerateThreshold;
        
        // Store defense weights
        this.instanceDefenseWeightNoThreat = config.defenseWeightNoThreat;
        this.instanceDefenseWeightMinor = config.defenseWeightMinor;
        this.instanceDefenseWeightModerate = config.defenseWeightModerate;
        this.instanceDefenseWeightHigh = config.defenseWeightHigh;
    }

    /**
     * Get weight for a category, using instance config if available, otherwise defaults.
     */
    private float getWeight(String category) {
        if (instanceWeights != null) {
            Float weight = instanceWeights.get(category);
            if (weight != null) {
                return weight;
            }
        }
        Float weight = weights.get(category);
        return weight != null ? weight : 0.0f;
    }

    /**
     * Get threat minor threshold, using instance config if available.
     */
    private int getThreatMinorThreshold() {
        return instanceThreatMinorThreshold != null ? instanceThreatMinorThreshold : THREAT_MINOR_THRESHOLD;
    }

    /**
     * Get threat moderate threshold, using instance config if available.
     */
    private int getThreatModerateThreshold() {
        return instanceThreatModerateThreshold != null ? instanceThreatModerateThreshold : THREAT_MODERATE_THRESHOLD;
    }

    /**
     * Get defense weight for threat level, using instance config if available.
     */
    private float getDefenseWeight(String threatLevel) {
        switch (threatLevel) {
            case "none":
                return instanceDefenseWeightNoThreat != null ? instanceDefenseWeightNoThreat : DEFENSE_WEIGHT_NO_THREAT;
            case "minor":
                return instanceDefenseWeightMinor != null ? instanceDefenseWeightMinor : DEFENSE_WEIGHT_MINOR;
            case "moderate":
                return instanceDefenseWeightModerate != null ? instanceDefenseWeightModerate : DEFENSE_WEIGHT_MODERATE;
            case "high":
                return instanceDefenseWeightHigh != null ? instanceDefenseWeightHigh : DEFENSE_WEIGHT_HIGH;
            default:
                return 0.0f;
        }
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        updateCardCount(); // Keep track of cards gained so far
        updateDefenseWeightBasedOnOpponents();  // Adjust defense weight based on opponent threats
        if (state == null || options == null || options.isEmpty()) {
            return null;
        }

        if (state.phase() == GameState.TurnPhase.ACTION) {
            return chooseActionDecision(state, options);
        }

        if (state.phase() == GameState.TurnPhase.MONEY) {
            return chooseMoneyDecision(state, options);
        }

        if (state.phase() == GameState.TurnPhase.BUY) {
            return chooseBuyDecision(state, options);
        }

        // Check if this is a trash decision (not tied to a specific phase)
        if (!options.isEmpty() && options.get(0) instanceof TrashCardDecision) {
            return chooseTrashDecision(options);
        }

        // Check if this is a discard decision (not tied to a specific phase)
        if (!options.isEmpty() && options.get(0) instanceof DiscardCardDecision) {
            return chooseDiscardDecision(options);
        }

        // Check if this is a gain card decision (not tied to a specific phase)
        if (!options.isEmpty() && options.get(0) instanceof GainCardDecision) {
            return chooseGainCardDecision(options);
        }

        return findBestFallback(options, state.phase());
    }

    /**
     * Trash strategy: Prioritize trashing the BUG card, fall back to first option.
     */
    public Decision chooseTrashDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        // Try to find and trash the BUG card first
        for (Decision option : options) {
            if (option instanceof TrashCardDecision) {
                Card cardToTrash = ((TrashCardDecision) option).card();
                if (cardToTrash.type() == Card.Type.BUG) {
                    return option;
                }
            }
        }

        // Fallback: return first trash option
        return options.get(0);
    }

    /**
     * Discard strategy: Prioritize discarding in this order:
     * 1. BUG card
     * 2. Point cards (METHOD, MODULE, FRAMEWORK)
     * 3. Fallback to first option
     */
    public Decision chooseDiscardDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        // Priority 1: Try to find and discard the BUG card first
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision) {
                Card cardToDiscard = ((DiscardCardDecision) option).card();
                if (cardToDiscard.type() == Card.Type.BUG) {
                    return option;
                }
            }
        }

        // Priority 2: Try to discard point cards (low-value)
        List<Card.Type> pointCards = Arrays.asList(Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK);
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision) {
                Card cardToDiscard = ((DiscardCardDecision) option).card();
                if (pointCards.contains(cardToDiscard.type())) {
                    return option;
                }
            }
        }

        // Fallback: return first discard option
        return options.get(0);
    }

    /**
     * Gain card strategy: Use weight-based logic to select best card.
     * Select the card with the highest weight, with diminishing returns for duplicates.
     */
    public Decision chooseGainCardDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        Decision bestDecision = null;
        Float bestScore = -1.0f;

        // Evaluate each option by weight
        for (Decision option : options) {
            if (!(option instanceof GainCardDecision)) {
                continue;
            }

            Card.Type cardType = ((GainCardDecision) option).cardType();
            if (cardType == null) {
                continue;
            }

            // Get weight for this card
            String cardTypeKey = cardType.toString();
            Float weight = getWeight(cardTypeKey);

            // Decrement score if we already have many of this card (diminishing returns for duplicates)
            int currentCount = acquiredCards.getOrDefault(cardType, 0);
            float score = weight / (1.0f + (currentCount * 0.5f));

            if (score > bestScore) {
                bestScore = score;
                bestDecision = option;
            }
        }

        // If we found a good card to gain, return it
        if (bestDecision != null) {
            return bestDecision;
        }

        // Fallback: return first option
        return options.get(0);
    }

    /**
     * ACTION phase strategy: Lookahead search to find the action sequence that maximizes card draw.
     * Simulates different action combinations tracking actual hand state.
     * Assumes drawn cards are points (useless), so prioritizes maximizing draws over money.
     */
    public Decision chooseActionDecision(GameState state, ImmutableList<Decision> options) {
        if (state == null || options == null || options.isEmpty()) {
            return findBestFallback(options, GameState.TurnPhase.ACTION);
        }
        
        // Start with simulated hand - track which cards are played
        List<Card> simulatedHand = new ArrayList<>();
        // We don't have direct access to hand contents, so we track via PlayCardDecisions
        // in the options list
        
        ActionSequenceResult best = findBestActionSequence(simulatedHand, options, 0, state.availableActions());
        
        // Play the first card from the best sequence
        if (best.firstCardToPlay != null) {
            return best.firstCardToPlay;
        }
        
        // Fallback: end the ACTION phase if no good sequence found
        return findBestFallback(options, GameState.TurnPhase.ACTION);
    }
    
    /**
     * Result class for action sequence exploration
     */
    private static class ActionSequenceResult {
        Decision firstCardToPlay;
        int totalCardsDrawn;
        int totalMoneyGenerated;
        
        ActionSequenceResult(Decision card, int drawn, int money) {
            this.firstCardToPlay = card;
            this.totalCardsDrawn = drawn;
            this.totalMoneyGenerated = money;
        }
        
        // Score function: prioritize cards drawn, secondary score is money
        int getScore() {
            return totalCardsDrawn * 100 + totalMoneyGenerated;
        }
    }
    
    /**
     * Recursively find the best action sequence by exploring combinations.
     * Checks actual playable cards from options to ensure they're really available.
     */
    private ActionSequenceResult findBestActionSequence(List<Card> simulatedHand, 
                                                       ImmutableList<Decision> options, 
                                                       int depth, 
                                                       int actionsRemaining) {
        // Base cases
        if (depth > 10 || actionsRemaining <= 0 || options == null || options.isEmpty()) {
            return new ActionSequenceResult(null, 0, 0);
        }
        
        ActionSequenceResult bestOutcome = new ActionSequenceResult(null, 0, 0);
        
        // Try each playable action card that's actually in the options (confirmed available)
        for (Decision option : options) {
            if (!(option instanceof PlayCardDecision)) {
                continue;
            }
            
            Card card = ((PlayCardDecision) option).card();
            Card.Type cardType = card.type();
            
            // Skip non-action cards
            if (!isActionCard(cardType)) {
                continue;
            }
            
            // Estimate what this card generates (based on card type, not simulation)
            int cardsDrawn = estimateCardsDraw(cardType);
            int moneyGen = estimateMoney(cardType);
            int newActionsFromCard = estimateActionsGenerated(cardType);
            
            // Simulate playing this card - create new options by removing this card
            List<Decision> updatedOptionsList = new ArrayList<>(options);
            updatedOptionsList.remove(option);  // This card is now played
            
            // Calculate remaining actions
            int projectedActionsRemaining = (actionsRemaining - 1) + newActionsFromCard;
            
            // Recursively find best follow-up with updated options
            // Convert List back to ImmutableList for recursive call
            ImmutableList<Decision> updatedOptions = ImmutableList.copyOf(updatedOptionsList);
            ActionSequenceResult futureOutcome = findBestActionSequence(simulatedHand, updatedOptions, depth + 1, projectedActionsRemaining);
            
            // Total outcome from playing this card
            int totalDrawn = cardsDrawn + futureOutcome.totalCardsDrawn;
            int totalMoney = moneyGen + futureOutcome.totalMoneyGenerated;
            
            ActionSequenceResult thisOutcome = new ActionSequenceResult(option, totalDrawn, totalMoney);
            
            // Update best if this is better
            if (thisOutcome.getScore() > bestOutcome.getScore()) {
                bestOutcome = thisOutcome;
            }
        }
        
        return bestOutcome;
    }
    
    /**
     * Check if a card type is an action card (can be played in ACTION phase)
     */
    private boolean isActionCard(Card.Type cardType) {
        List<String> bridgingCards = referenceCards.get("bridging");
        List<String> circulationCards = referenceCards.get("circulation");
        
        String cardName = cardType.toString();
        return (bridgingCards != null && bridgingCards.contains(cardName)) ||
               (circulationCards != null && circulationCards.contains(cardName));
    }
    
    /**
     * Estimate how many cards this action card draws
     */
    private int estimateCardsDraw(Card.Type cardType) {
        // Based on card type, return estimated cards drawn
        switch (cardType) {
            case DAILY_SCRUM: return 3;           // High draw
            case BACKLOG: return 3;               // High draw
            case PARALLELIZATION: return 2;      // Good draw
            case IPO: return 2;                   // Good draw
            case CODE_REVIEW: return 2;          // Good draw
            case SPRINT_PLANNING: return 2;      // Decent draw
            case DEPLOYMENT_PIPELINE: return 1;  // Some draw
            case TECH_DEBT: return 1;            // Minimal draw
            case UNIT_TEST: return 1;            // Minimal draw
            default: return 0;
        }
    }
    
    /**
     * Estimate how much money this action card generates
     */
    private int estimateMoney(Card.Type cardType) {
        switch (cardType) {
            case DAILY_SCRUM: return 0;          // No money
            case BACKLOG: return 0;              // No money
            case PARALLELIZATION: return 0;     // No money
            case IPO: return 2;                  // Some money
            case CODE_REVIEW: return 1;         // Little money
            case SPRINT_PLANNING: return 1;     // Little money
            case DEPLOYMENT_PIPELINE: return 2; // Some money
            case TECH_DEBT: return 0;           // No money
            case UNIT_TEST: return 0;           // No money
            default: return 0;
        }
    }
    
    /**
     * Estimate how many action points this card generates
     */
    private int estimateActionsGenerated(Card.Type cardType) {
        switch (cardType) {
            case SPRINT_PLANNING: return 1;      // Generates extra action
            case IPO: return 1;                  // Generates extra action
            case CODE_REVIEW: return 0;         // No extra actions
            case DAILY_SCRUM: return 0;         // No extra actions
            case BACKLOG: return 0;             // No extra actions
            case PARALLELIZATION: return 0;     // No extra actions
            case DEPLOYMENT_PIPELINE: return 1; // Generates extra action
            case TECH_DEBT: return 0;           // No extra actions
            case UNIT_TEST: return 0;           // No extra actions
            default: return 0;
        }
    }


    public Decision chooseMoneyDecision(GameState state, ImmutableList<Decision> options) {
        // Play any available money card (will repeat until all are played)
        // no strategic benefit to not playing money cards
        for (Decision option : options) {
            if (!(option instanceof PlayCardDecision)) {
                continue;
            }

            Card playedCard = ((PlayCardDecision) option).card();
            if (playedCard.type().category() == Card.Type.Category.MONEY) {
                return option;
            }
        }

        return findBestFallback(options, GameState.TurnPhase.MONEY);
    }

    /**
    * Weight-based BUY phase logic: Buy cards based on weight priorities and what's missing from deck.
    * Strategy: Prioritize categories with highest weights, prefer acquiring cards from under-represented categories.
     */
    public Decision chooseBuyDecision(GameState state, ImmutableList<Decision> options) {
        // PRIORITY 1: Always buy FRAMEWORK if available (victory points matter late game)
        for (Decision option : options) {
            Card.Type cardType = getTypeFromDecision(option);
            if (cardType == Card.Type.FRAMEWORK) {
                return option;
            }
        }
        
        Decision bestDecision = null;
        Float bestScore = -1.0f;
        
        // Evaluate each option
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision && ((EndPhaseDecision) option).phase() == GameState.TurnPhase.BUY) {
                // Lower priority than buying cards, but keep as fallback
                continue;
            }
            
            // Get card type from decision
            Card.Type cardType = getTypeFromDecision(option);
            if (cardType == null) {
                continue;
            }
            
            // Find which category this card belongs to
            String category = findCardCategory(cardType);
            if (category == null) {
                continue;
            }
            
            // Calculate score: base weight + priority bonus for within-category preference
            Float weight = getWeight(category);
            
            // Add priority bonus: prefer better cards within each category
            float priorityBonus = 0.0f;
            if ("money".equals(category)) {
                Integer priority = moneyPriority.get(cardType);
                if (priority != null) {
                    priorityBonus = (4.0f - priority) * 0.1f;  // 0.3, 0.2, 0.1 for priorities 1, 2, 3
                }
            } else if ("points".equals(category)) {
                Integer priority = pointsPriority.get(cardType);
                if (priority != null) {
                    priorityBonus = (4.0f - priority) * 0.1f;  // 0.3, 0.2, 0.1 for priorities 1, 2, 3
                }
            }
            
            // Decrement score if we already have many of this card
            int currentCount = acquiredCards.getOrDefault(cardType, 0);
            float score = (weight + priorityBonus) / (1.0f + (currentCount * 0.5f));  // Diminishing returns for duplicates
            
            if (score > bestScore) {
                bestScore = score;
                bestDecision = option;
            }
        }
        
        // If we found a good card to buy, return it
        if (bestDecision != null) {
            return bestDecision;
        }
        
        // Fallback: try to end the BUY phase
        for (Decision d : options) {
            if (d instanceof EndPhaseDecision && ((EndPhaseDecision) d).phase() == GameState.TurnPhase.BUY) {
                return d;
            }
        }
        
        // Last resort: return first option
        return options.get(0);
    }
    
    /**
     * Dynamically adjust defense weight based on opponent threat level.
     * Monitors opponent card acquisitions through observer events.
     * If opponents acquire attack cards, increase defense weight.
     */
    private void updateDefenseWeightBasedOnOpponents() {
        if (!getObserver().isPresent()) {
            return;
        }
        
        RecordingGameObserver observer = (RecordingGameObserver) getObserver().get();
        List<Event> events = observer.getEventsSnapshot();
        List<String> attackCards = referenceCards.get("attack");
        
        // Only process new events since last check
        for (int i = lastProcessedEventCount; i < events.size(); i++) {
            Event event = events.get(i);
            
            if (event instanceof GainCardEvent) {
                GainCardEvent gainEvent = (GainCardEvent) event;
                String gainedByPlayer = gainEvent.playerName();
                
                // Skip our own cards
                if (gainedByPlayer.equals(this.playerName)) {
                    continue;
                }
                
                // Check if opponent gained an attack card
                Card.Type cardType = gainEvent.cardType();
                if (attackCards != null && attackCards.contains(cardType.toString())) {
                    opponentAttackCards.put(cardType, opponentAttackCards.getOrDefault(cardType, 0) + 1);
                }
            }
        }
        
        lastProcessedEventCount = events.size();
        
        // Adjust defense weight based on total opponent attacks acquired
        int totalOpponentAttacks = opponentAttackCards.values().stream().mapToInt(Integer::intValue).sum();
        
        float newDefenseWeight;
        if (totalOpponentAttacks == 0) {
            newDefenseWeight = getDefenseWeight("none");
        } else if (totalOpponentAttacks <= getThreatMinorThreshold()) {
            newDefenseWeight = getDefenseWeight("minor");
        } else if (totalOpponentAttacks <= getThreatModerateThreshold()) {
            newDefenseWeight = getDefenseWeight("moderate");
        } else {
            newDefenseWeight = getDefenseWeight("high");
        }
        
        // Update weights in both static map and instance map (if configured)
        weights.put("defense", newDefenseWeight);
        if (instanceWeights != null) {
            instanceWeights.put("defense", newDefenseWeight);
        }
    }

    /**
     * Find which category a card type belongs to
     */
    private String findCardCategory(Card.Type cardType) {
        for (String category : referenceCards.keySet()) {
            List<String> cardNames = referenceCards.get(category);
            if (cardNames.contains(cardType.toString())) {
                return category;
            }
        }
        return null;
    }

    // Helper: extract Card.Type from Decision if possible
    private Card.Type getTypeFromDecision(Decision d) {
        if (d instanceof PlayCardDecision) {
            return ((PlayCardDecision) d).card().type();
        }
        // Prefer direct type check for BuyDecision if available
        if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
            return ((edu.brandeis.cosi.atg.decisions.BuyDecision) d).cardType();
        }
        return null;
    }

    private Decision findBestFallback(ImmutableList<Decision> options, GameState.TurnPhase preferredPhase) {
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                EndPhaseDecision end = (EndPhaseDecision) option;
                if (end.phase() == preferredPhase) {
                    return option;
                }
            }
        }

        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        return options.get(0);
    }

    /**
     * Updates the card count by analyzing observed GainCardEvent events.
     * This tracks all cards acquired by the player through any means:
     * - Cards gained from card-draw actions (DAILY_SCRUM, IPO)
     * - Cards purchased in the BUY phase
     */
    private void updateCardCount() {
        if (!getObserver().isPresent()) {
            return;
        }
        
        RecordingGameObserver observer = (RecordingGameObserver) getObserver().get();
        List<Event> events = observer.getEventsSnapshot();
        
        for (Event event : events) {
            if (event instanceof GainCardEvent) {
                GainCardEvent gainEvent = (GainCardEvent) event;
                Card.Type cardType = gainEvent.cardType();
                acquiredCards.put(cardType, acquiredCards.getOrDefault(cardType, 0) + 1);
            }
        }
    }

    /**
     * Returns the count of a specific card type that has been acquired (gained or purchased).
     */
    public int getAcquiredCardCount(Card.Type cardType) {
        return acquiredCards.getOrDefault(cardType, 0);
    }

    /**
     * Returns all acquired cards (gained or purchased) tracked so far.
     */
    public Map<Card.Type, Integer> getAcquiredCards() {
        return new HashMap<>(acquiredCards);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }
}

