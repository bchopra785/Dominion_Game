package edu.brandeis.cosi103a.groupb.network;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public class DecisionRequest {
    private GameState state;
    private List<Decision> options;
    private Event reason;
    
    @JsonProperty("player_uuid") // Maps "player_uuid" in JSON to "playerUuid" in Java
    private String playerUuid;

    // Standard Getters and Setters
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public List<Decision> getOptions() { return options; }
    public void setOptions(List<Decision> options) { this.options = options; }

    public Event getReason() { return reason; }
    public void setReason(Event reason) { this.reason = reason; }

    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }
}
