package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi.atg.cards.Card;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.*;

public class BigMoneyPlayerMoneyFallbackTest {

    private GameState makeStateWithMoney(int spendableMoney) {
        return new GameState(
            "TestPlayer",
            new Hand(ImmutableList.of(), ImmutableList.of()),
            GameState.TurnPhase.BUY,
            1,
            spendableMoney,
            1,
            new CardStacks(ImmutableMap.of())
        );
    }

    @Test
    public void choosesBestAffordableMoneyCard() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision dBitcoin = new BuyDecision(Card.Type.BITCOIN);
        Decision dEthereum = new BuyDecision(Card.Type.ETHEREUM);
        Decision dDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(dBitcoin, dEthereum, dDogecoin, end);

        Decision chosen = p.makeDecision(makeStateWithMoney(10), options);
        assertSame(dDogecoin, chosen);
    }

    @Test
    public void choosesLowestWhenOnlyLowAvailable() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision dBitcoin = new BuyDecision(Card.Type.BITCOIN);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(dBitcoin, end);

        Decision chosen = p.makeDecision(makeStateWithMoney(10), options);
        assertSame(dBitcoin, chosen);
    }

    @Test
    public void choosesEndPhaseWhenNothingAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(end);

        Decision chosen = p.makeDecision(makeStateWithMoney(10), options);
        assertTrue(chosen instanceof EndPhaseDecision);
    }

    // --- Framework-first tests moved here ---

    @Test
    public void frameworkAvailableAndAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        GameState state = makeStateWithMoney(8);

        Decision frameworkDecision = new BuyDecision(Card.Type.FRAMEWORK);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(frameworkDecision, end);

        Decision chosen = p.makeDecision(state, options);
        assertSame(frameworkDecision, chosen);
    }

    @Test
    public void frameworkSkippedWhenNotAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        GameState state = makeStateWithMoney(6);

        Decision bitcoinDecision = new BuyDecision(Card.Type.BITCOIN);
        Decision frameworkDecision = new BuyDecision(Card.Type.FRAMEWORK);
        Decision ethereumDecision = new BuyDecision(Card.Type.ETHEREUM);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(
            bitcoinDecision,
            frameworkDecision,
            ethereumDecision,
            end
        );

        Decision chosen = p.makeDecision(state, options);
        assertSame(ethereumDecision, chosen);
    }

    @Test
    public void frameworkPreferredOverMoneyWhenAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        GameState state = makeStateWithMoney(8);

        Decision frameworkDecision = new BuyDecision(Card.Type.FRAMEWORK);
        Decision dogecoinDecision = new BuyDecision(Card.Type.DOGECOIN);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(dogecoinDecision, frameworkDecision, end);

        Decision chosen = p.makeDecision(state, options);
        assertSame(frameworkDecision, chosen);
    }

    @Test
    public void returnsNullWhenStateIsNull() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision dBitcoin = new BuyDecision(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(dBitcoin);

        Decision chosen = p.makeDecision(null, options);
        assertNull(chosen);
    }

    @Test
    public void returnsNullWhenOptionsAreEmpty() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision chosen = p.makeDecision(makeStateWithMoney(5), ImmutableList.of());
        assertNull(chosen);
    }

    @Test
    public void fallsBackToFirstOptionWhenNoMoneyAndNoEndPhase() {
        BigMoneyPlayer p = new BigMoneyPlayer("TestPlayer");

        Decision methodDecision = new BuyDecision(Card.Type.METHOD);
        Decision moduleDecision = new BuyDecision(Card.Type.MODULE);
        ImmutableList<Decision> options = ImmutableList.of(methodDecision, moduleDecision);

        Decision chosen = p.makeDecision(makeStateWithMoney(10), options);
        assertSame(methodDecision, chosen);
    }
}
