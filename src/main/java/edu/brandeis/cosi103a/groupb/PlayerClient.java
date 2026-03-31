package edu.brandeis.cosi103a.groupb;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GameObserver;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.network.DecisionRequest;
import edu.brandeis.cosi103a.groupb.network.DecisionResponse;
import edu.brandeis.cosi103a.groupb.network.LogEventRequest;

import java.net.URI;


public class PlayerClient extends ParentPlayer {
    
    private String serverUrl;
    private String Uuid;
    private final RestTemplate restTemplate;

    public PlayerClient(String name, String Uuid, String serverUrl) {
        super(name);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        if (Uuid == null || Uuid.isBlank()) {
            throw new IllegalArgumentException("UUID must not be null or empty");
        }
        if (serverUrl == null || serverUrl.isBlank()) {
            throw new IllegalArgumentException("Server URL must not be null or empty");
        }
        try {
            new URI(serverUrl).toURL().toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Server URL is not a valid URL: " + serverUrl, e);
        }
            this.serverUrl = serverUrl;
            this.Uuid = Uuid;
            this.restTemplate = new RestTemplate();
        }

    // This is method called by engine, and it gets decision by POST request to server using method below
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        
        return this.makeDecision(state, options, Optional.empty());
    }

    // This is method that sends post request to server and gets decision, and it is called by the above method
    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        
        // Set headers for POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create the object to send to the server
        DecisionRequest request = new DecisionRequest();
        request.setState(state);
        request.setOptions(options);
        request.setReason(reason.orElse(null));
        request.setplayer_uuid(Uuid);

        HttpEntity<DecisionRequest> entity = new HttpEntity<>(request, headers);

        // Send POST request to server and get response
        DecisionResponse responseBody = restTemplate.postForObject(
            serverUrl + "/decide", 
            entity, 
            DecisionResponse.class
        );

        return responseBody.getDecision();
    }

    // This method is called by engine to log events, and it sends POST request to server using method below
    public void logEvent(GameState state, Event event) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LogEventRequest request = new LogEventRequest();
        request.setState(state);
        request.setEvent(event);
        request.setPlayerUuid(Uuid);

        HttpEntity<LogEventRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.postForObject(serverUrl + "/log-event", entity, Void.class);
}

// For testing purposes, we can expose the RestTemplate to verify that the correct requests are being made
public RestTemplate getRestTemplate() {
    return restTemplate;
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
