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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrategyPlayerActionTest {

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

    @Test
    public void actionPrefersCodeReviewOverRefactor() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 1));
        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 2));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(refactor, codeReview, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(codeReview, chosen);
    }

    @Test
    public void actionUsesRefactorWhenCodeReviewMissing() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision bitcoin = new PlayCardDecision(new Card(Card.Type.BITCOIN, 1));
        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 2));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(bitcoin, refactor, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(refactor, chosen);
    }

    @Test
    public void actionTieBreakUsesFirstMatchingOption() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision codeReview1 = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 10));
        Decision codeReview2 = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 11));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(codeReview1, codeReview2, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(codeReview1, chosen);
    }

    @Test
    public void actionFallsBackToEndPhaseWhenNoPreferredActionExists() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision buyMethod = new BuyDecision(Card.Type.METHOD);
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(buyMethod, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(endAction, chosen);
    }

    @Test
    public void actionIgnoresBuyDecisionEvenWhenTypeIsPreferredAction() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision buyCodeReview = new BuyDecision(Card.Type.CODE_REVIEW);
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(buyCodeReview, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(endAction, chosen);
    }

    @Test
    public void actionPrefersCodeReviewEvenIfRefactorHasHigherCardValue() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision refactorHigh = new PlayCardDecision(new Card(Card.Type.REFACTOR, 99));
        Decision codeReviewLow = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 1));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(refactorHigh, codeReviewLow, endAction);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(codeReviewLow, chosen);
    }

    @Test
    public void actionFallsBackToAnyEndPhaseWhenActionEndMissing() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision buyMethod = new BuyDecision(Card.Type.METHOD);
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(buyMethod, endMoney);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(endMoney, chosen);
    }

    @Test
    public void moneyChoosesHighestValueMoneyCard() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision bitcoin = new PlayCardDecision(new Card(Card.Type.BITCOIN, 1));
        Decision ethereum = new PlayCardDecision(new Card(Card.Type.ETHEREUM, 2));
        Decision dogecoin = new PlayCardDecision(new Card(Card.Type.DOGECOIN, 3));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(bitcoin, ethereum, dogecoin, endMoney);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(dogecoin, chosen);
    }

    @Test
    public void moneyTieBreakUsesFirstMatchingOption() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision dogecoin1 = new PlayCardDecision(new Card(Card.Type.DOGECOIN, 20));
        Decision dogecoin2 = new PlayCardDecision(new Card(Card.Type.DOGECOIN, 21));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(dogecoin1, dogecoin2, endMoney);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(dogecoin1, chosen);
    }

    @Test
    public void moneyFallsBackToEndPhaseWhenNoMoneyPlayExists() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 5));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(codeReview, endMoney);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(endMoney, chosen);
    }

    @Test
    public void moneyIgnoresActionCardsAndSelectsMoneyCard() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 100));
        Decision ethereum = new PlayCardDecision(new Card(Card.Type.ETHEREUM, 2));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(codeReview, ethereum, endMoney);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(ethereum, chosen);
    }

    @Test
    public void moneyPrefersHigherValueMoneyType() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision bitcoin = new PlayCardDecision(new Card(Card.Type.BITCOIN, 5));
        Decision dogecoin = new PlayCardDecision(new Card(Card.Type.DOGECOIN, 3));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(bitcoin, dogecoin, endMoney);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(dogecoin, chosen);
    }

    @Test
    public void moneyFallsBackToAnyEndPhaseWhenMoneyEndMissing() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 5));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(codeReview, endAction);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(endAction, chosen);
    }

    @Test
    public void actionFallsBackToFirstOptionWhenNoEndPhaseExists() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision buyMethod = new BuyDecision(Card.Type.METHOD);
        Decision buyModule = new BuyDecision(Card.Type.MODULE);

        ImmutableList<Decision> options = ImmutableList.of(buyMethod, buyModule);

        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(buyMethod, chosen);
    }

    @Test
    public void moneyFallsBackToFirstOptionWhenNoEndPhaseExists() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 5));
        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 6));

        ImmutableList<Decision> options = ImmutableList.of(codeReview, refactor);

        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(codeReview, chosen);
    }

    @Test
    public void actionDecisionOnlyEndPhases() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        Decision endBuy = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endAction, endBuy);
        Decision chosen = p.chooseActionDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(endAction, chosen);
    }

    @Test
    public void moneyDecisionOnlyEndPhases() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endMoney, endAction);
        Decision chosen = p.chooseMoneyDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(endMoney, chosen);
    }

    @Test
    public void makeDecisionRoutesToActionLogic() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 1));
        Decision codeReview = new PlayCardDecision(new Card(Card.Type.CODE_REVIEW, 2));
        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);

        ImmutableList<Decision> options = ImmutableList.of(refactor, codeReview, endAction);

        Decision chosen = p.makeDecision(makeState(GameState.TurnPhase.ACTION, 0), options);
        assertSame(codeReview, chosen);
    }

    @Test
    public void makeDecisionRoutesToMoneyLogic() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision bitcoin = new PlayCardDecision(new Card(Card.Type.BITCOIN, 1));
        Decision dogecoin = new PlayCardDecision(new Card(Card.Type.DOGECOIN, 3));
        Decision endMoney = new EndPhaseDecision(GameState.TurnPhase.MONEY);

        ImmutableList<Decision> options = ImmutableList.of(bitcoin, dogecoin, endMoney);

        Decision chosen = p.makeDecision(makeState(GameState.TurnPhase.MONEY, 0), options);
        assertSame(dogecoin, chosen);
    }

    @Test
    public void makeDecisionReturnsNullWhenStateIsNull() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision endAction = new EndPhaseDecision(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endAction);

        Decision chosen = p.makeDecision(null, options);
        assertNull(chosen);
    }

    @Test
    public void makeDecisionReturnsNullWhenOptionsAreEmpty() {
        StrategyPlayer p = new StrategyPlayer("TestPlayer");

        Decision chosen = p.makeDecision(makeState(GameState.TurnPhase.ACTION, 0), ImmutableList.of());
        assertNull(chosen);
    }
}
