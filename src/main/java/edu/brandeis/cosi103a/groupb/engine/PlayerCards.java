package edu.brandeis.cosi103a.groupb.engine;

import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.Hand;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerCards {
    //private List<Card> hand;
    private List<Card> discard;
    private BoardCards board; // needed for case of trashing card action to the board
    private List<Card> playedCards; // for tracking cards played in the current turn
    private List<Card> unplayedCards; // for tracking cards in hand that have not been

    protected PlayerCards(BoardCards board) {
        //this.hand = new ArrayList<>();
        this.discard = new ArrayList<>();
        this.board = board;
        this.playedCards = new ArrayList<>();
        this.unplayedCards = new ArrayList<>();

        for(int i = 0; i < 7; i++) {
            this.discard.add(new Card(Card.Type.BITCOIN, i)); //doesnt this need to come from board cards?
        }
        for(int i = 0; i < 3; i++) {
            this.discard.add(new Card(Card.Type.METHOD, i));
        }
    }
    // Get record of cards in hand for game state
    // protected ImmutableList<Card> getHand() {
        
    //     ImmutableList<Card> h = ImmutableList.copyOf(hand);
    //     return h;
    // }

    //Get record class Hand
    public Hand getHand() {
        ImmutableCollection<Card> playedCardsImmutable = ImmutableList.copyOf(playedCards);
        ImmutableCollection<Card> unplayedCardsImmutable = ImmutableList.copyOf(unplayedCards);
        
        Hand hand = new Hand(playedCardsImmutable, unplayedCardsImmutable);
        return hand;
    }

    // REFRESH DECK METHODS
    // Draws a card to hand and returns true if successful, false if no cards to draw
    public boolean drawToHand() {
        if (!discard.isEmpty()) {
            Card card = discard.remove(0);
            unplayedCards.add(card);
            //hand.add(card);
            return true;
        }
        return false;
    }

    // Shuffles cards in the discard pile, called internally
    private void shuffleCards(){
        Collections.shuffle(this.discard);
    }

    // Moves cards from hand to discard, called internally
    private void returnAllToDeck() {
        discard.addAll(playedCards);
        discard.addAll(unplayedCards);
        playedCards.clear();
        unplayedCards.clear();
        // discard.addAll(hand);
        // hand.clear();
    }

    // Refresh deck and draw 5 new cards
    protected void refreshHand() {
        returnAllToDeck();
        shuffleCards();

        // Draw back 5 cards to hand
        while (unplayedCards.size() < 5) {
            if (!drawToHand()) {
                break; // No more cards to draw
            }
        }
    }
    // ACTIONS
    // Should be called by engine for gaining a card
    public void gainCard(Card card) {
        discard.add(card);
    }

    // Trash card (action) moves a card from player deck back to the Board
    public void trashCard(Card card) throws IllegalArgumentException{
        if(unplayedCards.contains(card)) {
            unplayedCards.remove(card);
            this.board.trashCardToBoard(card);
        }else{
            throw new IllegalArgumentException("Card not in hand");
        }
    }

    public void playCard(Card card) throws IllegalArgumentException {
        if(unplayedCards.contains(card)) {
            unplayedCards.remove(card);
            playedCards.add(card);
        }else{
            throw new IllegalArgumentException("Card not in hand");
        }
    }

    // Get cost of currency cards in hand for checking if player can play a card
    public int getCostInHand() {
        int costInHand = 0;
        for (Card c: this.unplayedCards) {
            if (c.type() == Card.Type.BITCOIN || c.type() == Card.Type.ETHEREUM || c.type() == Card.Type.DOGECOIN) {
                costInHand += c.value(); //cahnged to value and removed the second count
            }
        }
    return costInHand;
    }

    // Get total value of victory cards (METHOD, MODULE, FRAMEWORK)
    protected int getScore() {
        returnAllToDeck();
        int victoryPoints = 0;
        // Check all cards in discard pile
        for (Card c : this.discard) {
            if (c.type() == Card.Type.METHOD || c.type() == Card.Type.MODULE || c.type() == Card.Type.FRAMEWORK) {
                victoryPoints += c.value();
            }
        }
        return victoryPoints;
    }

    // Get all cards in the discard pile as an immutable collection
    protected ImmutableCollection<Card> getDiscardPile() {
        returnAllToDeck();
        return ImmutableList.copyOf(this.discard);
    }

    // Get all unplayed cards in hand as an immutable collection
    protected ImmutableCollection<Card> getUnplayedCards() {
        return ImmutableList.copyOf(this.unplayedCards);
    }

}
