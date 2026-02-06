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

    // Creates a new deck with cards for ATG
    protected Deck(){

        methods = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            methods.add(new Card(, i));
        }

        modules = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            modules.add(new Card("Module", Card.Type.AUTOMATION, 5, 3));
        }

        frameworks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            frameworks.add(new Card("Framework", Card.Type.AUTOMATION, 8, 6));
        }

        bitcoins = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            bitcoins.add(new Card("Bitcoin", Card.Type.CRYPTOCURRENCY, 0, 1));
        }

        ethereums = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            ethereums.add(new Card("Ethereum", Card.Type.CRYPTOCURRENCY, 3, 2));
        }

        dogecoins = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            dogecoins.add(new Card("Dogecoin", Card.Type.CRYPTOCURRENCY, 6, 3));
        }
        throw new UnsupportedOperationException("Deck not implemented yet");
    }

    protected Card drawDeckCard(String description){
        throw new UnsupportedOperationException("Draw not implemented yet");
    }

    protected boolean frameworksLeft(){
        throw new UnsupportedOperationException("Frameworks left check not implemented yet");
    }

}
