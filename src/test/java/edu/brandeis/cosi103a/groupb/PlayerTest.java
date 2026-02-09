package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;

import edu.brandeis.cosi103a.groupb.Prototypes.PrototypeCard;
import edu.brandeis.cosi103a.groupb.Prototypes.PrototypeDeck;
import edu.brandeis.cosi103a.groupb.Prototypes.PrototypePlayer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class PlayerTest {

    @Test
    public void testConstructor() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypePlayer player = new PrototypePlayer(deck, null);
        assertNotNull(player, "Player should be created successfully");
    }

    @Test
    public void testDrawTurnHand() {
        PrototypeDeck deck = new PrototypeDeck();
        for (int i = 0; i < 10; i++) {
            deck.addToDrawPile(new PrototypeCard(2, 0, 1, "method"));
        }
        PrototypePlayer player = new PrototypePlayer(deck, null);

        List<PrototypeCard> drawnCards = player.drawTurnHand();
        assertNotNull(drawnCards, "drawTurnHand should return a list");
        assertEquals(5, drawnCards.size(), "drawTurnHand should return 5 cards");
    }

    @Test
    public void testAddPoints() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypePlayer player = new PrototypePlayer(deck, null);

        player.addPoints(10);
        // Since getPoints is commented out, we can't directly test the points value
        // But we can test that the method doesn't throw an exception
        assertDoesNotThrow(() -> player.addPoints(5), "addPoints should not throw an exception");
    }

    @Test
    public void testGetDeck() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypePlayer player = new PrototypePlayer(deck, null);

        PrototypeDeck retrievedDeck = player.getDeck();
        assertNotNull(retrievedDeck, "getDeck should return a deck");
        assertEquals(deck, retrievedDeck, "Should return the same deck");
    }

    @Test
    public void testToString() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypePlayer player = new PrototypePlayer(deck, null);

        String str = player.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Player"), "toString should contain 'Player'");
        assertTrue(str.contains("points"), "toString should mention points");
        assertTrue(str.contains("hand"), "toString should mention hand");
        assertTrue(str.contains("deck"), "toString should mention deck");
    }
}
