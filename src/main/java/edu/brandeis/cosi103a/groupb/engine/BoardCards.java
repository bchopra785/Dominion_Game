package edu.brandeis.cosi103a.groupb.engine;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
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

    // Creates a new deck with cards for ATG
    protected BoardCards(){

        // Victory Cards
        methods = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            
            methods.add(new Card(Card.Type.METHOD, i));
        }

        modules = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            modules.add(new Card(Card.Type.MODULE, i));
        }
        
        frameworks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            frameworks.add(new Card(Card.Type.FRAMEWORK, i));
        }
        // Currency Cards
        bitcoins = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            bitcoins.add(new Card(Card.Type.BITCOIN, i));
        }

        ethereums = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            ethereums.add(new Card(Card.Type.ETHEREUM, i));
        }

        dogecoins = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            dogecoins.add(new Card(Card.Type.DOGECOIN, i));
        }
        // Action cards
        refactors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            refactors.add(new Card(Card.Type.REFACTOR, i));
        }

        evergreens = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            evergreens.add(new Card(Card.Type.EVERGREEN_TEST, i));
        }

        codereviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codereviews.add(new Card(Card.Type.CODE_REVIEW, i));
        }

        bugs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            bugs.add(new Card(Card.Type.BUG, i));
        }
    }

    // For game state tracking and for engine to know available cards
    protected ImmutableMap<Card.Type, Integer> getCardStacks() {
        ImmutableMap<Card.Type, Integer> map = ImmutableMap.<Card.Type, Integer>builder()
            .put(Card.Type.METHOD, methods.size())
            .put(Card.Type.MODULE, modules.size())
            .put(Card.Type.FRAMEWORK, frameworks.size())
            .put(Card.Type.BITCOIN, bitcoins.size())
            .put(Card.Type.ETHEREUM, ethereums.size())
            .put(Card.Type.DOGECOIN, dogecoins.size())
            .put(Card.Type.REFACTOR, refactors.size())
            .put(Card.Type.EVERGREEN_TEST, evergreens.size())
            .put(Card.Type.CODE_REVIEW, codereviews.size())
            .put(Card.Type.BUG, bugs.size())
            .build();
        return map;
    }

    // Draws a card, should be returned to engine
    protected Card drawDeckCard(Card.Type t){
        switch (t) {
            case Card.Type.METHOD:
                if (!methods.isEmpty()) {
                    return methods.remove(0);
                }
                break;
            case Card.Type.MODULE:
                if (!modules.isEmpty()) {
                    return modules.remove(0);
                }
                break;
            case Card.Type.FRAMEWORK:
                if (!frameworks.isEmpty()) {
                    return frameworks.remove(0);
                }
                break;
            case Card.Type.BITCOIN:
                if (!bitcoins.isEmpty()) {
                    return bitcoins.remove(0);
                }
                break;
            case Card.Type.ETHEREUM:
                if (!ethereums.isEmpty()) {
                    return ethereums.remove(0);
                }
                break;
            case Card.Type.DOGECOIN:
                if (!dogecoins.isEmpty()) {
                    return dogecoins.remove(0);
                }
                break;
            case Card.Type.REFACTOR:
                if (!refactors.isEmpty()) {
                    return refactors.remove(0);
                }
                break;
            case Card.Type.EVERGREEN_TEST:
                if (!evergreens.isEmpty()) {
                    return evergreens.remove(0);
                }
                break;
            case Card.Type.CODE_REVIEW:
                if (!codereviews.isEmpty()) {
                    return codereviews.remove(0);
                }
                break;
            case Card.Type.BUG:
                if (!bugs.isEmpty()) {
                    return bugs.remove(0);
                }
                break;
        }
        return null; // No card available or invalid name
    }

    protected boolean frameworksLeft(){
        return frameworks.size() > 0;
    }

    // To call after an action card that trashes a card from player hand and moves it back to the board
    protected void trashCardToBoard(Card card) {
        Card.Type type = Card.Type.valueOf(card.description());
        switch (type) {
            case Card.Type.METHOD:
                methods.add(card);
                break;
            case Card.Type.MODULE:
                modules.add(card);
                break;
            case Card.Type.FRAMEWORK:
                frameworks.add(card);
                break;
            case Card.Type.BITCOIN:
                bitcoins.add(card);
                break;
            case Card.Type.ETHEREUM:
                ethereums.add(card);
                break;
            case Card.Type.DOGECOIN:
                dogecoins.add(card);
                break;
            case Card.Type.REFACTOR:
                refactors.add(card);
                break;
            case Card.Type.EVERGREEN_TEST:
                evergreens.add(card);
                break;
            case Card.Type.CODE_REVIEW:
                codereviews.add(card);
                break;
            case Card.Type.BUG:
                bugs.add(card);
                break;
        }
    }

    protected CardStacks getPlayableCards(int costInHand) {
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
