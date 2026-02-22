package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Console-based Player that reads choices from a shared Scanner and writes prompts to a print stream.
 *
 * Accepts a shared Scanner to allow multiple console players to read from the same input.
 * The caller (Engine/harness) is responsible for creating and managing the Scanner.
 */
public class ConsolePlayer implements Player {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final String name;
    private final Scanner scanner;
    private final PrintStream out;

    // Constructor accepting a shared Scanner
    public ConsolePlayer(Scanner scanner, PrintStream out) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("PrintStream cannot be null");
        }
        this.scanner = scanner;
        this.out = out;
        this.name = "ConsolePlayer-" + COUNTER.getAndIncrement();
    }

    // Package-private constructor for tests (inject streams, creates Scanner internally for testing)
    ConsolePlayer(InputStream in, PrintStream out) {
        this(new Scanner(in), out);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            out.println("No available options; returning null");
            return null;
        }

        out.println("Choose one of the following options:");
        for (int i = 0; i < options.size(); i++) {
            out.printf("[%d] %s%n", i, String.valueOf(options.get(i)));
        }
        out.print("Enter option index: ");

        while (true) {
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
                } else {
                    out.print("Invalid input. Enter a valid index: ");
                }
            } catch (NumberFormatException ignored) {
                out.print("Invalid input. Enter a valid index: ");
            }
        }
    }
}
