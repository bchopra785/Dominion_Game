

import org.junit.jupiter.api.Test;

import edu.brandeis.cosi103a.groupb.Prototypes.PrototypeCard;
import edu.brandeis.cosi103a.groupb.Prototypes.PrototypeDeck;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class DeckTest {

    @Test
    public void testDefaultConstructor() {
        PrototypeDeck deck = new PrototypeDeck();
        assertNotNull(deck, "Deck should be created successfully");
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty initially");
        assertEquals(0, deck.getDiscardSize(), "Discard pile should be empty initially");
    }

    @Test
    public void testConstructorWithInitialCards() {
        List<PrototypeCard> initialCards = new ArrayList<>();
        initialCards.add(new PrototypeCard(2, 0, 1, "method"));
        initialCards.add(new PrototypeCard(5, 0, 3, "module"));
        PrototypeDeck deck = new PrototypeDeck(initialCards);
        assertNotNull(deck, "Deck should be created successfully");
        assertEquals(2, deck.getDrawPileSize(), "Draw pile should contain initial cards");
    }

    @Test
    public void testAddToDrawPile() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card = new PrototypeCard(2, 0, 1, "method");
        deck.addToDrawPile(card);
        assertEquals(1, deck.getDrawPileSize(), "Draw pile should contain one card after adding");
    }

    @Test
    public void testAddToDiscard() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card = new PrototypeCard(2, 0, 1, "method");
        deck.addToDiscard(card);
        assertEquals(1, deck.getDiscardSize(), "Discard pile should contain one card after adding");
    }

    @Test
    public void testDrawCard() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card = new PrototypeCard(2, 0, 1, "method");
        deck.addToDrawPile(card);
        PrototypeCard drawnCard = deck.drawCard();
        assertNotNull(drawnCard, "Should draw a card successfully");
        assertEquals(card, drawnCard, "Drawn card should be the same as added card");
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty after drawing");
    }

    @Test
    public void testDeckrefilledafterempty() {
        PrototypeDeck deck = new PrototypeDeck();
        for (int i = 0; i < 5; i++) {
            deck.addToDiscard(new PrototypeCard(i, i, i, "test" + i));
        }
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty");

        PrototypeCard drawnCard = deck.drawCard();
        assertNotNull(drawnCard);
        //one card drawn, 4 should remain
        assertEquals(4, deck.getDrawPileSize(), "Draw pile should have at least 5 cards after reshuffling from discard");
    }

    @Test
    public void testShuffleDrawPile() {
        PrototypeDeck deck = new PrototypeDeck();
        List<PrototypeCard> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cards.add(new PrototypeCard(i, i, i, "test" + i));
        }
        deck = new PrototypeDeck(cards);
        List<PrototypeCard> originalOrder = new ArrayList<>(deck.getDrawPile());
        deck.shuffleDrawPile();
        List<PrototypeCard> shuffledOrder = deck.getDrawPile();
        // Note: This test is probabilistic - in rare cases the shuffle might result in the same order
        // For a more robust test, you could check that the list is not in the original order
        assertEquals(originalOrder.size(), shuffledOrder.size(), "Shuffled deck should have same size");
    }

    @Test
    public void testReshuffleFromDiscard() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card1 = new PrototypeCard(2, 0, 1, "method");
        PrototypeCard card2 = new PrototypeCard(5, 0, 3, "module");
        deck.addToDiscard(card1);
        deck.addToDiscard(card2);
        assertEquals(2, deck.getDiscardSize(), "Discard pile should have 2 cards");
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty");
        deck.reshuffleFromDiscard();
        assertEquals(0, deck.getDiscardSize(), "Discard pile should be empty after reshuffling");
        assertEquals(2, deck.getDrawPileSize(), "Draw pile should have 2 cards after reshuffling");
    }

    @Test
    public void testGetDrawPile() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card = new PrototypeCard(2, 0, 1, "method");
        deck.addToDrawPile(card);
        List<PrototypeCard> drawPile = deck.getDrawPile();
        assertNotNull(drawPile, "getDrawPile should return a list");
        assertEquals(1, drawPile.size(), "Returned list should contain the cards");
        // Test that it's a copy by modifying the returned list
        drawPile.clear();
        assertEquals(1, deck.getDrawPileSize(), "Original deck should not be affected by modifying returned list");
    }

    @Test
    public void testGetDiscardPile() {
        PrototypeDeck deck = new PrototypeDeck();
        PrototypeCard card = new PrototypeCard(2, 0, 1, "method");
        deck.addToDiscard(card);
        List<PrototypeCard> discardPile = deck.getDiscardPile();
        assertNotNull(discardPile, "getDiscardPile should return a list");
        assertEquals(1, discardPile.size(), "Returned list should contain the cards");
        // Test that it's a copy
        discardPile.clear();
        assertEquals(1, deck.getDiscardSize(), "Original deck should not be affected by modifying returned list");
    }

    @Test
    public void testGetDrawPileSize() {
        PrototypeDeck deck = new PrototypeDeck();
        assertEquals(0, deck.getDrawPileSize(), "Empty deck should have size 0");
        deck.addToDrawPile(new PrototypeCard(2, 0, 1, "method"));
        assertEquals(1, deck.getDrawPileSize(), "Deck with one card should have size 1");
    }

    @Test
    public void testGetDiscardSize() {
        PrototypeDeck deck = new PrototypeDeck();
        assertEquals(0, deck.getDiscardSize(), "Empty discard should have size 0");
        deck.addToDiscard(new PrototypeCard(2, 0, 1, "method"));
        assertEquals(1, deck.getDiscardSize(), "Discard with one card should have size 1");
    }

    @Test
    public void testIsDrawPileEmpty() {
        PrototypeDeck deck = new PrototypeDeck();
        assertTrue(deck.isDrawPileEmpty(), "Empty deck should return true for isDrawPileEmpty");
        deck.addToDrawPile(new PrototypeCard(2, 0, 1, "method"));
        assertFalse(deck.isDrawPileEmpty(), "Deck with cards should return false for isDrawPileEmpty");
    }

    @Test
    public void testToString() {
        PrototypeDeck deck = new PrototypeDeck();
        String str = deck.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Deck"), "toString should contain 'Deck'");
        assertTrue(str.contains("drawPile"), "toString should mention drawPile");
        assertTrue(str.contains("discardPile"), "toString should mention discardPile");
    }
}
