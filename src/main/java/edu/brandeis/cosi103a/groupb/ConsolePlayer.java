package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
/**PULL BEFORE MERGIN */
/**
 * Console-based Player that reads choices from a shared Scanner and writes prompts to a print stream.
 *
 * Accepts a shared Scanner to allow multiple console players to read from the same input.
 * The caller (Engine/harness) is responsible for creating and managing the Scanner.
 */
public class ConsolePlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    private final Scanner scanner;
    private final PrintStream out;
    private final RecordingGameObserver observer;

    // Zero-arg constructor required by Engine
    public ConsolePlayer() {
        this(System.in, System.out);
    }

    // Package-private constructor for tests (inject streams)
    public ConsolePlayer(InputStream in, PrintStream out) {
        this(new Scanner(in), out);
    }

    // Shared Scanner constructor (allows multiple players to share an input Scanner)
    public ConsolePlayer(Scanner scanner, PrintStream out) {
        super("ConsolePlayer-" + COUNTER.getAndIncrement());
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("PrintStream cannot be null");
        }
        this.scanner = scanner;
        this.out = out;
        this.observer = new RecordingGameObserver();
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            out.println("No available options; returning null");
            return null;
        }

            out.println("\n\n------------------------------");
            out.print(describeGameState(state));
            out.println();
            out.println(getName() + ", it's your turn!");
            out.println("Choose one of the following options:");
            for (int i = 0; i < options.size(); i++) {
                out.println("[" + i + "] " + options.get(i).getDescription());
            }
            out.print("Enter option index: ");

        while (true) {
            if (!scanner.hasNextLine()) { //stop the program if there is no more input
                // out.println();
                // out.println("Input closed; selecting default option 0");
                // return options.get(0);
                throw new IllegalStateException("Input closed - no more decisions available");
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

    @Override
    public Optional<GameObserver> getObserver() {
        return Optional.of(observer);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }

    public int getObservedEventCount() {
        return observer.getEventsSnapshot().size();
    }
}
