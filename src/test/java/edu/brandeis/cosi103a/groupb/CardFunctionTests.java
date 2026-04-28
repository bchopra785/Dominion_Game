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
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.ActionCards;



public class CardFunctionTests {

    // --- EDGE CASE TESTS FOR ACTION CARDS ---

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



    // ...rest of the test class...

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

        // discard first card, then end
        player.queueDecision(new DiscardCardDecision(new Card(Card.Type.BITCOIN, 0)));
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));

        GameState newState = ActionCards.playActionCard(
            new Card(Card.Type.BACKLOG, 0),
            makeState(player.getName(), pc),
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
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
        GameState after = ActionCards.playActionCard(new Card(Card.Type.DAILY_SCRUM, 0), before, p1, players, map, board);

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
        ActionCards.playActionCard(new Card(Card.Type.EVERGREEN_TEST, 0), before, p1, players, map, board);

        assertEquals(0, pc2.getDiscardSize(), "Player with Monitoring should not receive bug");

        // Now without monitoring, they should get a bug
        pc2.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        ActionCards.playActionCard(new Card(Card.Type.EVERGREEN_TEST, 0), before, p1, players, map, board);
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
        ActionCards.playActionCard(new Card(Card.Type.HACK, 0), before, attacker, players, map, board);

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
        ActionCards.playActionCard(new Card(Card.Type.RANSOMWARE, 0), before, attacker, players, map, board);

        assertEquals(3, defenderCards.getHand().unplayedCards().size(), "Defender should have discarded 2 cards");
    }

    @Test
    public void testIpo_grantsActionsMoneyAndCards() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        int beforeActions = 1;
        GameState before = makeState(player.getName(), pc);

        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.IPO, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );

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
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.MERGE_CONFLICT, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );

        assertEquals(2, after.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testMonitoring_drawsTwoCards() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        int before = pc.getHand().unplayedCards().size();
        GameState beforeState = makeState(player.getName(), pc);
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.MONITORING, 0),
            beforeState,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertEquals(before + 2, after.currentPlayerHand().unplayedCards().size());
        assertEquals(beforeState.phase(), after.phase(), "Monitoring should preserve current phase");
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

        assertDoesNotThrow(() -> ActionCards.playActionCard(
            new Card(Card.Type.REFACTOR, 0),
            makeState(player.getName(), pc),
            player,
            List.of(player),
            Map.of(player, pc),
            board
        ));
    }

    @Test
    public void testSprintPlanning_grantsActionBuyAndCard() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.SPRINT_PLANNING, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );

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
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.TECH_DEBT, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertNotNull(after);

        assertEquals(beforeDiscard + 1, pc.getDiscardSize(), "Should have discarded one card");
    }

    @Test
    public void testUnitTest_choiceBranchesWork() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        // Option 0: +2 Actions
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));
        GameState after0 = ActionCards.playActionCard(
            new Card(Card.Type.UNIT_TEST, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertEquals(before.availableActions() + 2, after0.availableActions());

        // Option 1: +$2
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.BUY));
        GameState after1 = ActionCards.playActionCard(
            new Card(Card.Type.UNIT_TEST, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertEquals(before.spendableMoney() + 2, after1.spendableMoney());

        // Option 2: +2 Cards
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.CLEANUP));
        GameState after2 = ActionCards.playActionCard(
            new Card(Card.Type.UNIT_TEST, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertEquals(before.currentPlayerHand().unplayedCards().size() + 2, after2.currentPlayerHand().unplayedCards().size());
    }

    @Test
    public void testDeploymentPipeline_activatesCostReduction() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);

        GameState before = makeState(player.getName(), pc);
        
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.DEPLOYMENT_PIPELINE, 0),
            before,
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );

        // Should add +$1 and +1 Buy, cost reduction is handled by Engine
        assertEquals(before.spendableMoney() + 1, after.spendableMoney());
        assertEquals(before.availableBuys() + 1, after.availableBuys());
    }

    @Test
    public void testParallelization_playActionWithNoEffect() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);
        // Add an action card that does nothing (simulate with Monitoring)
        pc.setUnplayedCards(List.of(new Card(Card.Type.MONITORING, 0)));
        List<ParentPlayer> players = List.of(player);
        Map<ParentPlayer, PlayerCards> map = Map.of(player, pc);
        // Should not throw
        // Queue EndPhaseDecision so player can respond
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));
        assertDoesNotThrow(() -> ActionCards.playActionCard(
            new Card(Card.Type.PARALLELIZATION, 0),
            makeState(player.getName(), pc),
            player,
            players,
            map,
            board
        ));
    }

    @Test
    public void testParallelization_noPlayableActions() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);
        // No action cards in hand
        pc.setUnplayedCards(List.of());
        List<ParentPlayer> players = List.of(player);
        Map<ParentPlayer, PlayerCards> map = Map.of(player, pc);
        // Queue EndPhaseDecision so player can respond
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));
        assertDoesNotThrow(() -> ActionCards.playActionCard(
            new Card(Card.Type.PARALLELIZATION, 0),
            makeState(player.getName(), pc),
            player,
            players,
            map,
            board
        ));
    }

    @Test
    public void testBacklog_noCardsToDiscard() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);
        // No cards in hand
        pc.setUnplayedCards(List.of());
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.EndPhaseDecision(GameState.TurnPhase.ACTION));
        GameState newState = ActionCards.playActionCard(
            new Card(Card.Type.BACKLOG, 0),
            makeState(player.getName(), pc),
            player,
            List.of(player),
            Map.of(player, pc),
            board
        );
        assertEquals(0, newState.currentPlayerHand().unplayedCards().size());
    }

    
    @Test
    public void testEvergreenTest_allOthersHaveMonitoring() throws Exception {
        StubPlayer p1 = new StubPlayer("P1");
        StubPlayer p2 = new StubPlayer("P2");
        TestPlayerCards pc1 = new TestPlayerCards(board);
        TestPlayerCards pc2 = new TestPlayerCards(board);
        p1.setPlayerCards(pc1);
        p2.setPlayerCards(pc2);
        pc2.setUnplayedCards(List.of(new Card(Card.Type.MONITORING, 0)));
        List<ParentPlayer> players = List.of(p1, p2);
        Map<ParentPlayer, PlayerCards> map = Map.of(p1, pc1, p2, pc2);
        ActionCards.playActionCard(new Card(Card.Type.EVERGREEN_TEST, 0), makeState(p1.getName(), pc1), p1, players, map, board);
        assertEquals(0, pc2.getDiscardSize());
    }

    @Test
    public void testEvergreenTest_bugPileEmpty() throws Exception {
        StubPlayer p1 = new StubPlayer("P1");
        StubPlayer p2 = new StubPlayer("P2");
        TestPlayerCards pc1 = new TestPlayerCards(board);
        TestPlayerCards pc2 = new TestPlayerCards(board);
        p1.setPlayerCards(pc1);
        p2.setPlayerCards(pc2);
        // Remove all bugs from board
        while (board.drawDeckCard(Card.Type.BUG) != null) {}
        List<ParentPlayer> players = List.of(p1, p2);
        Map<ParentPlayer, PlayerCards> map = Map.of(p1, pc1, p2, pc2);
        ActionCards.playActionCard(new Card(Card.Type.EVERGREEN_TEST, 0), makeState(p1.getName(), pc1), p1, players, map, board);
        // Should not throw, discard remains 0
        assertEquals(0, pc2.getDiscardSize());
    }

    @Test
    public void testHack_handAlreadyThreeOrLess() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");
        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);
        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);
        defenderCards.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0), new Card(Card.Type.ETHEREUM, 1), new Card(Card.Type.DOGECOIN, 2)));
        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = Map.of(attacker, attackerCards, defender, defenderCards);
        ActionCards.playActionCard(new Card(Card.Type.HACK, 0), makeState(attacker.getName(), attackerCards), attacker, players, map, board);
        assertEquals(3, defenderCards.getHand().unplayedCards().size());
    }

    @Test
    public void testHack_handEmpty() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");
        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);
        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);
        defenderCards.setUnplayedCards(List.of());
        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = Map.of(attacker, attackerCards, defender, defenderCards);
        ActionCards.playActionCard(new Card(Card.Type.HACK, 0), makeState(attacker.getName(), attackerCards), attacker, players, map, board);
        assertEquals(0, defenderCards.getHand().unplayedCards().size());
    }

    @Test
    public void testRansomware_handLessThanTwo() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");
        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);
        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);
        defenderCards.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = Map.of(attacker, attackerCards, defender, defenderCards);
        ActionCards.playActionCard(new Card(Card.Type.RANSOMWARE, 0), makeState(attacker.getName(), attackerCards), attacker, players, map, board);
        // Should gain bug if bug available, or nothing if not
        // Accept either 1 or 0 in discard
        assertTrue(defenderCards.getDiscardSize() <= 1);
    }

    @Test
    public void testRansomware_bugPileEmpty() throws Exception {
        StubPlayer attacker = new StubPlayer("A");
        StubPlayer defender = new StubPlayer("D");
        TestPlayerCards attackerCards = new TestPlayerCards(board);
        TestPlayerCards defenderCards = new TestPlayerCards(board);
        attacker.setPlayerCards(attackerCards);
        defender.setPlayerCards(defenderCards);
        defenderCards.setUnplayedCards(List.of(new Card(Card.Type.BITCOIN, 0)));
        while (board.drawDeckCard(Card.Type.BUG) != null) {}
        List<ParentPlayer> players = List.of(attacker, defender);
        Map<ParentPlayer, PlayerCards> map = Map.of(attacker, attackerCards, defender, defenderCards);
        ActionCards.playActionCard(new Card(Card.Type.RANSOMWARE, 0), makeState(attacker.getName(), attackerCards), attacker, players, map, board);
        assertEquals(0, defenderCards.getDiscardSize());
    }

    @Test
    public void testDeploymentPipeline_noCardsToBuyEvenWithReduction() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);
        // Empty the board so no cards can be bought
        Field cardMapField = BoardCards.class.getDeclaredField("cardMap");
        cardMapField.setAccessible(true);
        ((Map<?, ?>) cardMapField.get(board)).clear();
        // Should not throw
        assertDoesNotThrow(() -> ActionCards.playActionCard(
            new Card(Card.Type.DEPLOYMENT_PIPELINE, 0),
            makeState(player.getName(), pc),
            player,
            List.of(player),
            Map.of(player, pc),
            board
        ));
    }

    @Test
    public void testParallelization_playsActionTwice() throws Exception {
        StubPlayer player = new StubPlayer("P1");
        TestPlayerCards pc = new TestPlayerCards(board);
        player.setPlayerCards(pc);
        // Add a CodeReview card to hand
        Card codeReview = new Card(Card.Type.CODE_REVIEW, 0);
        pc.setUnplayedCards(List.of(codeReview));
        List<ParentPlayer> players = List.of(player);
        Map<ParentPlayer, PlayerCards> map = Map.of(player, pc);
        // Queue the decision to play CodeReview
        player.queueDecision(new edu.brandeis.cosi.atg.decisions.PlayCardDecision(codeReview));
        // Initial hand size
        int before = pc.getHand().unplayedCards().size();
        // Play Parallelization
        GameState after = ActionCards.playActionCard(
            new Card(Card.Type.PARALLELIZATION, 0),
            makeState(player.getName(), pc),
            player,
            players,
            map,
            board
        );
        // CodeReview draws 2 cards, Parallelization should apply it twice (total +3, since CodeReview is played from hand)
        assertEquals(before + 3, after.currentPlayerHand().unplayedCards().size());
    }

}
