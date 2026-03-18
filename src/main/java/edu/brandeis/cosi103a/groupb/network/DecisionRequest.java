package edu.brandeis.cosi103a.groupb.network;

import java.util.List;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public class DecisionRequest {
    private GameState state;
    private List<Decision> options;
    private Event reason;
    private String player_uuid;
    
    // @JsonProperty("player_uuid") // Maps "player_uuid" in JSON to "playerUuid" in Java
    // @JsonAlias("PlayerUuid")
    

    public DecisionRequest() {}

    public DecisionRequest(GameState state, List<Decision> options, Event reason, String player_uuid) {
        this.state = state;
        this.options = options;
        this.reason = reason;
        this.player_uuid = player_uuid;
    }

    // Standard Getters and Setters
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public List<Decision> getOptions() { return options; }
    public void setOptions(List<Decision> options) { this.options = options; }

    public Event getReason() { return reason; }
    public void setReason(Event reason) { this.reason = reason; }

    public String getplayer_uuid() { return player_uuid; }
    public void setplayer_uuid(String player_uuid) { this.player_uuid = player_uuid; }
}
