package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flexible strategy player that mostly follows the BigMoneyPlayer buy pattern,
 * with a 30% chance to buy BUG in the BUY phase.
 *
 * Action phase behavior:
 * - play BUG if available
 *
 * Money phase behavior:
 * - play the highest-value money card
 *
 * Buy phase behavior:
 * - buy BUG with a 30% probability when it is available
 * - otherwise follow the big-money fallback path
 */
public class FlexiblePlayer extends ParentPlayer {

    private static final double BUG_BUY_PROBABILITY = 0.30;
    private static final AtomicInteger COUNTER = new AtomicInteger(1);


    public FlexiblePlayer() {
        super("FlexiblePlayer-" + COUNTER.getAndIncrement());
    }

    public FlexiblePlayer(String name) {
        super(name);
    }

    public FlexiblePlayer(String name, PrintStream out) {
        super(name);
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
        Decision firstBug = null;
        Decision firstCodeReview = null;
        Decision firstRefactor = null;

        for (Decision option : options) {
            if (!(option instanceof PlayCardDecision)) {
                continue;
            }

            Card playedCard = ((PlayCardDecision) option).card();
            Card.Type type = playedCard.type();

            if (type == Card.Type.BUG && firstBug == null) {
                firstBug = option;
            } else if (type == Card.Type.CODE_REVIEW && firstCodeReview == null) {
                firstCodeReview = option;
            } else if (type == Card.Type.REFACTOR && firstRefactor == null) {
                firstRefactor = option;
            }
        }

        if (firstBug != null) {
            return firstBug;
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
    * Flexible BUY-phase logic: occasionally buy BUG, otherwise mostly follow BigMoneyPlayer's buy strategy.
     */
    public Decision chooseBuyDecision(GameState state, ImmutableList<Decision> options) {
        Decision bugDecision = findFirstDecisionByType(options, Card.Type.BUG);
        if (bugDecision != null && shouldBuyBug()) {
            return bugDecision;
        }

        return chooseBigMoneyBuyDecision(state, options);
    }

    protected boolean shouldBuyBug() {
        return Math.random() < BUG_BUY_PROBABILITY;
    }

    private Decision chooseBigMoneyBuyDecision(GameState state, ImmutableList<Decision> options) {
        for (Decision option : options) {
            if (option instanceof BuyDecision) {
                BuyDecision buyDecision = (BuyDecision) option;
                Card.Type cardType = buyDecision.cardType();
                if (cardType == Card.Type.FRAMEWORK && canAfford(state, cardType)) {
                    return option;
                }
            }
        }

        for (Decision option : options) {
            if (option instanceof BuyDecision) {
                BuyDecision buyDecision = (BuyDecision) option;
                Card.Type cardType = buyDecision.cardType();
                if (cardType == Card.Type.MODULE && canAfford(state, cardType)) {
                    return option;
                }
            }
        }

        for (Decision option : options) {
            if (option instanceof BuyDecision) {
                BuyDecision buyDecision = (BuyDecision) option;
                Card.Type cardType = buyDecision.cardType();
                if (cardType == Card.Type.METHOD && canAfford(state, cardType)) {
                    return option;
                }
            }
        }

        Decision bestMoneyBuy = null;
        int bestValue = Integer.MIN_VALUE;

        for (Decision option : options) {
            if (!(option instanceof BuyDecision)) {
                continue;
            }

            Card.Type cardType = ((BuyDecision) option).cardType();
            int value = moneyValueForType(cardType);
            if (value > bestValue) {
                bestValue = value;
                bestMoneyBuy = option;
            }
        }

        if (bestMoneyBuy != null) {
            return bestMoneyBuy;
        }

        return findBestFallback(options, GameState.TurnPhase.BUY);
    }

    private boolean canAfford(GameState state, Card.Type cardType) {
        return state == null || cardType == null || cardType.cost() <= state.spendableMoney();
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

    private Decision findFirstDecisionByType(ImmutableList<Decision> options, Card.Type targetType) {
        for (Decision option : options) {
            if (getTypeFromDecision(option) == targetType) {
                return option;
            }
        }
        return null;
    }

    private int moneyValueForType(Card.Type cardType) {
        if (cardType == null) {
            return Integer.MIN_VALUE;
        }

        switch (cardType) {
            case DOGECOIN:
                return 3;
            case ETHEREUM:
                return 2;
            case BITCOIN:
                return 1;
            default:
                return Integer.MIN_VALUE;
        }
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

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }
}
