package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;

public class BoardCardsTest {
    // Card costs: Bug(0), Method(2), Module(5), Framework(8), Bitcoin(0), Ethereum(3), Dogecoin(6), Refactor(4), Code Review(3), Evergreen Test(5)

    private BoardCards boardCards;

    @BeforeEach
    public void setUp() {
        boardCards = new BoardCards();
    }

    @Test
    public void testGetPlayableCardsWithZeroCost() {
        // With 0 cost, only free cards should be playable: Bug(0) and Bitcoin(0)
        CardStacks playableCards = boardCards.getPlayableCards(0);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertEquals(2, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsWithCostTwo() {
        // With cost 2, playable: Bug(0), Bitcoin(0), Method(2)
        CardStacks playableCards = boardCards.getPlayableCards(2);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertEquals(3, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsWithCostThree() {
        // With cost 3, playable: Bug(0), Bitcoin(0), Method(2), Ethereum(3), Code Review(3)
        CardStacks playableCards = boardCards.getPlayableCards(3);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.CODE_REVIEW));
        assertEquals(5, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsWithCostFour() {
        // With cost 4, playable: Bug(0), Bitcoin(0), Method(2), Ethereum(3), Code Review(3), Refactor(4)
        CardStacks playableCards = boardCards.getPlayableCards(4);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.CODE_REVIEW));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.REFACTOR));
        assertEquals(6, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsWithCostFive() {
        // With cost 5, playable: Bug(0), Bitcoin(0), Method(2), Ethereum(3), Code Review(3), Refactor(4), Module(5), Evergreen Test(5)
        CardStacks playableCards = boardCards.getPlayableCards(5);
        assertEquals(8, playableCards.getCardTypes().size());
        assertFalse(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK)); // Framework costs 8
        assertFalse(playableCards.getCardTypes().contains(Card.Type.DOGECOIN)); // Dogecoin costs 6
    }

    @Test
    public void testGetPlayableCardsWithCostSix() {
        // With cost 6, all cards except Framework(8) should be playable
        CardStacks playableCards = boardCards.getPlayableCards(6);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.DOGECOIN)); // Dogecoin costs 6
        assertFalse(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK)); // Framework costs 8
        assertEquals(9, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsWithCostEight() {
        // With cost 8, all cards should be playable
        CardStacks playableCards = boardCards.getPlayableCards(8);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.MODULE));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.DOGECOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.REFACTOR));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.CODE_REVIEW));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.EVERGREEN_TEST));
        assertEquals(10, playableCards.getCardTypes().size());
    }

    @Test
    public void testGetPlayableCardsExcludesEmptyPiles() {
        // Empty piles should not be included
        while (!boardCards.bitcoins.isEmpty()) {
            boardCards.bitcoins.remove(0);
        }
        CardStacks playableCards = boardCards.getPlayableCards(8);
        assertFalse(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
    }

    @Test
    public void testGetPlayableCardsDoesNotExceedBudget() {
        // No cards with cost > budget should be returned
        CardStacks playableCards = boardCards.getPlayableCards(3);
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 3, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 3");
        }
    }
}
