package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.cards.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BigMoneyPlayerMoneyFallbackTest {

    @Test
    public void choosesBestAffordableMoneyCard() {
        BigMoneyPlayer p = new BigMoneyPlayer("Mika");

        Decision dBitcoin = new BuyDecision(Card.Type.BITCOIN);
        Decision dEthereum = new BuyDecision(Card.Type.ETHEREUM);
        Decision dDogecoin = new BuyDecision(Card.Type.DOGECOIN);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(dBitcoin, dEthereum, dDogecoin, end);

        Decision chosen = p.makeDecision(null, options);
        assertSame(dDogecoin, chosen);
    }

    @Test
    public void choosesLowestWhenOnlyLowAvailable() {
        BigMoneyPlayer p = new BigMoneyPlayer("Mika");

        Decision dBitcoin = new BuyDecision(Card.Type.BITCOIN);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(dBitcoin, end);

        Decision chosen = p.makeDecision(null, options);
        assertSame(dBitcoin, chosen);
    }

    @Test
    public void choosesEndPhaseWhenNothingAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("Mika");

        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(end);

        Decision chosen = p.makeDecision(null, options);
        assertTrue(chosen instanceof EndPhaseDecision);
    }

    // --- Framework-first tests moved here ---

    @Test
    public void frameworkAvailableAndAffordable() {
        BigMoneyPlayer p = new BigMoneyPlayer("Mika");

        GameState state = new GameState(
            "Mika",
            null,
            GameState.TurnPhase.BUY,
            1,
            5,
            1,
            null
        );

        Decision frameworkDecision = new BuyDecision(Card.Type.FRAMEWORK);
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);

        ImmutableList<Decision> options = ImmutableList.of(frameworkDecision, end);

        Decision chosen = p.makeDecision(state, options);
        assertSame(frameworkDecision, chosen);
    }

    @Test
    public void frameworkChosenWhenMultipleAffordableOptions() {
        BigMoneyPlayer p = new BigMoneyPlayer("Mika");

        GameState state = new GameState(
            "Mika",
            null,
            GameState.TurnPhase.BUY,
            1,
            6,
            1,
            null
        );

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
        assertSame(frameworkDecision, chosen);
    }
}
