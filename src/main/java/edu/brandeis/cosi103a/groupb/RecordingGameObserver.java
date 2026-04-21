package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintStream;

public class RecordingGameObserver implements GameObserver {

    private final List<Event> observedEvents = new ArrayList<>();
    private final PrintStream out;
    private boolean verbose = true;  // Control event logging verbosity (default: false to reduce output)

    public RecordingGameObserver() {
        this(System.out, true);
    }

    public RecordingGameObserver(PrintStream out) {
        this(out, true);
    }

    public RecordingGameObserver(PrintStream out, boolean verbose) {
        this.out = out == null ? System.out : out;
        this.verbose = verbose;
    }

    @Override
    public synchronized void notifyEvent(GameState state, Event event) {
        if (event != null) {
            observedEvents.add(event);
            if (verbose) {
                String prefix = "[EVENT] ";
                if (state != null) {
                    prefix = "[" + state.currentPlayerName() + " | " + state.phase() + "] ";
                }
                out.println(prefix + event);
            }
        }
    }

    public synchronized List<Event> getEventsSnapshot() {
        return new ArrayList<>(observedEvents);
    }

    public synchronized void clear() {
        observedEvents.clear();
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
