package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.List;

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

        boolean gameOver = false;
        while (!gameOver) {
            for (Player player : players) {
                //get decision from player
                
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

    public static void main(String[] args) {
        //TO DO: instantiate players
        Player player1 = null;
        Player player2 = null;
        Player player3 = null;
        Player player4 = null;

        List<Player> players = List.of(player1, player2, player3, player4);
        
        Engine engine = new Engine(players);
    }     
}
