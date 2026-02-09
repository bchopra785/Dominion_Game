

import org.junit.jupiter.api.Test;

import edu.brandeis.cosi103a.groupb.Prototypes.PrototypeCard;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    public void testConstructor() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        assertNotNull(card, "Card should be created successfully");
    }

    @Test
    public void testGetCost() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        assertEquals(0, card.getCost(), "getCost should return the correct cost");
    }

    @Test
    public void testGetValue() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        assertEquals(1, card.getValue(), "getValue should return the correct value");
    }

    @Test
    public void testGetType() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        assertEquals("bitcoin", card.getType(), "getType should return the correct type");
    }

    @Test
    public void testGetPoints() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        assertEquals(0, card.getPoints(), "getPoints should return the correct points");
    }

    @Test
    public void testSetCost() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        card.setCost(7);
        assertEquals(7, card.getCost(), "setCost should update the cost correctly");
    }

    @Test
    public void testSetValue() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        card.setValue(15);
        assertEquals(15, card.getValue(), "setValue should update the value correctly");
    }

    @Test
    public void testSetType() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        card.setType("ethereum");
        assertEquals("ethereum", card.getType(), "setType should update the type correctly");
    }

    @Test
    public void testSetPoints() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        card.setPoints(5);
        assertEquals(5, card.getPoints(), "setPoints should update the points correctly");
    }

    @Test
    public void testToString() {
        PrototypeCard card = new PrototypeCard(0, 1, 0, "bitcoin");
        String expected = "Card: bitcoin  cost: 0, value: 1, points: 0\n";
        assertEquals(expected, card.toString(), "toString should return the correct string representation");
    }
}
