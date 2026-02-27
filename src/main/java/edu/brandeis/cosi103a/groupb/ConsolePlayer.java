package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Console-based Player that reads choices from an input stream and writes prompts to a print stream.
 *
 * Zero-arg constructor required by the Engine delegates to System.in/System.out.
 */
public class ConsolePlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final Scanner scanner;
    private final PrintStream out;

    // Zero-arg constructor required by Engine
    public ConsolePlayer() {
        this(System.in, System.out);
    }

    // Package-private constructor for tests (inject streams)
    public ConsolePlayer(InputStream in, PrintStream out) {
        super("ConsolePlayer-" + COUNTER.getAndIncrement());
        this.scanner = new Scanner(in);
        this.out = out;
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        try {
            if (options == null || options.isEmpty()) {
                out.println("No available options; returning null");
                return null;
            }

            out.println("------------------------------");
            out.print(describeGameState(state));
            out.println();
            out.println("Player " + getName() + ", it's your turn!");
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
                    }
                } catch (NumberFormatException ignored) {
                }
                out.print("Invalid input. Enter a valid index: ");
            }
        } catch (Exception e) {
            out.println("Error reading input; selecting default option 0");
            return (options == null || options.isEmpty()) ? null : options.get(0);
        }
    }
}
