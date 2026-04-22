package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.engine.Engine;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for WeightedPlayer using repeated games.
 * Verifies invariants and properties that should hold across many game runs.
 */
@DisplayName("WeightedPlayer Property Tests")
public class WeightedPlayerPropertyTest {

    /**
     * Property: After many games, acquired cards map is never null or contains valid entries
     */
    @RepeatedTest(50)
    @DisplayName("Acquired cards map is always valid")
    public void testAcquiredCardsMapIsValid() throws PlayerViolationException {
        WeightedPlayer weightedPlayer = new WeightedPlayer("Weighted");
        BigMoneyPlayer opponent1 = new BigMoneyPlayer("BigMoney1");
        BigMoneyPlayer opponent2 = new BigMoneyPlayer("BigMoney2");
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(weightedPlayer);
        players.add(opponent1);
        players.add(opponent2);
        
        Engine engine = new Engine(players);
        engine.play();
        
        // Property: Map should never be null
        Map<Card.Type, Integer> acquired = weightedPlayer.getAcquiredCards();
        assertNotNull(acquired, "Acquired cards map should never be null");
        
        // Property: All entries should have valid Card.Type keys
        for (Card.Type cardType : acquired.keySet()) {
            assertNotNull(cardType, "Card.Type key should never be null");
        }
    }

    /**
     * Property: All card counts should be non-negative
     */
    @RepeatedTest(50)
    @DisplayName("All card counts are non-negative")
    public void testCardCountsAreNonNegative() throws PlayerViolationException {
        WeightedPlayer weightedPlayer = new WeightedPlayer("Weighted");
        BigMoneyPlayer opponent1 = new BigMoneyPlayer("BigMoney1");
        BigMoneyPlayer opponent2 = new BigMoneyPlayer("BigMoney2");
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(weightedPlayer);
        players.add(opponent1);
        players.add(opponent2);
        
        Engine engine = new Engine(players);
        engine.play();
        
        Map<Card.Type, Integer> acquired = weightedPlayer.getAcquiredCards();
        
        // Property: All counts should be >= 0
        for (Integer count : acquired.values()) {
            assertGreaterThanOrEqual(count, 0, "Card count should never be negative");
        }
    }

    /**
     * Property: getAcquiredCardCount returns consistent results
     */
    @RepeatedTest(50)
    @DisplayName("getAcquiredCardCount is consistent with getAcquiredCards")
    public void testCardCountConsistency() throws PlayerViolationException {
        WeightedPlayer weightedPlayer = new WeightedPlayer("Weighted");
        BigMoneyPlayer opponent1 = new BigMoneyPlayer("BigMoney1");
        BigMoneyPlayer opponent2 = new BigMoneyPlayer("BigMoney2");
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(weightedPlayer);
        players.add(opponent1);
        players.add(opponent2);
        
        Engine engine = new Engine(players);
        engine.play();
        
        Map<Card.Type, Integer> acquired = weightedPlayer.getAcquiredCards();
        
        // Property: For each card in the map, getAcquiredCardCount should return the same value
        for (Card.Type cardType : acquired.keySet()) {
            Integer mapCount = acquired.get(cardType);
            int methodCount = weightedPlayer.getAcquiredCardCount(cardType);
            assertEquals(mapCount.intValue(), methodCount, 
                "getAcquiredCardCount should match map entry for " + cardType);
        }
    }

    /**
     * Property: For any card type not explicitly acquired, count should be 0
     */
    @RepeatedTest(50)
    @DisplayName("Unacquired cards return count of 0")
    public void testUnacquiredCardsReturnZero() throws PlayerViolationException {
        WeightedPlayer weightedPlayer = new WeightedPlayer("Weighted");
        BigMoneyPlayer opponent1 = new BigMoneyPlayer("BigMoney1");
        BigMoneyPlayer opponent2 = new BigMoneyPlayer("BigMoney2");
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(weightedPlayer);
        players.add(opponent1);
        players.add(opponent2);
        
        Engine engine = new Engine(players);
        engine.play();
        
        Map<Card.Type, Integer> acquired = weightedPlayer.getAcquiredCards();
        
        // Property: Cards not in map should return 0
        for (Card.Type cardType : Card.Type.values()) {
            if (!acquired.containsKey(cardType)) {
                assertEquals(0, weightedPlayer.getAcquiredCardCount(cardType),
                    "Card type not in acquired map should return count of 0: " + cardType);
            }
        }
    }

    /**
     * Property: Money and point cards are acquired (strategy should buy them)
     */
    @RepeatedTest(50)
    @DisplayName("WeightedPlayer acquires money or framework cards")
    public void testWeightedPlayerAcquiresStrategicCards() throws PlayerViolationException {
        WeightedPlayer weightedPlayer = new WeightedPlayer("Weighted");
        BigMoneyPlayer opponent1 = new BigMoneyPlayer("BigMoney1");
        BigMoneyPlayer opponent2 = new BigMoneyPlayer("BigMoney2");
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(weightedPlayer);
        players.add(opponent1);
        players.add(opponent2);
        
        Engine engine = new Engine(players);
        engine.play();
        
        Map<Card.Type, Integer> acquired = weightedPlayer.getAcquiredCards();
        
        // Property: Should acquire at least one of the strategic card types
        boolean hasMoney = acquired.getOrDefault(Card.Type.DOGECOIN, 0) > 0 ||
                          acquired.getOrDefault(Card.Type.ETHEREUM, 0) > 0 ||
                          acquired.getOrDefault(Card.Type.BITCOIN, 0) > 0;
        boolean hasPoints = acquired.getOrDefault(Card.Type.FRAMEWORK, 0) > 0 ||
                           acquired.getOrDefault(Card.Type.MODULE, 0) > 0 ||
                           acquired.getOrDefault(Card.Type.METHOD, 0) > 0;
        boolean hasCardDraw = acquired.getOrDefault(Card.Type.DAILY_SCRUM, 0) > 0 ||
                             acquired.getOrDefault(Card.Type.IPO, 0) > 0;
        
        assertTrue(hasMoney || hasPoints || hasCardDraw,
            "WeightedPlayer should acquire money, points, or card-draw cards");
    }

    // Helper assertion method
    private void assertGreaterThanOrEqual(int actual, int expected, String message) {
        assertTrue(actual >= expected, message + " (actual: " + actual + ", expected: >= " + expected + ")");
    }

    private void assertGreaterThan(int actual, int expected, String message) {
        assertTrue(actual > expected, message + " (actual: " + actual + ", expected: > " + expected + ")");
    }
}
