package edu.brandeis.cosi103a.groupb;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.network.DecisionRequest;
import edu.brandeis.cosi103a.groupb.network.DecisionResponse;
import edu.brandeis.cosi103a.groupb.network.LogEventRequest;

@RestController
public class PlayerServer {

    private static ParentPlayer strategyPlayer;

    public static void main(String[] args) {
        // Determine which strategy to use from environment variable
        String strategy = System.getenv("PLAYER_STRATEGY");
        if (strategy == null || strategy.isEmpty()) {
            strategy = "V3"; // default to V3
        }

        System.out.println("Initializing PlayerServer with strategy: " + strategy);

        switch (strategy.toUpperCase()) {
            case "V2":
                strategyPlayer = new V2StrategyPlayer("V2StrategyPlayer");
                break;
            case "V3":
                strategyPlayer = new V3StrategyPlayer("V3StrategyPlayer");
                break;
            case "BIGMONEY":
                strategyPlayer = new BigMoneyPlayer("BigMoneyPlayer");
                break;
            default:
                System.out.println("Unknown strategy: " + strategy + ". Defaulting to V3.");
                strategyPlayer = new V3StrategyPlayer("V3StrategyPlayer");
        }

        SpringApplication.run(PlayerServer.class, args);
    }

@PostMapping(value = "/decide", consumes = "application/json", produces = "application/json") 
public ResponseEntity<DecisionResponse> decide(@RequestBody DecisionRequest request) {
    try {
        // Validate request fields
        if (request.getState() == null || request.getOptions() == null) {
            return ResponseEntity.badRequest().build(); // 400
        }

        GameState state = request.getState();
        List<Decision> options = request.getOptions();

        // Make decision using the selected strategy player and return ResponseEntity<DecisionResponse>
        ImmutableList<Decision> immutableOptions = ImmutableList.copyOf(options);
        Decision chosenDecision = strategyPlayer.makeDecision(state, immutableOptions);
        DecisionResponse responseBody = new DecisionResponse(chosenDecision, "Using " + strategyPlayer.getClass().getSimpleName() + " strategy");
        return ResponseEntity.ok(responseBody);

    } catch (IllegalArgumentException e) {
        // Bad data in the request, e.g. empty options list
        return ResponseEntity.badRequest().build(); // 400

    } catch (Exception e) {
        // Catch-all for unexpected errors
        return ResponseEntity.internalServerError().build(); // 500
    }
}

    @PostMapping(value = "/log-event", consumes = "application/json", produces = "application/json") 
    public ResponseEntity<Void> logEvent(@RequestBody(required = false) LogEventRequest request) {
        // Validate the request
        if (request == null) {
            return ResponseEntity.badRequest().build(); // 400
        }

        try {
            // Handle the event (log it, update state, etc.)
            System.out.println("Event received: " + request);

            // Return 200 with empty body
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Return 500 if something goes wrong
            return ResponseEntity.internalServerError().build();
        }
    }
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
