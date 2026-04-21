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
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CardWeights;
import edu.brandeis.cosi103a.groupb.rating.optimization.data_classes.CategoryWeights;
import edu.brandeis.cosi103a.groupb.rating.optimization.optimizers.V3Optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Strategy player for Milestone 3 Story 2.
 *
 */
public class WeightedPlayer3 extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final Map<Card.Type, Integer> acquiredCards = new HashMap<>();  // Tracks both gained and purchased cards
    private static final Map<String, List<String>> referenceCards = new HashMap<>();  // Reference map for attack card detection
    private static final Map<Card.Type, Float> cardWeights = new HashMap<>();  // Individual card weights
    private static final Map<String, CardWeights> DECK_AWARE_WEIGHTS = new HashMap<>();  // Deck-aware optimized weights for each board config
    private final Map<Card.Type, Integer> opponentAttackCards = new HashMap<>();  // Tracks opponent attack card acquisitions
    private int lastProcessedEventCount = 0;  // Track which events we've already processed
    private final String playerName;  // Store player name to identify own events
    private boolean verbose = false;  // Control console output verbosity (default: false for optimization)
    
    // Deck-aware detection: track which action cards we've seen to detect the missing one
    private final java.util.Set<Card.Type> seenActionCards = new java.util.HashSet<>();
    private String detectedMissingActionCard = null;  // Cached once detected

    // Instance-level configurable weights (override static defaults if set)
    private Map<Card.Type, Float> instanceCardWeights = null;
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

    // Static initializer block - runs once when class loads
    static {
        referenceCards.put("circulation", Arrays.asList("DAILY_SCRUM", "BACKLOG", "PARALLELIZATION"));
        referenceCards.put("bridging", Arrays.asList("IPO", "CODE_REVIEW", "SPRINT_PLANNING", "UNIT_TEST"));
        referenceCards.put("attack", Arrays.asList("EVERGREEN_TEST", "HACK", "RANSOMWARE"));  // For threat detection
        
        // Individual card weights - optimized from 300-game evolutionary optimization (73.3% win rate)
        // Discovered through medium optimization: 4 generations × 5 configs × 15 games
        
        // Circulation cards (flexibility/card draw)
        cardWeights.put(Card.Type.DAILY_SCRUM, 3.62f);            // Excellent card draw
        cardWeights.put(Card.Type.BACKLOG, 3.376064f);            // Excellent card draw (optimized down slightly)
        cardWeights.put(Card.Type.PARALLELIZATION, 2.3897896f);   // Good card draw (optimized down)
        
        // Bridging cards (draw and actions)
        cardWeights.put(Card.Type.IPO, 2.93f);                    // Good draw + actions
        cardWeights.put(Card.Type.CODE_REVIEW, 2.9575925f);       // Strongest bridging card (optimized up!)
        cardWeights.put(Card.Type.SPRINT_PLANNING, 2.56f);        // Decent draw + action
        cardWeights.put(Card.Type.UNIT_TEST, 1.4571187f);         // Minor draw
        
        // Money cards (resources) - ETHEREUM now strongest
        cardWeights.put(Card.Type.BITCOIN, 1.936853f);            // Money card (optimized down)
        cardWeights.put(Card.Type.ETHEREUM, 2.4113789f);          // Strongest money card (optimized up!)
        cardWeights.put(Card.Type.DOGECOIN, 2.3845406f);          // Second strongest (optimized up)
        
        // Attack cards - HACK is strongest
        cardWeights.put(Card.Type.EVERGREEN_TEST, 2.1362183f);    // Attack (optimized down)
        cardWeights.put(Card.Type.HACK, 2.6675334f);              // Strongest attack (optimized up!)
        cardWeights.put(Card.Type.RANSOMWARE, 2.057161f);         // Attack (optimized down)
        
        // Defense cards - better diversity
        cardWeights.put(Card.Type.MONITORING, 1.67f);             // Defense
        cardWeights.put(Card.Type.REFACTOR, 1.8754771f);          // Defense (optimized up)
        
        // Point cards - better differentiation
        cardWeights.put(Card.Type.FRAMEWORK, 0.9208751f);         // Victory points (optimized down)
        cardWeights.put(Card.Type.MODULE, 0.5901168f);            // Points (optimized up)
        cardWeights.put(Card.Type.METHOD, 0.36834785f);           // Points (optimized down)
        
        // Try to load deck-aware weights from file (if they've been optimized)
        try {
            String configFile = "deckaware_weights.txt";
            Map<String, CardWeights> loadedConfigs = V3Optimizer.loadConfigsFromFile(configFile);
            DECK_AWARE_WEIGHTS.putAll(loadedConfigs);
            // System.err.println("[WeightedPlayer3 DEBUG] Loaded " + DECK_AWARE_WEIGHTS.size() + " deck-aware weight configurations");
        } catch (Exception e) {
            // File doesn't exist yet (first run) - that's fine, we'll use defaults
            // System.err.println("[WeightedPlayer3 DEBUG] No deck-aware weights file found, using default weights");
        }
    }


    public WeightedPlayer3() {
        super("WeightedPlayer-" + COUNTER.getAndIncrement());
        this.playerName = super.toString();
        this.verbose = false;
    }

    public WeightedPlayer3(String name) {
        super(name);
        this.playerName = name;
        this.verbose = false;
    }

    public WeightedPlayer3(String name, boolean verbose) {
        super(name);
        this.playerName = name;
        this.verbose = verbose;
    }

    /**
     * Create a WeightedPlayer with custom weight configuration for optimization.
     */
    public WeightedPlayer3(String name, CategoryWeights config) {
        super(name);
        this.playerName = name;
        this.verbose = false;
        if (config != null) {
            applyWeightConfig(config);
        }
    }

    /**
     * Create a WeightedPlayer with custom weight configuration and verbose control.
     */
    public WeightedPlayer3(String name, CategoryWeights config, boolean verbose) {
        super(name);
        this.playerName = name;
        this.verbose = verbose;
        if (config != null) {
            applyWeightConfig(config);
        }
    }

    /**
     * Apply a WeightConfig to this player instance, overriding default weights.
     */
    private void applyWeightConfig(CategoryWeights config) {
        // Create instance-level weight map from config - converting category weights to per-card weights
        this.instanceCardWeights = new HashMap<>();
        
        // Map each card to its corresponding category weight from config
        // Circulation cards
        float circulationWeight = config.circulation;
        this.instanceCardWeights.put(Card.Type.DAILY_SCRUM, circulationWeight);
        this.instanceCardWeights.put(Card.Type.BACKLOG, circulationWeight);
        this.instanceCardWeights.put(Card.Type.PARALLELIZATION, circulationWeight * 0.75f);  // Slightly lower
        
        // Bridging cards
        float bridgingWeight = config.bridging;
        this.instanceCardWeights.put(Card.Type.IPO, bridgingWeight);
        this.instanceCardWeights.put(Card.Type.CODE_REVIEW, bridgingWeight);
        this.instanceCardWeights.put(Card.Type.SPRINT_PLANNING, bridgingWeight);
        this.instanceCardWeights.put(Card.Type.DEPLOYMENT_PIPELINE, bridgingWeight * 0.6f);
        this.instanceCardWeights.put(Card.Type.TECH_DEBT, bridgingWeight * 0.4f);
        this.instanceCardWeights.put(Card.Type.UNIT_TEST, bridgingWeight * 0.4f);
        
        // Money cards - preserve hierarchy (DOGECOIN > ETHEREUM > BITCOIN)
        float moneyWeight = config.money;
        this.instanceCardWeights.put(Card.Type.DOGECOIN, moneyWeight * 1.015f);  // Best money card
        this.instanceCardWeights.put(Card.Type.ETHEREUM, moneyWeight);           // Second best
        this.instanceCardWeights.put(Card.Type.BITCOIN, moneyWeight * 0.985f);   // Third best
        
        // Attack cards
        float attackWeight = config.attack;
        this.instanceCardWeights.put(Card.Type.EVERGREEN_TEST, attackWeight);
        this.instanceCardWeights.put(Card.Type.HACK, attackWeight);
        this.instanceCardWeights.put(Card.Type.RANSOMWARE, attackWeight);
        
        // Defense cards
        float defenseWeight = config.defense;
        this.instanceCardWeights.put(Card.Type.MONITORING, defenseWeight);
        this.instanceCardWeights.put(Card.Type.MERGE_CONFLICT, defenseWeight);
        this.instanceCardWeights.put(Card.Type.REFACTOR, defenseWeight);
        
        // Points cards - preserve hierarchy (FRAMEWORK > MODULE > METHOD)
        float pointsWeight = config.points;
        this.instanceCardWeights.put(Card.Type.FRAMEWORK, pointsWeight * 1.02f);   // Best points card
        this.instanceCardWeights.put(Card.Type.MODULE, pointsWeight * 1.01f);      // Second best
        this.instanceCardWeights.put(Card.Type.METHOD, pointsWeight);              // Third best
        
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
     * Get weight for a specific card, using instance config if available, otherwise defaults.
     * If the missing action card has been detected, uses deck-aware weights optimized for that board.
     * Protected so subclasses can override for custom weight strategies.
     */
    protected float getWeight(Card.Type cardType) {
        if (instanceCardWeights != null) {
            Float weight = instanceCardWeights.get(cardType);
            if (weight != null) {
                return weight;
            }
        }
        
        // Use deck-aware weights if we've detected the missing card
        if (detectedMissingActionCard != null) {
            Map<Card.Type, Float> deckWeights = getDeckAwareWeights(detectedMissingActionCard);
            Float deckWeight = deckWeights.get(cardType);
            if (deckWeight != null) {
                if (verbose) {
                    System.out.println("[" + playerName + "] Using DECK-AWARE weight for " + cardType + ": " + deckWeight + " (config: " + detectedMissingActionCard + ")");
                }
                return deckWeight;
            }
        }
        
        Float weight = cardWeights.get(cardType);
        if (verbose && detectedMissingActionCard != null) {
            System.out.println("[" + playerName + "] Using DEFAULT weight for " + cardType + ": " + weight);
        }
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
        try {
            updateCardCount(); // Keep track of cards gained so far
            updateDefenseWeightBasedOnOpponents();  // Adjust defense weight based on opponent threats
            
            // Detect deck configuration on first turn (when we first see the board)
            if (detectedMissingActionCard == null && state != null) {
                detectBoardConfiguration(state);
            }
            
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
        } catch (Exception e) {
            System.err.println("ERROR in " + playerName + " makeDecision: " + e.getMessage());
            e.printStackTrace();
            return (options != null && !options.isEmpty()) ? options.get(0) : null;
        }
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
                    if (verbose) {
                        System.out.println(playerName + " TRASHING BUG card");
                    }
                    return option;
                }
            }
        }

        // Fallback: return first trash option
        if (verbose) {
            System.out.println(playerName + " No BUG card to trash, using fallback");
        }
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
                    if (verbose) {
                        System.out.println(playerName + " DISCARDING BUG card");
                    }
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
                    if (verbose) {
                        System.out.println(playerName + " DISCARDING point card: " + cardToDiscard.type());
                    }
                    return option;
                }
            }
        }

        // Fallback: return first discard option
        if (verbose) {
            System.out.println(playerName + " No BUG or point cards to discard, using fallback");
        }
        return options.get(0);
    }

    /**
     * Gain card strategy: Use same weight-based logic as buying.
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

            // Get weight for this specific card
            Float weight = getWeight(cardType);

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
            if (verbose) {
                Card.Type gainType = ((GainCardDecision) bestDecision).cardType();
                System.out.println(playerName + " GAINING: " + gainType + " (weight: " + bestScore + ")");
            }
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

    /**
     * Detect which 2 action cards are missing from the board by tracking seen cards.
     * The board randomly selects 10 of 12 possible action cards each game (5 out of 12 are missing).
     * This method tracks all action cards seen in buyableCards() and finds which 2 are NOT present.
     * 
     * @param state Current game state with buyableCards available
     * @return A sorted pair key like "BACKLOG_CODE_REVIEW" representing the first 2 missing cards, or null if not yet determined
     */
    private String detectMissingActionCard(GameState state) {
        // If already detected, return cached result
        if (detectedMissingActionCard != null) {
            return detectedMissingActionCard;
        }
        
        // Add any action cards from buyableCards to our seen set
        if (state != null && state.buyableCards() != null) {
            for (Card.Type cardType : state.buyableCards().getCardTypes()) {
                // Check if this is an action card (not money, not victory)
                Card.Type.Category category = cardType.category();
                if (category != Card.Type.Category.MONEY && category != Card.Type.Category.VICTORY) {
                    seenActionCards.add(cardType);
                }
            }
        }
        
        // List of the 12 action cards we consider (removed: DEPLOYMENT_PIPELINE, TECH_DEBT, MERGE_CONFLICT)
        Card.Type[] allActionCards = {
            Card.Type.BACKLOG, Card.Type.CODE_REVIEW, Card.Type.SPRINT_PLANNING, Card.Type.UNIT_TEST,
            Card.Type.HACK, Card.Type.REFACTOR, Card.Type.PARALLELIZATION, Card.Type.MONITORING,
            Card.Type.IPO, Card.Type.EVERGREEN_TEST, Card.Type.DAILY_SCRUM, Card.Type.RANSOMWARE
        };
        
        // Find which cards are NOT in our seen set
        List<Card.Type> missingCards = new ArrayList<>();
        for (Card.Type actionCard : allActionCards) {
            if (!seenActionCards.contains(actionCard)) {
                missingCards.add(actionCard);
            }
        }
        
        // If we've found at least 1 missing card, generate a key from the first 2
        if (missingCards.size() >= 1) {
            // Sort the missing cards for consistent key generation
            missingCards.sort((a, b) -> a.toString().compareTo(b.toString()));
            
            // Create key using first 2 missing cards (or just 1 if only 1 found)
            if (missingCards.size() >= 2) {
                detectedMissingActionCard = missingCards.get(0).toString() + "_" + missingCards.get(1).toString();
                // System.err.println("[WeightedPlayer3 DEBUG] " + playerName + " detected missing action card pair: " + detectedMissingActionCard);
                if (verbose) {
                    System.out.println(playerName + " detected missing action card pair: " + detectedMissingActionCard);
                }
            } else {
                // If we've only seen 11 cards, just use the 1 missing card as key
                detectedMissingActionCard = missingCards.get(0).toString();
                System.err.println("[WeightedPlayer3 DEBUG] " + playerName + " detected missing action card: " + detectedMissingActionCard);
                if (verbose) {
                    System.out.println(playerName + " detected missing action card: " + detectedMissingActionCard);
                }
            }
            return detectedMissingActionCard;
        }
        
        return null;  // Not yet determined (we haven't seen enough cards)
    }
    
    /**
     * Detect the board configuration by comparing available cards to all 12 action cards.
     * Call this early in the game to identify which cards are missing.
     */
    private void detectBoardConfiguration(GameState state) {
        if (state == null || state.buyableCards() == null) {
            return;
        }
        
        // List of all 12 action cards
        Set<Card.Type> allActionCards = new java.util.HashSet<>();
        allActionCards.add(Card.Type.BACKLOG);
        allActionCards.add(Card.Type.CODE_REVIEW);
        allActionCards.add(Card.Type.SPRINT_PLANNING);
        allActionCards.add(Card.Type.UNIT_TEST);
        allActionCards.add(Card.Type.HACK);
        allActionCards.add(Card.Type.REFACTOR);
        allActionCards.add(Card.Type.PARALLELIZATION);
        allActionCards.add(Card.Type.MONITORING);
        allActionCards.add(Card.Type.IPO);
        allActionCards.add(Card.Type.EVERGREEN_TEST);
        allActionCards.add(Card.Type.DAILY_SCRUM);
        allActionCards.add(Card.Type.RANSOMWARE);
        
        // Get cards available on the board
        Set<Card.Type> availableCards = new java.util.HashSet<>(state.buyableCards().getCardTypes());
        
        // Find which action cards are NOT available
        List<Card.Type> missingCards = new ArrayList<>();
        for (Card.Type actionCard : allActionCards) {
            if (!availableCards.contains(actionCard)) {
                missingCards.add(actionCard);
            }
        }
        
        // If we found exactly 2 missing cards, set the configuration key
        if (missingCards.size() == 2) {
            missingCards.sort((a, b) -> a.toString().compareTo(b.toString()));
            detectedMissingActionCard = missingCards.get(0).toString() + "_" + missingCards.get(1).toString();
            System.err.println("[WeightedPlayer3 DEBUG] Board config detected at game start: " + detectedMissingActionCard);
            if (verbose) {
                System.out.println(playerName + " detected board configuration: " + detectedMissingActionCard);
            }
        } else if (missingCards.size() == 1) {
            detectedMissingActionCard = missingCards.get(0).toString();
            System.err.println("[WeightedPlayer3 DEBUG] Board config detected (1 missing): " + detectedMissingActionCard);
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
    * Weight-based BUY phase logic: Buy cards based on individual card weights.
    * Each card has its own weight representing its strategic value.
     */
    public Decision chooseBuyDecision(GameState state, ImmutableList<Decision> options) {
        try {
            // Detect which action card is missing from this game's board
            detectMissingActionCard(state);
            
            // PRIORITY 1: Always buy FRAMEWORK if available (victory points matter late game)
            for (Decision option : options) {
                Card.Type cardType = getTypeFromDecision(option);
                if (cardType == Card.Type.FRAMEWORK) {
                    if (verbose) {
                        System.out.println(playerName + " BUYING FRAMEWORK (highest priority)");
                    }
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
                
                // Get weight for this specific card
                Float weight = getWeight(cardType);
                
                // Decrement score if we already have many of this card (diminishing returns for duplicates)
                int currentCount = acquiredCards.getOrDefault(cardType, 0);
                float score = weight / (1.0f + (currentCount * 0.5f));
                
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
        } catch (Exception e) {
            System.err.println("ERROR in " + playerName + " chooseBuyDecision: " + e.getMessage());
            e.printStackTrace();
            return options.isEmpty() ? null : options.get(0);
        }
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
        
        // Update individual defense card weights based on threat level
        cardWeights.put(Card.Type.MONITORING, newDefenseWeight);
        cardWeights.put(Card.Type.MERGE_CONFLICT, newDefenseWeight);
        cardWeights.put(Card.Type.REFACTOR, newDefenseWeight);
        
        // Also update instance weights if configured
        if (instanceCardWeights != null) {
            instanceCardWeights.put(Card.Type.MONITORING, newDefenseWeight);
            instanceCardWeights.put(Card.Type.MERGE_CONFLICT, newDefenseWeight);
            instanceCardWeights.put(Card.Type.REFACTOR, newDefenseWeight);
        }
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

    /**
     * ============================================================================
     * DECK-AWARE WEIGHTS CACHE
     * ============================================================================
     * 
     * The board randomly selects 10 of 12 action cards each game.
     * There are C(12,2) = 66 possible board configurations (all pairs of 2 missing cards).
     * 
     * This map stores optimized weights computed offline for each configuration.
     * Keys are sorted pairs: "CARD1_CARD2" where CARD1 < CARD2 alphabetically.
     * Values are weight maps optimized for that specific board configuration.
     * 
     * To populate this cache:
     * 1. Create 66 CardWeightConfig objects representing each pair combination
     * 2. Run CardWeightOptimizer against each configuration (5-10 min each)
     * 3. Export the optimized weights as JSON
     * 4. Load and populate this map at initialization
     * 
     * FUTURE: Implement JSON loading from external config file for easier management.
     */

    // TODO: Implement configuration loading from external JSON file
    // For now, this map is empty and will be populated during optimization phase
    

    /**
     * Get optimized card weights for a specific board configuration.
     * The board randomly selects 10 of 12 action cards each game (5 missing total, but we cache for 2-card pairs).
     * This method returns pre-optimized weights if the configuration is in the cache.
     * 
     * @param configKey A sorted pair like "BACKLOG_CODE_REVIEW" representing the missing cards,
     *                  or a single card name if only 1 missing card has been detected
     * @return Map of card weights optimized for this configuration, or default weights if not cached
     */
    public static Map<Card.Type, Float> getDeckAwareWeights(String configKey) {
        if (configKey == null) {
            return new HashMap<>(cardWeights);
        }
        
        CardWeights deckConfig = DECK_AWARE_WEIGHTS.get(configKey);
        if (deckConfig != null) {
            // System.err.println("[WeightedPlayer3 DEBUG] Found deck-aware config: " + configKey);
            return new HashMap<>(deckConfig.cardWeights);  // Return a copy to prevent external modifications
        }
        
        // Fallback to default weights if configuration not found
        // This may happen early in the game before all cards are seen
        // System.err.println("[WeightedPlayer3 DEBUG] Deck config NOT FOUND: " + configKey + ". Available: " + DECK_AWARE_WEIGHTS.keySet().size() + " configs");
        return new HashMap<>(cardWeights);
    }
}

