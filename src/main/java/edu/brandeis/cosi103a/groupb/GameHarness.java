package edu.brandeis.cosi103a.groupb;


import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameHarness {

    public static void main(String[] args) throws PlayerViolationException {
        Scanner scanner = new Scanner(System.in);
        
        // Create players
        List<ParentPlayer> players = new ArrayList<>();
        // Prompt for valid number of console players
        int numPlayers = 0;
        while(numPlayers < 1 || numPlayers > 4) {
                System.out.println("How many console players? (1-4)");
                numPlayers = scanner.nextInt();
                scanner.nextLine(); // consume newline
                if(numPlayers < 1 || numPlayers > 4) {
                    System.out.println("Invalid number of players. Must be between 1 and 4.");
                }
        }

        // Add console players
        for (int i = 0; i < numPlayers; i++) {
            players.add(new ConsolePlayer(scanner, System.out));
        }
        int maxRemainingPlayers = 4 - numPlayers;
        int numAutoPlayers = -1; // invalid initial amount
        // Add automated players if desired (optional)
        while(numAutoPlayers < 0 || numAutoPlayers > maxRemainingPlayers) {
                System.out.println("How many automated players? (0-" + maxRemainingPlayers + ")");
                numAutoPlayers = scanner.nextInt();
                scanner.nextLine(); // consume newline
                if(numAutoPlayers < 0 || numAutoPlayers > maxRemainingPlayers) {
                    System.out.println("Invalid number of automated players. Must be between 0 and " + maxRemainingPlayers + ".");
                }
        }

        // Add automated big money players (optional)
        for (int i = 0; i < numAutoPlayers; i++) {
            players.add(new BigMoneyPlayer());
        }

        // Create and run game
        Engine engine = new Engine(players);
        engine.play();
        
        scanner.close();
    }
}
