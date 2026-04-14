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

    public RecordingGameObserver() {
        this(System.out);
    }

    public RecordingGameObserver(PrintStream out) {
        this.out = out == null ? System.out : out;
    }

    @Override
    public synchronized void notifyEvent(GameState state, Event event) {
        if (event != null) {
            observedEvents.add(event);
            String prefix = "[EVENT] ";
            if (state != null) {
                prefix = "[" + state.currentPlayerName() + " | " + state.phase() + "] ";
            }
            out.println(prefix + event);
        }
    }

    public synchronized List<Event> getEventsSnapshot() {
        return new ArrayList<>(observedEvents);
    }

    public synchronized void clear() {
        observedEvents.clear();
    }
}
