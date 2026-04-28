package edu.brandeis.cosi103a.groupb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import org.junit.jupiter.api.Test;

class MutableGameStateTest {

    @Test
    void toGameStateReflectsFieldUpdates() {
        BoardCards board = new BoardCards();
        Hand initialHand = new Hand(ImmutableList.of(), ImmutableList.of(new Card(Card.Type.BITCOIN, 0)));
        CardStacks initialBuyable = board.getPlayableCards(0);

        MutableGameState mutable = new MutableGameState(
            "P1",
            initialHand,
            GameState.TurnPhase.ACTION,
            1,
            0,
            1,
            initialBuyable
        );

        Hand updatedHand = new Hand(
            ImmutableList.of(new Card(Card.Type.CODE_REVIEW, 0)),
            ImmutableList.of(new Card(Card.Type.ETHEREUM, 1))
        );
        CardStacks updatedBuyable = board.getPlayableCards(3);

        mutable.setCurrentPlayerName("P2");
        mutable.setCurrentPlayerHand(updatedHand);
        mutable.setPhase(GameState.TurnPhase.BUY);
        mutable.setAvailableActions(0);
        mutable.setSpendableMoney(3);
        mutable.setAvailableBuys(2);
        mutable.setBuyableCards(updatedBuyable);

        GameState snapshot = mutable.toGameState();

        assertEquals("P2", snapshot.currentPlayerName());
        assertEquals(GameState.TurnPhase.BUY, snapshot.phase());
        assertEquals(0, snapshot.availableActions());
        assertEquals(3, snapshot.spendableMoney());
        assertEquals(2, snapshot.availableBuys());
        assertEquals(updatedHand, snapshot.currentPlayerHand());
        assertEquals(updatedBuyable, snapshot.buyableCards());
    }

    @Test
    void constructorFromGameStateCopiesValues() {
        BoardCards board = new BoardCards();
        Hand hand = new Hand(ImmutableList.of(), ImmutableList.of(new Card(Card.Type.DOGECOIN, 2)));
        GameState source = new GameState("P3", hand, GameState.TurnPhase.MONEY, 1, 2, 1, board.getPlayableCards(2));

        MutableGameState mutable = new MutableGameState(source);
        GameState copied = mutable.toGameState();

        assertEquals(source.currentPlayerName(), copied.currentPlayerName());
        assertEquals(source.currentPlayerHand(), copied.currentPlayerHand());
        assertEquals(source.phase(), copied.phase());
        assertEquals(source.availableActions(), copied.availableActions());
        assertEquals(source.spendableMoney(), copied.spendableMoney());
        assertEquals(source.availableBuys(), copied.availableBuys());
        assertEquals(source.buyableCards(), copied.buyableCards());
    }
}
