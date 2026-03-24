
package edu.brandeis.cosi103a.groupb.network;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import java.util.Optional;
import edu.brandeis.cosi103a.groupb.RecordingGameObserver;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import com.google.common.collect.ImmutableList;

/**
 * StrategyPlayer implements BUY-phase and makeDecision logic as Person B's part.
 * - chooseBuyDecision: Implements BUY-phase logic for at least two distinct cards, applies tie-break rule.
 * - makeDecision: Routes to phase-specific logic (ACTION, BUY, MONEY, fallback).
 *
 * Only uses provided options. Does not touch ACTION or MONEY logic.
 */
public class StrategyPlayer extends ParentPlayer {
    private final RecordingGameObserver observer = new RecordingGameObserver();

    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.of(observer);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }
    public StrategyPlayer(String name) {
        super(name);
    }


    /**
     * BUY-phase logic: pick highest priority action card, tie-break by first in options.
     * Example: Prefer "REFACTOR", then "EVERGREEN_TEST", else first action card, else EndPhaseDecision, else first option.
     */
    public Decision chooseBuyDecision(ImmutableList<Decision> options, GameState state) {
        // Prefer REFACTOR
        for (Decision d : options) {
            Card c = getCardFromDecision(d);
            if (c != null && "REFACTOR".equals(getCardName(c))) return d;
        }
        // Prefer EVERGREEN_TEST
        for (Decision d : options) {
            Card c = getCardFromDecision(d);
            if (c != null && "EVERGREEN_TEST".equals(getCardName(c))) return d;
        }
        // Any other action card
        for (Decision d : options) {
            Card c = getCardFromDecision(d);
            if (c != null && isActionCard(c)) return d;
        }
        // Fallback: EndPhaseDecision if present
        for (Decision d : options) {
            if (d.getClass().getSimpleName().equals("EndPhaseDecision")) return d;
        }
        // Final fallback: pick first option
        return options.get(0);
    }


    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        // Route by phase
        if (state != null && state.phase() != null) {
            switch (state.phase().toString()) {
                case "ACTION":
                    // Person A's code (do not implement)
                    return null;
                case "BUY":
                    return chooseBuyDecision(options, state);
                case "MONEY":
                    // Person A's code (do not implement)
                    return null;
                default:
                    // Fallback: EndPhaseDecision if present
                    for (Decision d : options) {
                        if (d.getClass().getSimpleName().equals("EndPhaseDecision")) return d;
                    }
                    return options.get(0);
            }
        }
        // Fallback: EndPhaseDecision if present
        for (Decision d : options) {
            if (d.getClass().getSimpleName().equals("EndPhaseDecision")) return d;
        }
        return options.get(0);
    }

    // Helper to check if a card is an action card (by type/category if available)
    private boolean isActionCard(Card c) {
        try {
            Object type = c.getClass().getMethod("type").invoke(c);
            Object category = type.getClass().getMethod("category").invoke(type);
            return category.toString().equals("ACTION");
        } catch (Exception e) {
            return false;
        }
    }

    // Helper to extract Card from Decision (reflection or known method)
    private Card getCardFromDecision(Decision d) {
        try {
            // Try getCard() method if it exists
            return (Card) d.getClass().getMethod("getCard").invoke(d);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper to extract name from Card (reflection or known method)
    private String getCardName(Card c) {
        try {
            return (String) c.getClass().getMethod("getName").invoke(c);
        } catch (Exception e) {
            return c.toString();
        }
    }
}
