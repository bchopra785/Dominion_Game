package edu.brandeis.cosi103a.groupb;

public class PlayerServer {
    
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
