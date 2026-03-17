package edu.brandeis.cosi103a.groupb.network;


import com.fasterxml.jackson.annotation.JsonProperty;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.GameState;

public class LogEventRequest {

    @JsonProperty("state")
    private GameState state;

    @JsonProperty("event")
    private Event event;

    @JsonProperty("player_uuid")
    private String playerUuid;

    // Getters and setters
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(String playerUuid) { this.playerUuid = playerUuid; }

    @Override
    public String toString() {
        return "LogEventRequest{" +
                "state=" + state +
                ", event=" + event +
                ", playerUuid='" + playerUuid + '\'' +
                '}';
    }
}