package edu.brandeis.cosi103a.groupb.engine;
import edu.brandeis.cosi.atg.cards.Card;

import java.util.List;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerCards {
    private List<Card> hand;
    private List<Card> discard;
    private BoardCards board; // needed for case of trashing card action to the board

    protected PlayerCards(BoardCards board) {
        this.hand = new ArrayList<>();
        this.discard = new ArrayList<>();
        this.board = board;

        for(int i = 0; i < 7; i++) {
            this.discard.add(new Card(Card.Type.BITCOIN, i));
        }
        for(int i = 0; i < 3; i++) {
            this.discard.add(new Card(Card.Type.METHOD, i));
        }
    }

    // Get record of cards in hand for game state
    protected ImmutableList<Card> getHand() {
        ImmutableList<Card> h = ImmutableList.copyOf(hand);
        return h;
    }

    // REFRESH DECK METHODS
    // Draws a card to hand and returns true if successful, false if no cards to draw
    protected boolean drawToHand() {
        if (!discard.isEmpty()) {
            hand.add(discard.remove(0));
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
        discard.addAll(hand);
        hand.clear();
    }

    // Refresh deck and draw 5 new cards
    protected void refreshHand() {
        returnAllToDeck();
        shuffleCards();

        // Draw back 5 cards to hand
        while (hand.size() < 5) {
            if (!drawToHand()) {
                break; // No more cards to draw
            }
        }
    }
    // ACTIONS
    // Should be called by engine for gaining a card
    protected void gainCard(Card card) {
        discard.add(card);
    }

    // Trash card (action) moves a card from player deck back to the Board
    protected void trashCard(Card card) throws IllegalArgumentException{
        if(hand.contains(card)) {
            hand.remove(card);
            this.board.trashCardToBoard(card);
        }else{
            throw new IllegalArgumentException("Card not in hand");
        }
    }

    // Get cost of currency cards in hand for checking if player can play a card
    protected int getCostInHand() {
        int costInHand = 0;
        for (Card c: this.hand) {
            if(c.type() == Card.Type.BITCOIN || c.type() == Card.Type.ETHEREUM || c.type() == Card.Type.DOGECOIN) {
                costInHand += c.cost();
            }
            costInHand += c.cost();
        }
    return costInHand;
    }






}
