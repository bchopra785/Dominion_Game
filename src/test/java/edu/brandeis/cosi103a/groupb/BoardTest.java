package edu.brandeis.cosi103a.groupb;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

public class BoardTest {

    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void stockpilesAreInitializedCorrectly()
    {
        Board board = new Board();
        Map<String, List<Card>> stockpiles = board.stockpiles;

    
        assertNotNull(stockpiles, "stockpiles map should not be null");

        assertTrue(stockpiles.containsKey("method"), "should contain 'method' pile");
        assertTrue(stockpiles.containsKey("module"), "should contain 'module' pile");
        assertTrue(stockpiles.containsKey("framework"), "should contain 'framework' pile");
        assertTrue(stockpiles.containsKey("bitcoin"), "should contain 'bitcoin' pile");
        assertTrue(stockpiles.containsKey("ethereum"), "should contain 'ethereum' pile");
        assertTrue(stockpiles.containsKey("dogecoin"), "should contain 'dogecoin' pile");

        assertEquals(14, stockpiles.get("method").size(), "method pile size");
        assertEquals(8, stockpiles.get("module").size(), "module pile size");
        assertEquals(8, stockpiles.get("framework").size(), "framework pile size");
        assertEquals(60, stockpiles.get("bitcoin").size(), "bitcoin pile size");
        assertEquals(40, stockpiles.get("ethereum").size(), "ethereum pile size");
        assertEquals(30, stockpiles.get("dogecoin").size(), "dogecoin pile size");
    }

    @Test
    public void testRemoveCard(){
        Board board = new Board();
        Map<String, List<Card>> stockpiles = board.stockpiles;

        assertEquals(14, stockpiles.get("method").size(), "method pile size");
        Card removedCard = board.removeCard("method");
        assertNotNull(removedCard, "removed card should not be null");
        assertEquals(13, stockpiles.get("method").size(), "method pile size after removal");
    }


    @Test
    public void testGetCardsRemaining(){
        Board board = new Board();

        assertEquals(14, board.getCardsRemaining("method"), "method pile size");
        assertEquals(8, board.getCardsRemaining("module"), "module pile size");
        assertEquals(8, board.getCardsRemaining("framework"), "framework pile size");
        assertEquals(60, board.getCardsRemaining("bitcoin"), "bitcoin pile size");
        assertEquals(40, board.getCardsRemaining("ethereum"), "ethereum pile size");
        assertEquals(30, board.getCardsRemaining("dogecoin"), "dogecoin pile size");

        for (int i = 0; i < 5; i++) {
            board.removeCard("method");
        }
        assertEquals(9, board.getCardsRemaining("method"), "method pile size after removals");
    }


    @Test
    public void testGetAvailableCardTypes(){
        Board board = new Board();
        List<String> availableTypes = board.getAvailableCardTypes();

        assertEquals(6, availableTypes.size(), "all card types should be available initially");
        assertTrue(availableTypes.contains("method"), "should contain 'method'");
        assertTrue(availableTypes.contains("module"), "should contain 'module'");
        assertTrue(availableTypes.contains("framework"), "should contain 'framework'");
        assertTrue(availableTypes.contains("bitcoin"), "should contain 'bitcoin'");
        assertTrue(availableTypes.contains("ethereum"), "should contain 'ethereum'");
        assertTrue(availableTypes.contains("dogecoin"), "should contain 'dogecoin'");

        for (int i = 0; i < 14; i++) {
            board.removeCard("method");
        }
        availableTypes = board.getAvailableCardTypes();
        assertEquals(5, availableTypes.size(), "one card type should be unavailable after removing all method cards");
        assertFalse(availableTypes.contains("method"), "should not contain 'method' after all removed");
    }

    @Test
    public void testIsCardTypeAvailable(){
        Board board = new Board();
        assertTrue(board.isCardTypeAvailable("method"), "'method' should be available initially");
        for (int i = 0; i < 14; i++) {
            board.removeCard("method");
        }
        assertFalse(board.isCardTypeAvailable("method"), "'method' should not be available after removing all cards");
    }

    @Test
    public void testGetAllStockpiles() {
        Board board = new Board();
        Map<String, List<Card>> stockpiles = board.getAllStockpiles();
        assertNotNull(stockpiles, "getAllStockpiles should not return null");
        assertTrue(stockpiles.containsKey("method"), "should contain 'method'");
        assertTrue(stockpiles.containsKey("bitcoin"), "should contain 'bitcoin'");
    }

    @Test
    public void testToString() {
        Board board = new Board();
        String str = board.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("method"), "toString should mention 'method'");
        assertTrue(str.contains("bitcoin"), "toString should mention 'bitcoin'");
    }

}