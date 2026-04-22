package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

public class WeightedPlayerTest {

    // Helper to create a BuyDecision for a given Card.Type
    private BuyDecision buy(Card.Type type) {
        return new BuyDecision(type);
    }

    // Helper to create an EndPhaseDecision
    private EndPhaseDecision end(GameState.TurnPhase phase) {
        return new EndPhaseDecision(phase);
    }

    // Tests for chooseBuyDecision logic

    @SuppressWarnings("nullness")
    @Test
    public void testBuyEndsPhaseWhenNoCardAvailable() {
        WeightedPlayer player = new WeightedPlayer();
        Decision endPhase = end(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        assertEquals(endPhase, player.chooseBuyDecision(null, options));
    }

    @SuppressWarnings("nullness")
    @Test
    public void testMoneyEndsPhaseWhenNoMoneyCard() {
        WeightedPlayer player = new WeightedPlayer();
        EndPhaseDecision endPhase = end(GameState.TurnPhase.MONEY);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        assertEquals(endPhase, player.chooseMoneyDecision(null, options));
    }

    @SuppressWarnings("nullness")
    @Test
    public void testActionDecisionReturnsFallback() {
        WeightedPlayer player = new WeightedPlayer();
        EndPhaseDecision endPhase = end(GameState.TurnPhase.ACTION);
        ImmutableList<Decision> options = ImmutableList.of(endPhase);
        assertEquals(endPhase, player.chooseActionDecision(null, options));
    }

    @Test
    public void testGetAcquiredCardCountReturnsZeroForNeverAcquiredCard() {
        WeightedPlayer player = new WeightedPlayer();
        assertEquals(0, player.getAcquiredCardCount(Card.Type.FRAMEWORK));
    }

    @Test
    public void testGetAcquiredCardsReturnsEmptyMapWhenNoCardsAcquired() {
        WeightedPlayer player = new WeightedPlayer();
        Map<Card.Type, Integer> acquired = player.getAcquiredCards();
        assertTrue(acquired.isEmpty());
    }

    @Test
    public void testGetAcquiredCardsReturnsIndependentCopy() {
        WeightedPlayer player = new WeightedPlayer();
        Map<Card.Type, Integer> acquired1 = player.getAcquiredCards();
        Map<Card.Type, Integer> acquired2 = player.getAcquiredCards();
        
        // Verify they are independent copies (not the same object)
        assertNotSame(acquired1, acquired2);
        assertEquals(acquired1, acquired2);
    }


}
