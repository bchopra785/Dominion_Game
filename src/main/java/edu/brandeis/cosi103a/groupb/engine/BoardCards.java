package edu.brandeis.cosi103a.groupb.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;


public class BoardCards {
    // Store piles of different cards that players can buy/pickup

    public List<Card> methods;
    public List<Card> modules;
    public List<Card> frameworks;
    public List<Card> bitcoins;
    public List<Card> ethereums;
    public List<Card> dogecoins;
    public List<Card> refactors;
    public List<Card> evergreens;
    public List<Card> codereviews;
    public List<Card> bugs;

    public Map<Card.Type, List<Card>> cardMap; // for easier access to card piles by type
    public Map<Card.Type, Integer> cardsLeft; // for tracking number of cards left

    // Creates a new deck with cards for ATG
    public BoardCards(){
        // build each pile via the helper and stash in a single map
        cardMap = new HashMap<>();
        cardMap.put(Card.Type.METHOD, createStack(Card.Type.METHOD, 14));
        cardMap.put(Card.Type.MODULE, createStack(Card.Type.MODULE, 8));
        cardMap.put(Card.Type.FRAMEWORK, createStack(Card.Type.FRAMEWORK, 8));

        cardMap.put(Card.Type.BITCOIN, createStack(Card.Type.BITCOIN, 60));
        cardMap.put(Card.Type.ETHEREUM, createStack(Card.Type.ETHEREUM, 40));
        cardMap.put(Card.Type.DOGECOIN, createStack(Card.Type.DOGECOIN, 30));

        cardMap.put(Card.Type.REFACTOR, createStack(Card.Type.REFACTOR, 10));
        cardMap.put(Card.Type.EVERGREEN_TEST, createStack(Card.Type.EVERGREEN_TEST, 10));
        cardMap.put(Card.Type.CODE_REVIEW, createStack(Card.Type.CODE_REVIEW, 10));
        cardMap.put(Card.Type.BUG, createStack(Card.Type.BUG, 10));

        // keep the old references for backwards compatibility/tests
        methods = cardMap.get(Card.Type.METHOD);
        modules = cardMap.get(Card.Type.MODULE);
        frameworks = cardMap.get(Card.Type.FRAMEWORK);
        bitcoins = cardMap.get(Card.Type.BITCOIN);
        ethereums = cardMap.get(Card.Type.ETHEREUM);
        dogecoins = cardMap.get(Card.Type.DOGECOIN);
        refactors = cardMap.get(Card.Type.REFACTOR);
        evergreens = cardMap.get(Card.Type.EVERGREEN_TEST);
        codereviews = cardMap.get(Card.Type.CODE_REVIEW);
        bugs = cardMap.get(Card.Type.BUG);
    }

    // Only method that should create cards
    private List<Card> createStack(Card.Type type, int count) {
        List<Card> stack = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            stack.add(new Card(type, i));
        }
        return stack;
    }
    // For game state tracking and for engine to know available cards
    protected ImmutableMap<Card.Type, Integer> getCardStacks() {
        ImmutableMap.Builder<Card.Type, Integer> builder = ImmutableMap.builder();
        for (Map.Entry<Card.Type, List<Card>> entry : cardMap.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().size());
        }
        return builder.build();
    }

    // Draws a card, should be returned to engine
    public Card drawDeckCard(Card.Type t){
        List<Card> stack = cardMap.get(t);
        if (stack != null && !stack.isEmpty()) {
            return stack.remove(0);
        }
        return null; // No card available or invalid name
    }

    protected boolean frameworksLeft(){
        List<Card> stack = cardMap.get(Card.Type.FRAMEWORK);
        return stack != null && stack.size() > 0;
    }

    // To call after an action card that trashes a card from player hand and moves it back to the board
    protected void trashCardToBoard(Card card) {
        Card.Type type = card.type();
        List<Card> stack = cardMap.get(type);
        if (stack != null) {
            stack.add(card);
        }
    }

    //shouldn't this be called buyablecards?
    public CardStacks getPlayableCards(int costInHand) {
        // For now, all cards in hand are playable, but this can be changed to check for action cards and other conditions
        ImmutableMap<Card.Type, Integer> cardStacks = getCardStacks();

        ImmutableMap.Builder<Card.Type, Integer> playableCardsBuilder = ImmutableMap.builder();
        
        for(Card.Type t: cardStacks.keySet()){
            // Need to have the card and have enough cost in hand to play it
            if ((cardStacks.get(t) > 0) & (costInHand >= t.cost())) {
                playableCardsBuilder.put(t, cardStacks.get(t));
            }
        }
        return new CardStacks(playableCardsBuilder.build());
    }

}
