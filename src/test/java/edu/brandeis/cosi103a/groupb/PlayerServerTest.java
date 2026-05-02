package edu.brandeis.cosi103a.groupb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.brandeis.cosi103a.groupb.network.LogEventRequest;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.network.DecisionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import edu.brandeis.cosi.atg.cards.Card;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PlayerServer.class)
public class PlayerServerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParentPlayer strategyPlayer;

    // --- /log-event tests ---

    @Test
    public void logEvent_validRequest_returns200() throws Exception {
        LogEventRequest request = new LogEventRequest();
        request.setPlayerUuid("test-uuid-123");
        // set state and event if you have mock objects for them

        mockMvc.perform(post("/log-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // empty body
    }

    @Test
    public void logEvent_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/log-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void logEvent_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/log-event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ this is not valid json }"))
                .andExpect(status().isBadRequest());
    }

    // --- /decide tests ---

    @Test
    public void decide_validRequest_returns200WithDecision() throws Exception {
        DecisionRequest request = new DecisionRequest();
        request.setplayer_uuid("test-uuid-123");
        // set state and options with mock objects
        request.setState(mockGameState());
        Decision refactor = new PlayCardDecision(new Card(Card.Type.REFACTOR, 1));
        ImmutableList<Decision> options = ImmutableList.of(refactor);
        request.setOptions(options);

        // Mock the strategy player to return a decision
        when(strategyPlayer.makeDecision(any(GameState.class), any(ImmutableList.class)))
                .thenReturn(refactor);

        mockMvc.perform(post("/decide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").exists()); // checks decision field is present
    }

    @Test
    public void decide_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/decide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

        // --- Helpers ---
    private GameState mockGameState() {
        return new GameState(
            "P1",
            new Hand(ImmutableList.of(), ImmutableList.of()),
            GameState.TurnPhase.BUY,
            1,
            0,
            1,
            new CardStacks(ImmutableMap.of())
        );
    }

}