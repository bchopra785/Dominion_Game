package edu.brandeis.cosi103a.groupb;


import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//RUN  [mvn spring-boot:run]before running this method to start server
//run game harness in another terminal to start the game and connect remote players
public class GameHarness {

    public static void main(String[] args) throws PlayerViolationException {
        Scanner scanner = new Scanner(System.in);
        
        // Create players
        List<ParentPlayer> players = new ArrayList<>();
        // Prompt for valid number of console players
        int minPlayers = 2; // minimum number of players in a game
        int maxPlayers = 4; //change this to change max number of players in the game
        int numPlayers = 0;

        List<Integer> selectedPlayers = new ArrayList<>();
        Map<Integer, String> playerOptions = new HashMap<>();
            playerOptions.put(1, "Console Player");
            playerOptions.put(2, "Automated Player");
            playerOptions.put(3, "Remote Player");

        System.out.println("Welcome to the Dominion Game Harness!");
        System.out.println("You must have a minimum of " + minPlayers + " players to start a game.");
        System.out.println("You can have up to " + maxPlayers + " players in a game.");

        while(numPlayers < minPlayers || numPlayers > maxPlayers) {
            System.out.println("Please select a player type for player " + (numPlayers + 1));

            System.out.println("[0] No more players (start game)");
            for (Map.Entry<Integer, String> entry : playerOptions.entrySet()) {
                System.out.println("[" + entry.getKey() + "] " + entry.getValue());
            }
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            if(choice == 0) {
                if(numPlayers < minPlayers) {
                    System.out.println("You must have at least " + minPlayers + " players to start the game.");
                } else {
                    break; // exit loop and start game
                }
            } else if (playerOptions.containsKey(choice)) {
                numPlayers++;
                selectedPlayers.add(choice);
            } else {
                System.out.println("Invalid choice. Please select a valid player type.");
            }

        }

        for (int choice : selectedPlayers) {
            if(choice == 1) {
                players.add(new ConsolePlayer(scanner, System.out));
            } else if (choice == 2) {
                players.add(new BigMoneyPlayer());
            } else if (choice == 3) {
                String uuid = java.util.UUID.randomUUID().toString();
                players.add(new PlayerClient("Remote Player", uuid, "http://localhost:8080"));
            }
        }

        // Create and run game
        Engine engine = new Engine(players);
        GameResult result = engine.play();
        System.out.println("\n\n\n\n------------------------------");
        System.out.println("Game Results:");
        for (PlayerResult playerResult : result.playerResults()) {
            //System.out.println(playerResult);
            System.out.println(playerResult.playerName() + ": " + playerResult.score() + " points");
        }
        
        scanner.close();
    }
}
