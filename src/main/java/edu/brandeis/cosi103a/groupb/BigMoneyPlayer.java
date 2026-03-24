package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.cards.Card;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.PrintStream;

/**
 * Automated "Big Money" player implementing the money-fallback path only.
 *
 * Strategy (fallback): choose the highest-valued money card available in the
 * provided options. If no money card is available, select an EndPhaseDecision
 * (i.e. buy nothing).
 */
public class BigMoneyPlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private PrintStream out;

    public BigMoneyPlayer() {
        super("BigMoneyPlayer-" + COUNTER.getAndIncrement());
        this.out = System.out;
    }

    public BigMoneyPlayer(String name) {
        super(name);
        this.out = System.out;
    }

    public BigMoneyPlayer(String name, PrintStream out) {
        super(name);
        this.out = out;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {

        out.print(describeGameState(state));
        out.println(getName() + " is making a decision...");
        if (options == null || options.isEmpty() || state == null) {
            return null;
        }

        // MONEY phase: play money cards so spendableMoney increases before BUY phase.
        if (state.phase() == GameState.TurnPhase.MONEY) {
            Decision bestMoneyPlay = null;
            int bestValue = Integer.MIN_VALUE;
            for (Decision opt : options) {
                if (opt instanceof PlayCardDecision) {
                    Card c = ((PlayCardDecision) opt).card();
                    if (c.type().category() == Card.Type.Category.MONEY) {
                        if (c.value() > bestValue) {
                            bestValue = c.value();
                            bestMoneyPlay = opt;
                        }
                    }
                }
            }
            if (bestMoneyPlay != null) {
                return bestMoneyPlay;
            }
        }

        // FRAMEWORK-FIRST: if a BuyDecision for FRAMEWORK is available and affordable, take it.
        for (Decision opt : options) {
            if (opt instanceof BuyDecision) {
                BuyDecision bd = (BuyDecision) opt;
                Card.Type ct = bd.cardType();
                if (ct == Card.Type.FRAMEWORK) {
                    // affordability check using card type's cost
                    if (ct.cost() <= state.spendableMoney()) {
                        out.println(getName() + " chose FRAMEWORK");
                        return opt;
                    }
                }
            }
        }

        // Existing fallback: Pick best money card among BuyDecision or GainCardDecision options.
        Decision best = null;
        int bestValue = Integer.MIN_VALUE;

        for (Decision opt : options) {
            Card.Type t = null;
            if (opt instanceof BuyDecision) {
                t = ((BuyDecision) opt).cardType();
            } else if (opt instanceof GainCardDecision) {
                t = ((GainCardDecision) opt).cardType();
            }

            if (t == null) {
                continue;
            }

            // Consider only money card types
            int value = moneyValueForType(t);
            if (value > bestValue) {
                bestValue = value;
                best = opt;
            }
        }

        if (best != null) {
            Card.Type bestType = null;
            if (best instanceof BuyDecision) {
                bestType = ((BuyDecision) best).cardType();
            } else if (best instanceof GainCardDecision) {
                bestType = ((GainCardDecision) best).cardType();
            }
            out.println(getName() + " chose " + (bestType != null ? bestType : "unknown"));
            return best;
        }

        // If no money card was found, pick an EndPhaseDecision if present
        for (Decision opt : options) {
            if (opt instanceof EndPhaseDecision) {
                out.println(getName() + " chose EndPhaseDecision");
                return opt;
            }
        }

        // Fallback: return first option
        out.println(getName() + " chose fallback first option");
        return options.get(0);
    }

    private int moneyValueForType(Card.Type t) {
        if (t == null) return Integer.MIN_VALUE;
        switch (t) {
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



    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }


}
