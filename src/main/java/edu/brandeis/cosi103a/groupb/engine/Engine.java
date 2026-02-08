package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.*;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.*;
import edu.brandeis.cosi.atg.player.*;
import edu.brandeis.cosi.atg.state.*;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class Engine implements edu.brandeis.cosi.atg.engine.Engine {

    private final List<Player> players;

    public Engine(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > 4) {
            throw new IllegalArgumentException("Engine supports at most 4 players");
        }
        this.players = players;

        //TO DO: initialize cards

    }

    public GameResult play() throws PlayerViolationException {
        BoardCards boardCards = new BoardCards();
    
        boolean gameOver = false;
        while (!gameOver) {
            for (Player player : players) {

                

                //draw a new hand
                ImmutableCollection<Card> playedcards = null; //placeholder
                ImmutableCollection<Card> unplayedCards = null; //placeholder
                Hand hand = new Hand(playedcards, unplayedCards);
                ImmutableCollection<Card> cards = hand.getAllCards();

                //get decision from player
                List<Decision> decisions = new ArrayList<>();
                List<Card> actionCards = new ArrayList<>();
                int actionCardAmt = 0;
                List<Card> spendCards = new ArrayList<>();
                int totalMoney = 0;
                int availableBuys = 1;


                for(Card card: cards){
                    Card.Type.Category category = Card.Type.Category.valueOf(card.description());

                    if(category.equals(Card.Type.Category.ACTION)){
                        actionCards.add(card);
                        actionCardAmt++;
                    } else if(category.equals(Card.Type.Category.MONEY)){
                        totalMoney += card.value();
                        spendCards.add(card);
                    }
                }

                //ACTION PHASE
                CardStacks buyableCards = boardCards.getPlayableCards(totalMoney);
                ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
                
                // Add PlayCardDecision for each action card
                for (int i = 0; i < actionCardAmt; i++) {
                    optionsBuilder.add(new PlayCardDecision(actionCards.get(i)));
                }
                
                // Add single EndPhaseDecision
                optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
                
                ImmutableList<Decision> options = optionsBuilder.build();
                  
                GameState state = new GameState(player.getName(), hand, GameState.TurnPhase.ACTION, actionCardAmt, totalMoney, availableBuys, buyableCards);
                Decision decision = player.makeDecision(state, options);
                //if refactor, gain a card costing up to 2 more than trashed card
                //if evergreen_test, each other player gains a bug
                
                //ACTION PHASE
                
                //MONEY PHASE

                //BUY PHASE

                //CLEANUP PHASE
            }
            //check framework cards left
            gameOver = true; //placeholder
        }
        throw new UnsupportedOperationException("Game engine not implemented yet");
    }

    public Card.Type getCardType(Card card) {
        Card.Type type = Card.Type.valueOf(card.description());
        return type;
    }

    public Card.Type.Category getCardCategory(Card card) {
        Card.Type.Category category = Card.Type.Category.valueOf(card.description());
        return category;
    }

    public static void main(String[] args) {
        //Card card = new Card(Card.Type.BITCOIN, 2);
        //TO DO: instantiate players
        Player player1 = null;
        Player player2 = null;
        Player player3 = null;
        Player player4 = null;

        List<Player> players = List.of(player1, player2, player3, player4);
        
        Engine engine = new Engine(players);
    }     
    
}
