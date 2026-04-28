package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class FlexiblePlayerTest {

    private GameState makeState(GameState.TurnPhase phase, int spendableMoney) {
        return new GameState(
            "TestPlayer",
            new Hand(ImmutableList.of(), ImmutableList.of()),
            phase,
            1,
            spendableMoney,
            1,
            new CardStacks(ImmutableMap.of())
        );
    }

    private static final class TestFlexiblePlayer extends FlexiblePlayer {
        private final boolean buyBug;

        private TestFlexiblePlayer(boolean buyBug) {
            super("TestPlayer");
            this.buyBug = buyBug;
        }

        @Override
        protected boolean shouldBuyBug() {
            return buyBug;
        }
    }

    @Test
    public void actionPrefersBugOverOtherPlays() {
        FlexiblePlayer player = new FlexiblePlayer("TestPlayer");

        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 2));
        Decision bug = new PlayCardDecision(new Card(Card.Type.BUG, 0));
        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 1));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(codeReview, bug, refactor, endAction);

        Decision chosen = player.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(bug, chosen);
    }

    @Test
    public void buyChoosesBugWhenChanceTriggers() {
        FlexiblePlayer player = new TestFlexiblePlayer(true);

        Decision bug = new BuyDecision(Card.Type.BUG);
        Decision framework = new BuyDecision(Card.Type.FRAMEWORK);
        Decision dogecoin = new BuyDecision(Card.Type.DOGECOIN);
        Decision endBuy = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(framework, dogecoin, bug, endBuy);

        Decision chosen = player.chooseBuyDecision(makeState(GameState.TurnPhase.BUY, 8), options);
        assertSame(bug, chosen);
    }

    @Test
    public void buyFallsBackToBigMoneyWhenChanceDoesNotTrigger() {
        FlexiblePlayer player = new TestFlexiblePlayer(false);

        Decision bug = new BuyDecision(Card.Type.BUG);
        Decision framework = new BuyDecision(Card.Type.FRAMEWORK);
        Decision dogecoin = new BuyDecision(Card.Type.DOGECOIN);
        Decision endBuy = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(bug, dogecoin, framework, endBuy);

        Decision chosen = player.chooseBuyDecision(makeState(GameState.TurnPhase.BUY, 8), options);
        assertSame(framework, chosen);
    }

    @Test
    public void buyPrefersModuleWhenFrameworkUnavailable() {
        FlexiblePlayer player = new TestFlexiblePlayer(false);

        Decision module = new BuyDecision(Card.Type.MODULE);
        Decision method = new BuyDecision(Card.Type.METHOD);
        Decision dogecoin = new BuyDecision(Card.Type.DOGECOIN);
        Decision endBuy = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(method, dogecoin, module, endBuy);

        Decision chosen = player.chooseBuyDecision(makeState(GameState.TurnPhase.BUY, 5), options);
        assertSame(module, chosen);
    }

    @Test
    public void buyUsesMethodWhenModuleAndFrameworkUnavailable() {
        FlexiblePlayer player = new TestFlexiblePlayer(false);

        Decision method = new BuyDecision(Card.Type.METHOD);
        Decision bitcoin = new BuyDecision(Card.Type.BITCOIN);
        Decision endBuy = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(bitcoin, method, endBuy);

        Decision chosen = player.chooseBuyDecision(makeState(GameState.TurnPhase.BUY, 2), options);
        assertSame(method, chosen);
    }
}