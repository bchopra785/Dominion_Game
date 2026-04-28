package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.ChooseEffectDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.GameEvent;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.engine.Engine;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void enginePublishesUnitTestPlayCardEventOnce() throws Exception {
        ScriptedPlayer p1 = new ScriptedPlayer("P1");
        BigMoneyPlayer p2 = new BigMoneyPlayer("P2");

        List<ParentPlayer> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);

        Engine engine = new Engine(players);
        PlayerCards p1Cards = getPlayerCards(engine, p1);
        Card unitTestCard = new Card(Card.Type.UNIT_TEST, 0);
        setHandToUnitTestOnly(p1Cards, unitTestCard);

        p1.queueDecision(new PlayCardDecision(unitTestCard));
        p1.queueDecision(new ChooseEffectDecision(ChooseEffectDecision.Effect.UNIT_TEST_PLUS_TWO_ACTIONS));
        p1.queueDecision(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        p1.queueDecision(new EndPhaseDecision(GameState.TurnPhase.MONEY));
        p1.queueDecision(new EndPhaseDecision(GameState.TurnPhase.BUY));

        assertThrows(IllegalStateException.class, engine::play);

        RecordingGameObserver observer = (RecordingGameObserver) p1.getObserver().orElseThrow();
        long playCardEvents = observer.getEventsSnapshot().stream()
            .filter(event -> event instanceof PlayCardEvent)
            .map(event -> (PlayCardEvent) event)
            .filter(event -> event.playerName().equals("P1"))
            .count();

        assertEquals(1L, playCardEvents);
    }

    private static PlayerCards getPlayerCards(Engine engine, ParentPlayer player) throws Exception {
        Field field = Engine.class.getDeclaredField("playerCardsMap");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<ParentPlayer, PlayerCards> playerCardsMap = (java.util.Map<ParentPlayer, PlayerCards>) field.get(engine);
        return playerCardsMap.get(player);
    }

    private static void setHandToUnitTestOnly(PlayerCards playerCards, Card unitTestCard) throws Exception {
        Field unplayedField = PlayerCards.class.getDeclaredField("unplayedCards");
        unplayedField.setAccessible(true);
        List<edu.brandeis.cosi.atg.cards.Card> unplayedCards = new ArrayList<>();
        unplayedCards.add(unitTestCard);
        unplayedField.set(playerCards, unplayedCards);

        Field playedField = PlayerCards.class.getDeclaredField("playedCards");
        playedField.setAccessible(true);
        playedField.set(playerCards, new ArrayList<>());

        Field deckField = PlayerCards.class.getDeclaredField("deck");
        deckField.setAccessible(true);
        List<edu.brandeis.cosi.atg.cards.Card> deck = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            deck.add(new edu.brandeis.cosi.atg.cards.Card(edu.brandeis.cosi.atg.cards.Card.Type.METHOD, i));
        }
        deckField.set(playerCards, deck);

        Field discardField = PlayerCards.class.getDeclaredField("discard");
        discardField.setAccessible(true);
        discardField.set(playerCards, new ArrayList<>());
    }

    private static class ScriptedPlayer extends BigMoneyPlayer {
        private final Deque<edu.brandeis.cosi.atg.decisions.Decision> decisions = new ArrayDeque<>();

        ScriptedPlayer(String name) {
            super(name);
        }

        void queueDecision(edu.brandeis.cosi.atg.decisions.Decision decision) {
            decisions.addLast(decision);
        }

        @Override
        public edu.brandeis.cosi.atg.decisions.Decision makeDecision(GameState state, ImmutableList<edu.brandeis.cosi.atg.decisions.Decision> options) {
            if (!decisions.isEmpty()) {
                return decisions.removeFirst();
            }
            throw new IllegalStateException("No scripted decision available");
        }
    }

}
