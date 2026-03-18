package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Backlog;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.DailyScrum;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.EvergreenTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Hack;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Ipo;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.MergeConflict;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Monitoring;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Ransomware;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Refactor;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.SprintPlanning;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.TechDebt;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.UnitTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.DeploymentPipeline;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class CardFunctionTests {

    private BoardCards board;

    static class StubPlayer extends ConsolePlayer {
        private final Deque<Decision> decisions = new ArrayDeque<>();

        StubPlayer(String name) {
            super(new java.util.Scanner(""), new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
            // Override name by reflection since ConsolePlayer sets a generated name
            try {
                java.lang.reflect.Field nameField = ParentPlayer.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(this, name);
            } catch (Exception ignored) {
            }
        }

        void queueDecision(Decision d) {
            decisions.addLast(d);
        }

        @Override
        public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
            assertFalse(decisions.isEmpty(), "No decision queued for player " + getName());
            return decisions.pollFirst();
        }

        @Override
        public java.util.Optional<edu.brandeis.cosi.atg.event.GameObserver> getObserver() {
            return java.util.Optional.empty();
        }
    }

    static class TestPlayerCards extends PlayerCards {
        TestPlayerCards(BoardCards board) {
            super(board);
        }

        void setUnplayedCards(List<Card> cards) throws Exception {
            Field field = PlayerCards.class.getDeclaredField("unplayedCards");
            field.setAccessible(true);
            field.set(this, new ArrayList<>(cards));
        }

        int getDiscardSize() throws Exception {
            Field field = PlayerCards.class.getDeclaredField("discard");
            field.setAccessible(true);
            return ((List<Card>) field.get(this)).size();
        }

        List<Card> getDiscardList() throws Exception {
            Field field = PlayerCards.class.getDeclaredField("discard");
            field.setAccessible(true);
            return new ArrayList<>((List<Card>) field.get(this));
        }
    }

    @BeforeEach
    public void setup() {
        board = new BoardCards();
    }

    private GameState makeState(String playerName, PlayerCards playerCards) {
        Hand hand = playerCards.getHand();
        int actions = 1;
        int money = playerCards.getCostInHand();
        int buys = 1;
        CardStacks buyable = board.getPlayableCards(money);
        return new GameState(playerName, hand, GameState.TurnPhase.ACTION, actions, money, buys, buyable);
    }

    @Test
    public void testBacklog_discardThenDrawsSameNumber() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        // two cards in hand
        pc.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0), new Card(Card.Type.ETHEREUM, 1)));

        Backlog backlog = new Backlog();

        // discard first card, then end
        player.queueDecision(new DiscardCardDecision(new Card(Card.Type.BITCOIN, 0)));
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));

        GameState newState = backlog.play(makeState(player.getName(), pc), player, pc, board);
        assertEquals(2, newState.currentPlayerHand().unplayedCards().size());
        assertEquals(1, pc.getDiscardSize());
    }
/** 
    @Test
    public void testCodeReview_drawsAndAddsActions() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        pc.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        GameState before = makeState(player.getName(), pc);

        CodeReview cr = new CodeReview();
        GameState after = cr.play(before, player, pc, board);

        assertEquals(before.availableActions() + 2, after.availableActions());
        assertEquals(before.currentPlayerHand().unplayedCards().size() + 1, after.currentPlayerHand().unplayedCards().size());
    }
*/
    @Test
    public void testDailyScrum_givesOtherPlayersACard() throws Exception {
        StubPlayer p1 = new StubPlayer("P1");
        StubPlayer p2 = new StubPlayer("P2");
        StubPlayer p3 = new StubPlayer("P3");

        TestPlayerCards pc1 = new TestPlayerCards(board);
        TestPlayerCards pc2 = new TestPlayerCards(board);
        TestPlayerCards pc3 = new TestPlayerCards(board);

        p1.setPlayerCards(pc1);
        p2.setPlayerCards(pc2);
        p3.setPlayerCards(pc3);

        // All start with empty hand, draw from deck via drawToHand
        pc1.drawToHand();
        pc2.drawToHand();
        pc3.drawToHand();

        List<ParentPlayer> players = List.of(p1, p2, p3);
        Map<ParentPlayer, PlayerCards> map = new HashMap<>();
        map.put(p1, pc1);
        map.put(p2, pc2);
        map.put(p3, pc3);

        GameState before = makeState(p1.getName(), pc1);
        DailyScrum ds = new DailyScrum();
        GameState after = ds.play(before, p1, players, map, board);

        assertEquals(2, after.availableBuys());
        assertEquals(2, pc2.getHand().unplayedCards().size());
        assertEquals(2, pc3.getHand().unplayedCards().size());
    }

    @Test
    public void testEvergreenTest_givesBugsUnlessMonitored() throws Exception {
        StubPlayer p1 = new StubPlayer("P1");
        StubPlayer p2 = new StubPlayer("P2");

        TestPlayerCards pc1 = new TestPlayerCards(board);
        TestPlayerCards pc2 = new TestPlayerCards(board);

        p1.setPlayerCards(pc1);
        p2.setPlayerCards(pc2);

        // give p2 a monitoring card so they avoid the bug
        pc2.setUnplayedCards(List.of(new Card(Card.Type.MONITORING, 0)));

        List<ParentPlayer> players = List.of(p1, p2);
        Map<ParentPlayer, PlayerCards> map = new HashMap<>();
        map.put(p1, pc1);
        map.put(p2, pc2);

        GameState before = makeState(p1.getName(), pc1);
        EvergreenTest et = new EvergreenTest();
        et.play(before, p1, players, map, board);

        assertEquals(0, pc2.getDiscardSize(), "Player with Monitoring should not receive bug");

        // Now without monitoring, they should get a bug
        pc2.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        et.play(before, p1, players, map, board);
        assertEquals(1, pc2.getDiscardSize(), "Player without Monitoring should gain a bug");
    }

    @Test
    public void testHack_discardDownToThree() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");

        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);

        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);

        // Set defender hand to 5 cards
        defenderCards.setUnplayedCards(List.of(
            new Card(Card.Type.BITCOIN, 0),
            new Card(Card.Type.ETHEREUM, 1),
            new Card(Card.Type.DOGECOIN, 2),
            new Card(Card.Type.METHOD, 2),
            new Card(Card.Type.MODULE, 3)
        ));

        // Defender will discard two cards; queue those decisions
        defender.queueDecision(new DiscardCardDecision(new Card(Card.Type.BITCOIN, 0)));
        defender.queueDecision(new DiscardCardDecision(new Card(Card.Type.ETHEREUM, 1)));

        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = new HashMap<>();
        map.put(attacker, attackerCards);
        map.put(defender, defenderCards);

        GameState before = makeState(attacker.getName(), attackerCards);
        Hack hack = new Hack();
        hack.play(before, attacker, players, map, board);

        assertEquals(3, defenderCards.getHand().unplayedCards().size(), "Defender should be down to 3 cards");
    }

    @Test
    public void testRansomware_discardTwoOrGainBug() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");

        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);

        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);

        // Set defender hand to 5 cards
        defenderCards.setUnplayedCards(List.of(
            new Card(Card.Type.BITCOIN, 0),
            new Card(Card.Type.ETHEREUM, 1),
            new Card(Card.Type.DOGECOIN, 2),
            new Card(Card.Type.METHOD, 2),
            new Card(Card.Type.MODULE, 3)
        ));

        // Defender chooses to discard 2 cards
        defender.queueDecision(new DiscardCardDecision(new Card(Card.Type.BITCOIN, 0)));
        defender.queueDecision(new DiscardCardDecision(new Card(Card.Type.ETHEREUM, 1)));

        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = new HashMap<>();
        map.put(attacker, attackerCards);
        map.put(defender, defenderCards);

        GameState before = makeState(attacker.getName(), attackerCards);
        Ransomware ransomware = new Ransomware();
        ransomware.play(before, attacker, players, map, board);

        assertEquals(3, defenderCards.getHand().unplayedCards().size(), "Defender should have discarded 2 cards");
    }

    @Test
    public void testIpo_grantsActionsMoneyAndCards() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        int beforeActions = 1;
        GameState before = makeState(player.getName(), pc);

        Ipo ipo = new Ipo();
        GameState after = ipo.play(before, player, pc, board);

        assertEquals(beforeActions + 1, after.availableActions());
        assertEquals(before.spendableMoney() + 2, after.spendableMoney());
        assertEquals(before.currentPlayerHand().unplayedCards().size() + 2, after.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testMergeConflict_trashedCardDrawsCost() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        pc.setUnplayedCards(List.of(new Card(Card.Type.METHOD, 2)));
        player.queueDecision(new TrashCardDecision(new Card(Card.Type.METHOD, 2)));

        GameState before = makeState(player.getName(), pc);
        MergeConflict mc = new MergeConflict();
        GameState after = mc.play(before, player, pc, board);

        assertEquals(2, after.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testMonitoring_drawsTwoCards() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        int before = pc.getHand().unplayedCards().size();
        Monitoring mon = new Monitoring();
        GameState after = mon.play(makeState(player.getName(), pc), player, pc, board);
        assertEquals(before + 2, after.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testRefactor_handlesEmptyBuyOptions() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        // Empty the board so no cards can be gained
        Field cardMapField = BoardCards.class.getDeclaredField("cardMap");
        cardMapField.setAccessible(true);
        ((Map<?, ?>) cardMapField.get(board)).clear();

        pc.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        player.queueDecision(new TrashCardDecision(new Card(Card.Type.BITCOIN, 0)));

        Refactor refactor = new Refactor();
        assertDoesNotThrow(() -> refactor.play(makeState(player.getName(), pc), player, pc, board));
    }

    @Test
    public void testSprintPlanning_grantsActionBuyAndCard() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        SprintPlanning sp = new SprintPlanning();
        GameState after = sp.play(before, player, pc, board);

        assertEquals(before.availableActions() + 1, after.availableActions());
        assertEquals(before.availableBuys() + 1, after.availableBuys());
        assertEquals(before.currentPlayerHand().unplayedCards().size() + 1, after.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testTechDebt_discardsPerEmptyPile() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        // Empty one pile to force a discard
        while (board.drawDeckCard(Card.Type.METHOD) != null) {}
        
        // Set a hand with at least one card
        pc.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0), new Card(Card.Type.ETHEREUM, 1)));
        
        // Queue discard decision
        player.queueDecision(new DiscardCardDecision(new Card(Card.Type.BITCOIN, 0)));
        
        int beforeDiscard = pc.getDiscardSize();
        GameState before = makeState(player.getName(), pc);
        TechDebt td = new TechDebt();
        GameState after = td.play(before, player, pc, board);

        assertEquals(beforeDiscard + 1, pc.getDiscardSize(), "Should have discarded one card");
    }

    @Test
    public void testUnitTest_choiceBranchesWork() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        UnitTest ut = new UnitTest();

        // Option 0: +2 Actions
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));
        GameState after0 = ut.play(before, player, pc, board);
        assertEquals(before.availableActions() + 2, after0.availableActions());

        // Option 1: +$2
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.BUY));
        GameState after1 = ut.play(before, player, pc, board);
        assertEquals(before.spendableMoney() + 2, after1.spendableMoney());

        // Option 2: +2 Cards
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.CLEANUP));
        GameState after2 = ut.play(before, player, pc, board);
        assertEquals(before.currentPlayerHand().unplayedCards().size() + 2, after2.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testDeploymentPipeline_activatesCostReduction() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        
        DeploymentPipeline dp = new DeploymentPipeline();
        GameState after = dp.play(before, player, pc, board);

        // Should add +$1 and +1 Buy, cost reduction is handled by Engine
        assertEquals(before.spendableMoney() + 1, after.spendableMoney());
        assertEquals(before.availableBuys() + 1, after.availableBuys());
    }
}
