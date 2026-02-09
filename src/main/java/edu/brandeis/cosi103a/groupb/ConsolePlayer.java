package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
/**PULL BEFORE MERGIN */
/**
 * Console-based Player that reads choices from an input stream and writes prompts to a print stream.
 *
 * Zero-arg constructor required by the Engine delegates to System.in/System.out.
 */
public class ConsolePlayer implements Player {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final String name;
    private final Scanner scanner;
    private final PrintStream out;

    // Zero-arg constructor required by Engine
    public ConsolePlayer() {
        this(System.in, System.out);
    }

    // Package-private constructor for tests (inject streams)
    ConsolePlayer(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
        this.name = "ConsolePlayer-" + COUNTER.getAndIncrement();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        try {
            if (options == null || options.isEmpty()) {
                out.println("No available options; returning null");
                return null;
            }

            // Display game information to the player
            out.println("\n========== YOUR TURN ==========");
            displayHandAndDeck(state);
            out.println();
            displayGameState(state);
            out.println();
            displayDecisionOptions(options);
            out.println();

            // Get player input
            while (true) {
                out.print("Enter option index: ");
                if (!scanner.hasNextLine()) {
                    out.println();
                    out.println("Input closed; selecting default option 0");
                    return options.get(0);
                }
                String line = scanner.nextLine().trim();
                try {
                    int idx = Integer.parseInt(line);
                    if (idx >= 0 && idx < options.size()) {
                        return options.get(idx);
                    }
                } catch (NumberFormatException ignored) {
                }
                out.println("Invalid input. Please enter a number between 0 and " + (options.size() - 1));
            }
        } catch (Exception e) {
            out.println("Error reading input; selecting default option 0");
            return options.isEmpty() ? null : options.get(0);
        }
    }

    /**
     * Displays the player's current hand and deck information.
     */
    private void displayHandAndDeck(GameState state) {
        out.println("--- Hand & Deck ---");
        if (state.currentPlayerHand() != null) {
            var hand = state.currentPlayerHand();
            var allCards = hand.getAllCards();
            out.println("Hand: " + allCards.size() + " cards");
            for (var card : allCards) {
                out.println("  - " + card.description());
            }
        } else {
            out.println("Hand: Empty");
        }
    }

    /**
     * Displays the current game state including turn phase, actions, money, and buys.
     */
    private void displayGameState(GameState state) {
        out.println("--- Game State ---");
        out.println("Turn Phase: " + state.phase());
        out.println("Spendable Money: " + state.spendableMoney());
        out.println("Available Buys: " + state.availableBuys());
        out.println("Actions Remaining: " + state.availableActions());
}
    }

    /**
     * Displays all available decision options in a numbered list.
     */
    private void displayDecisionOptions(ImmutableList<Decision> options) {
        out.println("--- Available Actions ---");
        for (int i = 0; i < options.size(); i++) {
            String description = describeDecision(options.get(i));
            out.printf("[%d] %s%n", i, description);
        }
    }

    /**
     * Converts a Decision object into a human-readable string description.
     */
    private String describeDecision(Decision decision) {
        if (decision instanceof EndPhaseDecision endPhase) {
            return "End Phase: " + endPhase.phase();
        } else if (decision instanceof BuyDecision buy) {
            return "Buy: " + buy.cardType().description(); 
            
        } else if (decision instanceof PlayCardDecision playCard) {
            return "Play: " + playCard.card().description();
        } else {
            return decision.getDescription();
        }
    }
}
