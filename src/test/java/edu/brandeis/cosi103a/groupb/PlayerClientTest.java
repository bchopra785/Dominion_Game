package edu.brandeis.cosi103a.groupb;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.network.DecisionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class PlayerClientTest {

    private PlayerClient client;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        client = new PlayerClient("TestPlayer", "test-uuid-123", "http://localhost:8080");
        // Intercept the RestTemplate inside PlayerClient
        mockServer = MockRestServiceServer.createServer(client.getRestTemplate());
    }

    // --- makeDecision tests ---

    @Test
    public void makeDecision_validResponse_returnsDecision() throws Exception {
        Decision mockDecision = new GainCardDecision(Card.Type.BITCOIN); // create a mock/stub decision object
        DecisionResponse mockResponse = new DecisionResponse(mockDecision, "BigMoney strategy");

        mockServer.expect(requestTo("http://localhost:8080/decide"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(mockResponse),
                        MediaType.APPLICATION_JSON));

        Decision result = client.makeDecision(mockGameState(), ImmutableList.of(mockDecision));
        assertNotNull(result);
        assertEquals(mockDecision, result);

        mockServer.verify(); // confirms the request was actually made
    }

    @Test
    public void makeDecision_serverError_throwsException() {
        mockServer.expect(requestTo("http://localhost:8080/decide"))
                .andRespond(withServerError()); // simulates 500

        assertThrows(Exception.class, () ->
                client.makeDecision(mockGameState(), ImmutableList.of()));
    }

    // --- logEvent tests ---

    @Test
    public void logEvent_validRequest_sendsCorrectly() {
        mockServer.expect(requestTo("http://localhost:8080/log-event"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess()); // 200 empty body

        assertDoesNotThrow(() -> client.logEvent(mockGameState(), mockEvent()));

        mockServer.verify(); // confirms the request was actually sent
    }

    @Test
    public void logEvent_serverError_throwsException() {
        mockServer.expect(requestTo("http://localhost:8080/log-event"))
                .andRespond(withServerError());

        assertThrows(Exception.class, () ->
                client.logEvent(mockGameState(), mockEvent()));
    }

    // --- Constructor validation tests ---

    @Test
    public void constructor_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlayerClient(null, "uuid", "http://localhost:8080"));
    }

    @Test
    public void constructor_invalidUrl_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PlayerClient("name", "uuid", "not-a-valid-url"));
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

    private Event mockEvent() {
        return new PlayCardEvent(new Card(Card.Type.BITCOIN, 5), "P1");
    }
}