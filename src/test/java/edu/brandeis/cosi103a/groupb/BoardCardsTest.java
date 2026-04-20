package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
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
        // With cost 2, playable cards are those with cost <= 2
        // Foundational: Bug(0), Bitcoin(0), Method(2)
        // Action cards: none at cost <= 2
        CardStacks playableCards = boardCards.getPlayableCards(2);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        // Verify all returned cards have cost <= 2
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 2, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 2");
        }
    }

    @Test
    public void testGetPlayableCardsWithCostThree() {
        // With cost 3, playable: foundational cards + action cards with cost <= 3
        // Guaranteed foundational: Bug(0), Bitcoin(0), Method(2), Ethereum(3)
        // Optional action cards at cost 3: CODE_REVIEW (may or may not be selected)
        CardStacks playableCards = boardCards.getPlayableCards(3);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        // Verify all returned cards have cost <= 3
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 3, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 3");
        }
    }

    @Test
    public void testGetPlayableCardsWithCostFour() {
        // With cost 4, playable: foundational cards + action cards with cost <= 4
        // Guaranteed foundational: Bug(0), Bitcoin(0), Method(2), Ethereum(3)
        // Optional action cards at cost 4: REFACTOR (may or may not be selected)
        CardStacks playableCards = boardCards.getPlayableCards(4);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        // Verify all returned cards have cost <= 4
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 4, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 4");
        }
    }

    @Test
    public void testGetPlayableCardsWithCostFive() {
        // With cost 5, playable: foundational + action cards with cost <= 5
        // Guaranteed foundational: Bug(0), Bitcoin(0), Method(2), Ethereum(3), Module(5)
        // Optional action cards at cost 5: EVERGREEN_TEST, UNIT_TEST, DAILY_SCRUM, PARALLELIZATION (selected vary)
        CardStacks playableCards = boardCards.getPlayableCards(5);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.MODULE));
        // Verify cost constraints
        assertFalse(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK)); // Framework costs 8
        assertFalse(playableCards.getCardTypes().contains(Card.Type.DOGECOIN)); // Dogecoin costs 6
        // Verify all returned cards have cost <= 5
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 5, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 5");
        }
    }

    @Test
    public void testGetPlayableCardsWithCostSix() {
        // With cost 6, playable: foundational + action cards with cost <= 6
        // Guaranteed foundational: Bug(0), Bitcoin(0), Method(2), Ethereum(3), Dogecoin(6)
        // Plus selected action cards at cost <= 6
        CardStacks playableCards = boardCards.getPlayableCards(6);
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.DOGECOIN));
        // Should NOT have Framework (costs 8)
        assertFalse(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK));
        // Verify all returned cards have cost <= 6
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 6, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 6");
        }
    }

    @Test
    public void testGetPlayableCardsWithCostEight() {
        // With cost 8, all cards should be playable (all foundational + all 10 selected action cards)
        CardStacks playableCards = boardCards.getPlayableCards(8);
        // Verify all foundational cards are present
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BUG));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.METHOD));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.MODULE));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.FRAMEWORK));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.BITCOIN));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.ETHEREUM));
        assertTrue(playableCards.getCardTypes().contains(Card.Type.DOGECOIN));
        // At cost 8, should have: 7 foundational + 10 selected action cards = 17 total
        assertEquals(17, playableCards.getCardTypes().size());
        // Verify all returned cards have cost <= 8
        for (Card.Type cardType : playableCards.getCardTypes()) {
            assertTrue(cardType.cost() <= 8, 
                      "Card " + cardType + " costs " + cardType.cost() + " exceeds budget of 8");
        }
    }

    @Test
    public void testGetPlayableCardsExcludesEmptyPiles() {
        // Empty a pile using API rather than internal list
        Map<Card.Type,Integer> stacks = boardCards.getCardStacks();
        while (stacks.get(Card.Type.BITCOIN) > 0) {
            boardCards.drawDeckCard(Card.Type.BITCOIN);
            stacks = boardCards.getCardStacks();
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

    @Test
    public void testInitialCardStacksReflectConstructor() {
        // Verify getCardStacks reports the expected starting counts
        // Only test foundational cards (money and victory) which are always present
        Map<Card.Type,Integer> stacks = boardCards.getCardStacks();
        assertEquals(14, stacks.get(Card.Type.METHOD));
        assertEquals(8, stacks.get(Card.Type.MODULE));
        assertEquals(8, stacks.get(Card.Type.FRAMEWORK));
        assertEquals(60, stacks.get(Card.Type.BITCOIN));
        assertEquals(40, stacks.get(Card.Type.ETHEREUM));
        assertEquals(30, stacks.get(Card.Type.DOGECOIN));
        assertEquals(10, stacks.get(Card.Type.BUG));
        // Note: Action cards are randomly selected (10 of 15), so we don't assert their presence here
        // They may or may not be present in stacks
    }

    @Test
    public void testDrawDeckCardUpdatesStacks() {
        Map<Card.Type,Integer> stacks = boardCards.getCardStacks();
        int before = stacks.get(Card.Type.METHOD);
        Card drawn = boardCards.drawDeckCard(Card.Type.METHOD);
        assertNotNull(drawn);
        int after = boardCards.getCardStacks().get(Card.Type.METHOD);
        assertEquals(before - 1, after);
    }
}
