package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;

import java.util.ArrayList;
import java.util.List;

public class RecordingGameObserver implements GameObserver {

    private final List<Event> observedEvents = new ArrayList<>();

    @Override
    public synchronized void notifyEvent(GameState state, Event event) {
        if (event != null) {
            observedEvents.add(event);
        }
    }

    public synchronized List<Event> getEventsSnapshot() {
        return new ArrayList<>(observedEvents);
    }

    public synchronized void clear() {
        observedEvents.clear();
    }
}
