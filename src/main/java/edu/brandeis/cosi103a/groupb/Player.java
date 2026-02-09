package edu.brandeis.cosi103a.groupb;



import java.util.ArrayList;
import java.util.List;

public class Player {
    private Deck deck;
    private int points;
    private List<Card> hand;
    public String name;

    // Constructor
    public Player(Deck deck, String name) {
        this.deck = deck;
        this.points = 0;
        this.hand = new ArrayList<>();
        this.name = name;
    }

    // Draw 5 cards for a new turn (can be called at the start of each turn)
    public List<Card> drawTurnHand() {
        for (int i = 0; i < 5; i++) {
            Card drawnCard = deck.drawCard();
            if (drawnCard != null) {
                hand.add(drawnCard);
            }
        }
        return new ArrayList<>(hand);
    }

    public void clearHand() {
        for (Card card : hand) {
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
    public Deck getDeck() {
        return deck;
    }

    public int getPoints() {
        return points;
    }

    public List<Card> getHand() {
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
