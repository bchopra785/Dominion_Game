package edu.brandeis.cosi103a.groupb.Prototypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrototypeBoard {
    public Map<String, List<PrototypeCard>> stockpiles;

    // Constructor - initializes all stockpiles with the specified quantities
    public PrototypeBoard() {
        stockpiles = new HashMap<>();

        // Initialize method cards (x14)
        List<PrototypeCard> methodCards = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            methodCards.add(new PrototypeCard(2, 0, 1, "method"));
        }
        stockpiles.put("method", methodCards);

        // Initialize module cards (x8)
        List<PrototypeCard> moduleCards = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            moduleCards.add(new PrototypeCard(5, 0, 3, "module"));
        }
        stockpiles.put("module", moduleCards);

        // Initialize framework cards (x8)
        List<PrototypeCard> frameworkCards = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            frameworkCards.add(new PrototypeCard(8, 0, 6, "framework"));
        }
        stockpiles.put("framework", frameworkCards);

        // Initialize bitcoin cards (x60)
        List<PrototypeCard> bitcoinCards = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            bitcoinCards.add(new PrototypeCard(0, 1, 0, "bitcoin"));
        }
        stockpiles.put("bitcoin", bitcoinCards);

        // Initialize ethereum cards (x40)
        List<PrototypeCard> ethereumCards = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            ethereumCards.add(new PrototypeCard(3, 2, 0, "ethereum"));
        }
        stockpiles.put("ethereum", ethereumCards);

        // Initialize dogecoin cards (x30)
        List<PrototypeCard> dogecoinCards = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            dogecoinCards.add(new PrototypeCard(6, 3, 0, "dogecoin"));
        }
        stockpiles.put("dogecoin", dogecoinCards);
    }

    // Remove and return a card from the specified stockpile
    public PrototypeCard removeCard(String cardType) {
        List<PrototypeCard> stockpile = stockpiles.get(cardType.toLowerCase());
        if (stockpile != null && !stockpile.isEmpty()) {
            return stockpile.remove(stockpile.size() - 1);
        }
        return null; // No cards left or invalid card type
    }

    // Check how many cards are left in the specified stockpile
    public int getCardsRemaining(String cardType) {
        List<PrototypeCard> stockpile = stockpiles.get(cardType.toLowerCase());
        return stockpile != null ? stockpile.size() : 0;
    }

    // Get all available card types
    public List<String> getAvailableCardTypes() {
        List<String> availableTypes = new ArrayList<>();
        for (String type : stockpiles.keySet()) {
            if (!stockpiles.get(type).isEmpty()) {
                availableTypes.add(type);
            }
        }
        return availableTypes;
    }

    // Check if a specific card type is available
    public boolean isCardTypeAvailable(String cardType) {
        return getCardsRemaining(cardType) > 0;
    }

    // Get all stockpiles (for debugging/inspection)
    public Map<String, List<PrototypeCard>> getAllStockpiles() {
        Map<String, List<PrototypeCard>> copy = new HashMap<>();
        for (String type : stockpiles.keySet()) {
            copy.put(type, new ArrayList<>(stockpiles.get(type)));
        }
        return copy;
    }

    // toString method for easy printing
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Board stockpiles:\n");
        for (String type : stockpiles.keySet()) {
            sb.append(type).append(": ").append(stockpiles.get(type).size()).append(" cards\n");
        }
        return sb.toString();
    }
}
