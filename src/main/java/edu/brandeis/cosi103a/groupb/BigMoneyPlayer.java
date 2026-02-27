package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.cards.Card;

/**
 * Automated "Big Money" player implementing the money-fallback path only.
 *
 * Strategy (fallback): choose the highest-valued money card available in the
 * provided options. If no money card is available, select an EndPhaseDecision
 * (i.e. buy nothing).
 */
public class BigMoneyPlayer extends ParentPlayer {

    public BigMoneyPlayer() {
        super("BigMoneyPlayer");
    }

    public BigMoneyPlayer(String name) {
        super(name);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        if (options == null || options.isEmpty() || state == null) {
            return null;
        }

        // FRAMEWORK-FIRST: if a BuyDecision for FRAMEWORK is available and affordable, take it.
        for (Decision opt : options) {
            if (opt instanceof BuyDecision) {
                BuyDecision bd = (BuyDecision) opt;
                Card.Type ct = bd.cardType();
                if (ct == Card.Type.FRAMEWORK) {
                    // affordability check using card type's cost
                    if (ct.cost() <= state.spendableMoney()) {
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
            return best;
        }

        // If no money card was found, pick an EndPhaseDecision if present
        for (Decision opt : options) {
            if (opt instanceof EndPhaseDecision) {
                return opt;
            }
        }

        // Fallback: return first option
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
}
