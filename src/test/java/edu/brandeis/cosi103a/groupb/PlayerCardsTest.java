package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class PlayerCardsTest {

    private static class TestPlayerCards extends PlayerCards {
        TestPlayerCards(BoardCards board) {
            super(board);
        }

        int getCostInHandPublic() {
            return getCostInHand();
        }

        void setUnplayedCards(List<Card> cards) throws Exception {
            Field field = PlayerCards.class.getDeclaredField("unplayedCards");
            field.setAccessible(true);
            field.set(this, cards);
        }

        void setDeck(List<Card> cards) throws Exception {
            Field field = PlayerCards.class.getDeclaredField("deck");
            field.setAccessible(true);
            field.set(this, cards);
        }

        @SuppressWarnings("unchecked")
        int getDeckSize() throws Exception {
            Field field = PlayerCards.class.getDeclaredField("deck");
            field.setAccessible(true);
            return ((List<Card>) field.get(this)).size();
        }

        @SuppressWarnings("unchecked")
        int getDiscardSize() throws Exception {
            Field field = PlayerCards.class.getDeclaredField("discard");
            field.setAccessible(true);
            return ((List<Card>) field.get(this)).size();
        }

        int getUnplayedSize() {
            return getUnplayedCards().size();
        }

        int getPlayedSize() {
            return getHand().playedCards().size();
        }

        void callRefresh() {
            refreshHand();
        }

        int getScorePublic() {
            return getScore();
        }

        List<Card> getDiscardList() {
            return new ArrayList<>(getDiscardPile());
        }
    }

    private BoardCards board;
    private TestPlayerCards player;

    @BeforeEach
    public void setup() {
        board = new BoardCards();
        player = new TestPlayerCards(board);
    }

    @Test
    public void testGetCostInHandCountsCurrencyOnly() throws Exception {
        List<Card> unplayed = new ArrayList<>();
        unplayed.add(new Card(Card.Type.BITCOIN, 0)); //value of 1
        unplayed.add(new Card(Card.Type.ETHEREUM, 1)); //value of 2
        unplayed.add(new Card(Card.Type.DOGECOIN, 2)); //value of 3
        player.setUnplayedCards(unplayed);

        int cost = player.getCostInHandPublic();
        assertEquals(6, cost, "Cost in hand should count only currency cards");
    }

    @Test
    public void testDrawToHandMovesCard() throws Exception {
        int beforeDeck = player.getDeckSize();
        assertTrue(player.drawToHand());
        assertEquals(beforeDeck - 1, player.getDeckSize());
        assertEquals(1, player.getUnplayedSize());
    }

    @Test
    public void testPlayAndTrashAndExceptions() throws Exception {
        assertTrue(player.drawToHand());
        Card card = player.getHand().unplayedCards().iterator().next();
        player.playCard(card);
        assertEquals(1, player.getPlayedSize());
        assertThrows(IllegalArgumentException.class, () -> player.playCard(card));

        // trash a fresh card
        player.drawToHand();
        Card toTrash = player.getHand().unplayedCards().iterator().next();
        player.trashCard(toTrash);
        assertEquals(0, player.getUnplayedSize());
        assertThrows(IllegalArgumentException.class, () -> player.trashCard(toTrash));
    }

    @Test
    public void testRefreshHandRefillsAndDiscards() throws Exception {
        // draw five cards and play them
        for (int i = 0; i < 5; i++) {
            assertTrue(player.drawToHand());
            Card c = player.getHand().unplayedCards().iterator().next();
            player.playCard(c);
        }
        assertEquals(5, player.getPlayedSize());
        player.callRefresh();
        assertEquals(0, player.getPlayedSize());
        assertEquals(5, player.getUnplayedSize());
        assertEquals(5, player.getDiscardSize());
    }

    @Test
    public void testGainCardAddsToDiscard() throws Exception {
        Card c = new Card(Card.Type.BUG, 0);
        player.gainCard(c);
        assertEquals(1, player.getDiscardSize());
    }

    @Test
    public void testGetScoreCountsVictoryPoints() throws Exception {
        List<Card> customDeck = new ArrayList<>();
        customDeck.add(new Card(Card.Type.METHOD, 2));
        customDeck.add(new Card(Card.Type.BITCOIN, 1));
        customDeck.add(new Card(Card.Type.FRAMEWORK, 5));
        player.setDeck(customDeck);
        int score = player.getScorePublic();
        assertEquals(7, score);
    }

    @Test
    public void testGetDiscardPileAndUnplayedCards() throws Exception {
        player.gainCard(new Card(Card.Type.BITCOIN, 0));
        player.drawToHand();
        assertEquals(1, player.getDiscardList().size());
        assertEquals(1, player.getUnplayedSize());
    }
}
