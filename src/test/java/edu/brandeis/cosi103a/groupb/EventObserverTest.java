package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.GameEvent;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.engine.Engine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventObserverTest {

    private GameState makeState() {
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

    @Test
    public void playersExposeObserverAndReasonAwareDecisionDoesNotThrow() {
        BigMoneyPlayer player = new BigMoneyPlayer("P1");
        assertTrue(player.getObserver().isPresent());

        var chosen = player.makeDecision(
            makeState(),
            ImmutableList.of(new EndPhaseDecision(GameState.TurnPhase.BUY)),
            Optional.of(new GameEvent("test reason"))
        );

        assertNotNull(chosen);
    }

    @Test
    public void enginePublishesEventsToObservers() throws Exception {
        BigMoneyPlayer p1 = new BigMoneyPlayer("P1");
        BigMoneyPlayer p2 = new BigMoneyPlayer("P2");

        List<ParentPlayer> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        Engine engine = new Engine(players);
        engine.play();

        assertTrue(p1.getObservedEventCount() > 0);
        assertTrue(p2.getObservedEventCount() > 0);
    }

    @Test
    public void enginePublishesExpectedEventTypes() throws Exception {
        BigMoneyPlayer p1 = new BigMoneyPlayer("P1");
        BigMoneyPlayer p2 = new BigMoneyPlayer("P2");

        List<ParentPlayer> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        Engine engine = new Engine(players);
        engine.play();

        RecordingGameObserver observer = (RecordingGameObserver) p1.getObserver().orElseThrow();
        var events = observer.getEventsSnapshot();

        assertTrue(events.stream().anyMatch(e -> e instanceof PlayCardEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof GainCardEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof EndTurnEvent));
    }

    @Test
    public void consolePlayerReasonAwareDecisionOverloadWorks() {
        ConsolePlayer player = new ConsolePlayer(
            new ByteArrayInputStream("0\n".getBytes()),
            System.out
        );

        assertTrue(player.getObserver().isPresent());

        var chosen = player.makeDecision(
            makeState(),
            ImmutableList.of(new EndPhaseDecision(GameState.TurnPhase.BUY)),
            Optional.of(new GameEvent("console reason"))
        );

        assertNotNull(chosen);
    }

}
