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
    private List<Card> deck;
    private BoardCards board;
    private List<Card> playedCards; // for tracking cards played in the current turn
    private List<Card> unplayedCards; // for tracking cards in hand that have not been

    public PlayerCards(BoardCards board) { //It's really hard to test with protected, should ask TA's about this
                                           //the internet says not to use reflection and subclasses for testing so idk
        this.discard = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.board = board;
        this.playedCards = new ArrayList<>();
        this.unplayedCards = new ArrayList<>();

        for(int i = 0; i < 7; i++) {
            Card c = this.board.drawDeckCard(Card.Type.BITCOIN);
            this.deck.add(c);
        }
        for(int i = 0; i < 3; i++) {
            Card c = this.board.drawDeckCard(Card.Type.METHOD);
            this.deck.add(c);
        }

        shuffleCards(); // need to shuffle cards at the beginning
        refreshHand(); // draw initial hand of 5 cards
    }

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
        if (!deck.isEmpty()) {
            Card card = deck.remove(0);
            unplayedCards.add(card);
            return true;
        }
        return false;
    }

    // Shuffles cards in the deck, called internally
    private void shuffleCards(){
        Collections.shuffle(this.deck);
    }

    // Moves cards from hand to discard to deck
    protected void refreshHand() {
        discard.addAll(playedCards); 
        discard.addAll(unplayedCards);
        playedCards.clear();
        unplayedCards.clear();

        while (unplayedCards.size() < 5) {
            if (!drawToHand()) {
                deck.addAll(discard); // No more cards to draw, move discard to deck and shuffle
                discard.clear();
                shuffleCards();
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
            unplayedCards.remove(card); // card is just trashed and removed from the game 
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
    // Moves all cards to discard for end game cleanup
    protected int getScore() {
        discard.addAll(playedCards); 
        discard.addAll(unplayedCards);
        playedCards.clear();
        unplayedCards.clear();

        deck.addAll(discard);
        discard.clear();

        int victoryPoints = 0;
        // Check all cards in deck for victory cards and sum their values
        for (Card c : this.deck) {
            if (c.type() == Card.Type.METHOD || c.type() == Card.Type.MODULE || c.type() == Card.Type.FRAMEWORK) {
                victoryPoints += c.value();
            }
        }
        return victoryPoints;
    }

    // Used only for endgame to present all cards
    protected ImmutableCollection<Card> getDiscardPile() {

        return ImmutableList.copyOf(this.discard);
    }

    // Get all unplayed cards in hand as an immutable collection
    protected ImmutableCollection<Card> getUnplayedCards() {
        return ImmutableList.copyOf(this.unplayedCards);
    }

}
