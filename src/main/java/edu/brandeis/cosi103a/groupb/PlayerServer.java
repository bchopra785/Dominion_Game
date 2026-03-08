package edu.brandeis.cosi103a.groupb;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.network.DecisionRequest;
import edu.brandeis.cosi103a.groupb.network.DecisionResponse;

@RestController
public class PlayerServer {
    //returns RespondEntity<DecisionResponse>
    //DecisionResponse is the Custom Java class that contains chosen decision
    //decide is the name of the function
    //it takes in @RequestBody DecisionRequest request as input, where DecisionRequest is the Custom Java class that contains the game state and possible decisions
    //@RequestBody converts the JSON body of the HTTP reqyest into Java object "request" of type DecisionRequest
    @PostMapping(value = "/decide", consumes = "application/json", produces = "application/json") 
    public ResponseEntity<DecisionResponse> decide(@RequestBody DecisionRequest request) {
        GameState state = request.getState();
        List<Decision> options = request.getOptions();
        Event reason = request.getReason();
        String player_uuid = request.getplayer_uuid();

        //make decision and return ResponseEntity<DecisionResponse>
        //TODO: change to actual logic
        Decision chosenDecision = options.get(0);
        DecisionResponse responseBody = new DecisionResponse(chosenDecision, "Chose the first option as a placeholder");
        return ResponseEntity.ok(responseBody);
        
    }

    //TODO: implement logEvent endpoint
    // @PostMapping(value = "/log-event", consumes = "application/json")
    // public ResponseEntity<Void> logEvent(@RequestBody LogEventRequest request) {
      
    // }
}

// The "player server" will be an HTTP server. It will use an "RPC" (remote procedure call) API
// style. It will accept POST requests with JSON bodies that represent the game state and
// possible decisions, and respond with a chosen decision.

// HTTP Server
// The player server should expose two HTTP endpoints:
// ● /decide (POST)
// ○ Expects a JSON body formatted according to the "DecisionRequest" schema
// below
// ○ Successful (200) responses should include a JSON body formatted according to
// the "DecisionResponse" schema below.
// ○ Unsuccessful responses should use appropriate 4xx or 5xx HTTP response
// codes.
// ● /log-event (POST)
// ○ Expects a JSON body formatted according to the "LogEventRequest" schema
// below
// ○ Successful (200) responses should have empty bodies.
// ○ Unsuccessful responses should use appropriate 4xx or 5xx HTTP response
// codes


// The Player Server (The Brain)
// Role: A standalone web server (likely using Spring) that waits for instructions.

// Logic: This is where your actual strategy (AI or heuristic) lives.

// Endpoints:

// POST /decide: Receives the current situation and picks the best move.

// POST /log-event: Receives updates about what happened in the game (e.g., "Player 2 drew a card") so the "brain" can stay updated.
