package edu.brandeis.cosi103a.groupb.engine;

import java.util.ArrayList;
import java.util.List;

import edu.brandeis.cosi.atg.cards.Card;


public class Deck {
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

    // Creates a new deck with cards for ATG
    protected Deck(){

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
    }

    protected Card drawDeckCard(String description){
        throw new UnsupportedOperationException("Draw not implemented yet");
    }

    protected boolean frameworksLeft(){
        return frameworks.size() > 0;
    }

}
