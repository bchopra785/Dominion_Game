package edu.brandeis.cosi103a.groupb.Prototypes;

import java.util.ArrayList;
import java.util.List;

public class PrototypePlayer {
    private PrototypeDeck deck;
    private int points;
    private List<PrototypeCard> hand;
    public String name;

    // Constructor
    public PrototypePlayer(PrototypeDeck deck, String name) {
        this.deck = deck;
        this.points = 0;
        this.hand = new ArrayList<>();
        this.name = name;
    }

    // Draw 5 cards for a new turn (can be called at the start of each turn)
    public List<PrototypeCard> drawTurnHand() {
        for (int i = 0; i < 5; i++) {
            PrototypeCard drawnCard = deck.drawCard();
            if (drawnCard != null) {
                hand.add(drawnCard);
            }
        }
        return new ArrayList<>(hand);
    }

    public void clearHand() {
        for (PrototypeCard card : hand) {
            if (card != null) {
                deck.addToDiscard(card);
            }
        }
        hand.clear();
    }

    // Add points to the player's score
    public void addPoints(int points) {
        this.points += points;
    }

    // Getters
    public PrototypeDeck getDeck() {
        return deck;
    }

    public int getPoints() {
        return points;
    }

    public List<PrototypeCard> getHand() {
        return new ArrayList<>(hand); // return a copy to prevent external modification
    }

    // public int getHandSize() {
    //     return hand.size();
    // }

    // // Check if player can draw cards
    // public boolean canDrawCard() {
    //     return !deck.isDrawPileEmpty();
    // }

    // toString method for easy printing
    @Override
    public String toString() {
        return "Player{" +
                "points=" + points +
                ", hand=" + hand.size() + " cards" +
                ", deck=" + deck.getDrawPileSize() + " cards remaining" +
                '}';
    }
}
