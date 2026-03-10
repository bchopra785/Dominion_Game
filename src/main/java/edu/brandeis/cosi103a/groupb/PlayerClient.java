package edu.brandeis.cosi103a.groupb;

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

public class PlayerClient extends ParentPlayer {
    
    private String serverUrl;
    private String Uuid;
    private final RestTemplate restTemplate;

    public PlayerClient(String name, String Uuid, String serverUrl) {
        super(name);
        this.serverUrl = serverUrl;
        this.Uuid = Uuid;
        this.restTemplate = new RestTemplate();
    }

    //TODO:defensive programming: 
    //          check that serverUrl is valid URL, and that name and Uuid are not null/empty. 
    //          Throw IllegalArgumentException if any checks fail.

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        
        //set headers for POST request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        //create the object to send to the server
        DecisionRequest request = new DecisionRequest();
        request.setState(state);
        request.setOptions(options);
        request.setReason(reason.orElse(null));
        request.setplayer_uuid(Uuid);

        HttpEntity<DecisionRequest> entity = new HttpEntity<>(request, headers);

        //send POST request to server and get response
        DecisionResponse responseBody = restTemplate.postForObject(
            serverUrl + "/decide", 
            entity, 
            DecisionResponse.class
        );

        return responseBody.getDecision();
    }

    @Override
    public Optional<GameObserver> getObserver() {
        // Remote players don't provide observers
        return Optional.empty();
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        //this feels like a terrible idea but I don't know a way around at present
        return this.makeDecision(state, options, Optional.empty());
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
