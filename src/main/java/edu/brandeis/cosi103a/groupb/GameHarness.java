package edu.brandeis.cosi103a.groupb;


import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameHarness {

    public static void main(String[] args) throws PlayerViolationException {
        Scanner scanner = new Scanner(System.in);
        
        // Create players
        List<ConsolePlayer> players = new ArrayList<>();
        System.out.println("How many players? (1-4)");
        int numPlayers = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        if (numPlayers < 1 || numPlayers > 4) {
            System.out.println("Invalid number of players. Must be between 1 and 4.");
            scanner.close();
            return;
        }
        
        for (int i = 0; i < numPlayers; i++) {
            players.add(new ConsolePlayer(scanner, System.out));
        }
        
        // Create and run game
        Engine engine = new Engine(players);
        engine.play();
        
        scanner.close();
    }
}
