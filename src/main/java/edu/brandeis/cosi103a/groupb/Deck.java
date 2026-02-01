package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> drawPile;
    private List<Card> discardPile;

    // Constructor
    public Deck() {
        this.drawPile = new ArrayList<>();
        this.discardPile = new ArrayList<>();
    }

    // Constructor with initial cards
    public Deck(List<Card> initialCards) {
        this.drawPile = new ArrayList<>(initialCards);
        this.discardPile = new ArrayList<>();
    }

    public Card drawCard() {
        if (drawPile.isEmpty()) {
            System.out.println("Draw pile is empty, reshuffling from discard pile.");
            reshuffleFromDiscard();
            
        }
        return drawPile.remove(drawPile.size() - 1);
    }

    // Add a card to the draw pile
    public void addToDrawPile(Card card) {
        drawPile.add(card);
    }

    // Add a card to the discard pile
    public int addToDiscard(Card card) {
        discardPile.add(card);
        return card.getPoints();
    }


    // Shuffle the draw pile
    public void shuffleDrawPile() {
        Collections.shuffle(drawPile);
    }

    // Move all cards from discard pile back to draw pile and shuffle
    public void reshuffleFromDiscard() {
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffleDrawPile();
    }

    // Getters
    public List<Card> getDrawPile() {
        return new ArrayList<>(drawPile); // return a copy to prevent external modification
    }

    public List<Card> getDiscardPile() {
        return new ArrayList<>(discardPile); // return a copy to prevent external modification
    }

    // Get the number of cards in each pile
    public int getDrawPileSize() {
        return drawPile.size();
    }

    public int getDiscardSize() {
        return discardPile.size();
    }

    // Check if draw pile is empty
    public boolean isDrawPileEmpty() {
        return drawPile.isEmpty();
    }

    // toString method for easy printing
    @Override
    public String toString() {
        return "Deck{" +
                "drawPile=" + drawPile.size() + " cards" +
                ", discardPile=" + discardPile.size() + " cards" +
                '}';
    }

    
}
