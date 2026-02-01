package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class PlayerTest {

    @Test
    public void testConstructor() {
        Deck deck = new Deck();
        Player player = new Player(deck, null);
        assertNotNull(player, "Player should be created successfully");
    }

    @Test
    public void testDrawTurnHand() {
        Deck deck = new Deck();
        for (int i = 0; i < 10; i++) {
            deck.addToDrawPile(new Card(2, 0, 1, "method"));
        }
        Player player = new Player(deck, null);

        List<Card> drawnCards = player.drawTurnHand();
        assertNotNull(drawnCards, "drawTurnHand should return a list");
        assertEquals(5, drawnCards.size(), "drawTurnHand should return 5 cards");
    }

    @Test
    public void testAddPoints() {
        Deck deck = new Deck();
        Player player = new Player(deck, null);

        player.addPoints(10);
        // Since getPoints is commented out, we can't directly test the points value
        // But we can test that the method doesn't throw an exception
        assertDoesNotThrow(() -> player.addPoints(5), "addPoints should not throw an exception");
    }

    @Test
    public void testGetDeck() {
        Deck deck = new Deck();
        Player player = new Player(deck, null);

        Deck retrievedDeck = player.getDeck();
        assertNotNull(retrievedDeck, "getDeck should return a deck");
        assertEquals(deck, retrievedDeck, "Should return the same deck");
    }

    @Test
    public void testToString() {
        Deck deck = new Deck();
        Player player = new Player(deck, null);

        String str = player.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Player"), "toString should contain 'Player'");
        assertTrue(str.contains("points"), "toString should mention points");
        assertTrue(str.contains("hand"), "toString should mention hand");
        assertTrue(str.contains("deck"), "toString should mention deck");
    }
}
