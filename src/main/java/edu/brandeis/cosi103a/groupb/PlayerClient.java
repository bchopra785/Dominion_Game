package edu.brandeis.cosi103a.groupb;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.network.DecisionRequest;

public class PlayerClient implements Player {

    public PlayerClient(String name) {
        super(name);
        //TODO Auto-generated constructor stub
    }

    // @Override
    // public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'makeDecision'");
    // }

    public Decision decide(DecisionRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decide'");
    }

    @Override
    public void logEvent(LogEventRequest request) {
        // Similar logic: Send a POST to /log-event, but don't worry about the return value
    }

    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }


    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'makeDecision'");
    }

    @Override
    public Optional<GameObserver> getObserver() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObserver'");
    }

    
}

// The "player client" will implement the Player interface from the ATG API, and be used directly
// by the Engine. It will not contain any decision-making logic, and instead will request decisions
// from a remote HTTP server - the "player server". The player server will contain all the necessary
// decision-making logic, and respond with the chosen decision. The player client itself should not
// contain any player logic, and should be able to work with any player server which follows the
// server API specifications (found below).


// The Player Client (The Messenger)
// Role: This lives inside your existing Java game environment. It must implement the Player interface from the ATG API.

// Logic: None. It should be "dumb." When the engine asks it for a decision via the choose() method, the client should:

// Wrap the GameState, Decisions, and Event into a JSON object.

// Send a POST request to your server's /decide endpoint.

// Wait for the JSON response, extract the chosen decision, and return it to the engine
