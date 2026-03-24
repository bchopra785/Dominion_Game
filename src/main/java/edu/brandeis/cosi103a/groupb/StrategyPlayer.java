package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;

import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Strategy player for Milestone 2 Story 3.
 *
 * Person A scope in this class:
 * - ACTION phase selection in chooseActionDecision(...)
 * - MONEY phase selection in chooseMoneyDecision(...)
 *
 * Person B scope in this class:
 * - BUY phase strategy in chooseBuyDecision(...)
 * - makeDecision(...) integration details
 */
public class StrategyPlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final PrintStream out;
    private final RecordingGameObserver observer;

    public StrategyPlayer() {
        super("StrategyPlayer-" + COUNTER.getAndIncrement());
        this.out = System.out;
        this.observer = new RecordingGameObserver();
    }

    public StrategyPlayer(String name) {
        super(name);
        this.out = System.out;
        this.observer = new RecordingGameObserver();
    }

    public StrategyPlayer(String name, PrintStream out) {
        super(name);
        this.out = out;
        this.observer = new RecordingGameObserver();
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
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

        return findBestFallback(options, state.phase());
    }

    /**
     * Person A implementation: prioritize CODE_REVIEW, then REFACTOR.
     * Tie-break rule: first matching option in the provided options list.
     */
    public Decision chooseActionDecision(GameState state, ImmutableList<Decision> options) {
        Decision firstCodeReview = null;
        Decision firstRefactor = null;

        for (Decision option : options) {
            if (!(option instanceof PlayCardDecision)) {
                continue;
            }

            Card playedCard = ((PlayCardDecision) option).card();
            Card.Type type = playedCard.type();

            if (type == Card.Type.CODE_REVIEW && firstCodeReview == null) {
                firstCodeReview = option;
            } else if (type == Card.Type.REFACTOR && firstRefactor == null) {
                firstRefactor = option;
            }
        }

        if (firstCodeReview != null) {
            return firstCodeReview;
        }

        if (firstRefactor != null) {
            return firstRefactor;
        }

        return findBestFallback(options, GameState.TurnPhase.ACTION);
    }

    /**
     * Person A implementation: play highest-value money card.
     * Tie-break rule: first matching option in the provided options list.
     */
    public Decision chooseMoneyDecision(GameState state, ImmutableList<Decision> options) {
        Decision bestMoneyPlay = null;
        int bestValue = Integer.MIN_VALUE;

        for (Decision option : options) {
            if (!(option instanceof PlayCardDecision)) {
                continue;
            }

            Card playedCard = ((PlayCardDecision) option).card();
            if (playedCard.type().category() != Card.Type.Category.MONEY) {
                continue;
            }

            int value = playedCard.value();
            if (value > bestValue) {
                bestValue = value;
                bestMoneyPlay = option;
            }
        }

        if (bestMoneyPlay != null) {
            return bestMoneyPlay;
        }

        return findBestFallback(options, GameState.TurnPhase.MONEY);
    }

    /**
     * Person B will replace this BUY logic.
     */
    public Decision chooseBuyDecision(GameState state, ImmutableList<Decision> options) {
        return findBestFallback(options, GameState.TurnPhase.BUY);
    }

    private Decision findBestFallback(ImmutableList<Decision> options, GameState.TurnPhase preferredPhase) {
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                EndPhaseDecision end = (EndPhaseDecision) option;
                if (end.phase() == preferredPhase) {
                    out.println(getName() + " chose EndPhaseDecision(" + preferredPhase + ")");
                    return option;
                }
            }
        }

        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                out.println(getName() + " chose EndPhaseDecision(fallback)");
                return option;
            }
        }

        out.println(getName() + " chose first option fallback");
        return options.get(0);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }
}
