package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class DeckTest {

    @Test
    public void testDefaultConstructor() {
        Deck deck = new Deck();
        assertNotNull(deck, "Deck should be created successfully");
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty initially");
        assertEquals(0, deck.getDiscardSize(), "Discard pile should be empty initially");
    }

    @Test
    public void testConstructorWithInitialCards() {
        List<Card> initialCards = new ArrayList<>();
        initialCards.add(new Card(2, 0, 1, "method"));
        initialCards.add(new Card(5, 0, 3, "module"));
        Deck deck = new Deck(initialCards);
        assertNotNull(deck, "Deck should be created successfully");
        assertEquals(2, deck.getDrawPileSize(), "Draw pile should contain initial cards");
    }

    @Test
    public void testAddToDrawPile() {
        Deck deck = new Deck();
        Card card = new Card(2, 0, 1, "method");
        deck.addToDrawPile(card);
        assertEquals(1, deck.getDrawPileSize(), "Draw pile should contain one card after adding");
    }

    @Test
    public void testAddToDiscard() {
        Deck deck = new Deck();
        Card card = new Card(2, 0, 1, "method");
        deck.addToDiscard(card);
        assertEquals(1, deck.getDiscardSize(), "Discard pile should contain one card after adding");
    }

    @Test
    public void testDrawCard() {
        Deck deck = new Deck();
        Card card = new Card(2, 0, 1, "method");
        deck.addToDrawPile(card);
        Card drawnCard = deck.drawCard();
        assertNotNull(drawnCard, "Should draw a card successfully");
        assertEquals(card, drawnCard, "Drawn card should be the same as added card");
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty after drawing");
    }

    @Test
    public void testDeckrefilledafterempty() {
        Deck deck = new Deck();
        for (int i = 0; i < 5; i++) {
            deck.addToDiscard(new Card(i, i, i, "test" + i));
        }
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty");

        Card drawnCard = deck.drawCard();
        assertNotNull(drawnCard);
        //one card drawn, 4 should remain
        assertEquals(4, deck.getDrawPileSize(), "Draw pile should have at least 5 cards after reshuffling from discard");
    }

    @Test
    public void testShuffleDrawPile() {
        Deck deck = new Deck();
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cards.add(new Card(i, i, i, "test" + i));
        }
        deck = new Deck(cards);
        List<Card> originalOrder = new ArrayList<>(deck.getDrawPile());
        deck.shuffleDrawPile();
        List<Card> shuffledOrder = deck.getDrawPile();
        // Note: This test is probabilistic - in rare cases the shuffle might result in the same order
        // For a more robust test, you could check that the list is not in the original order
        assertEquals(originalOrder.size(), shuffledOrder.size(), "Shuffled deck should have same size");
    }

    @Test
    public void testReshuffleFromDiscard() {
        Deck deck = new Deck();
        Card card1 = new Card(2, 0, 1, "method");
        Card card2 = new Card(5, 0, 3, "module");
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
        Deck deck = new Deck();
        Card card = new Card(2, 0, 1, "method");
        deck.addToDrawPile(card);
        List<Card> drawPile = deck.getDrawPile();
        assertNotNull(drawPile, "getDrawPile should return a list");
        assertEquals(1, drawPile.size(), "Returned list should contain the cards");
        // Test that it's a copy by modifying the returned list
        drawPile.clear();
        assertEquals(1, deck.getDrawPileSize(), "Original deck should not be affected by modifying returned list");
    }

    @Test
    public void testGetDiscardPile() {
        Deck deck = new Deck();
        Card card = new Card(2, 0, 1, "method");
        deck.addToDiscard(card);
        List<Card> discardPile = deck.getDiscardPile();
        assertNotNull(discardPile, "getDiscardPile should return a list");
        assertEquals(1, discardPile.size(), "Returned list should contain the cards");
        // Test that it's a copy
        discardPile.clear();
        assertEquals(1, deck.getDiscardSize(), "Original deck should not be affected by modifying returned list");
    }

    @Test
    public void testGetDrawPileSize() {
        Deck deck = new Deck();
        assertEquals(0, deck.getDrawPileSize(), "Empty deck should have size 0");
        deck.addToDrawPile(new Card(2, 0, 1, "method"));
        assertEquals(1, deck.getDrawPileSize(), "Deck with one card should have size 1");
    }

    @Test
    public void testGetDiscardSize() {
        Deck deck = new Deck();
        assertEquals(0, deck.getDiscardSize(), "Empty discard should have size 0");
        deck.addToDiscard(new Card(2, 0, 1, "method"));
        assertEquals(1, deck.getDiscardSize(), "Discard with one card should have size 1");
    }

    @Test
    public void testIsDrawPileEmpty() {
        Deck deck = new Deck();
        assertTrue(deck.isDrawPileEmpty(), "Empty deck should return true for isDrawPileEmpty");
        deck.addToDrawPile(new Card(2, 0, 1, "method"));
        assertFalse(deck.isDrawPileEmpty(), "Deck with cards should return false for isDrawPileEmpty");
    }

    @Test
    public void testToString() {
        Deck deck = new Deck();
        String str = deck.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Deck"), "toString should contain 'Deck'");
        assertTrue(str.contains("drawPile"), "toString should mention drawPile");
        assertTrue(str.contains("discardPile"), "toString should mention discardPile");
    }
}
