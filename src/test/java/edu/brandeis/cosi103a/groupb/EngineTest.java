package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.engine.Engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EngineTest {
    
    private List<ParentPlayer> players;
    private ByteArrayOutputStream outBuf;
    private Scanner scanner;
    
    @Mock
    private ConsolePlayer mockPlayer;

    @BeforeEach
    public void setUp() {
        // Set up players before each test
        outBuf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBuf);
        String input = "0\n"; // Default input for players
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(in);
        
        players = new ArrayList<>();
        players.add(new ConsolePlayer(scanner, out));
        players.add(new ConsolePlayer(scanner, out));
    }

    @Test
    public void testConstructorWithValidPlayers() {
        // Test that engine can be constructed with 2 players
        assertDoesNotThrow(() -> new Engine(players));
    }

    @Test
    public void testConstructorWithOnePlayer() {
        // Test with just one player
        List<ParentPlayer> onePlayer = new ArrayList<>();
        onePlayer.add(players.get(0));
        assertDoesNotThrow(() -> new Engine(onePlayer));
    }

    @Test
    public void testConstructorWithFourPlayers() {
        // Test with maximum number of players (4)
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        assertDoesNotThrow(() -> new Engine(players));
    }

    @Test
    public void testConstructorWithNullPlayersThrowsException() {
        // Test that null players list throws IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Engine(null);
        });
        assertEquals("Players list cannot be null", exception.getMessage());
    }

    @Test
    public void testConstructorWithTooManyPlayersThrowsException() {
        // Test that more than 4 players throws IllegalArgumentException
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf))); // 5th player
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Engine(players);
        });
        assertEquals("Engine supports at most 4 players", exception.getMessage());
    }

    // @Test
    // public void testEngineInitializesPlayerCardsMap() {
    //     // Test that engine properly initializes player cards for each player
    //     Scanner scanner = new Scanner(System.in);
    //     List<ParentPlayer> players = new ArrayList<>();
    //     players.add(new ConsolePlayer(scanner, System.out));
    //     players.add(new ConsolePlayer(scanner, System.out));
    //     players.add(new BigMoneyPlayer());
    //     players.add(new BigMoneyPlayer());
    //     Engine engine = new Engine(players);
    //     GameState state = engine.getState(); // trigger initialization
    //     assertNotNull(state.currentPlayerName());
    //     assertNotNull(state.currentPlayerHand()); // trigger player cards map access
    // }


    @Test
    public void testEngineWithEmptyPlayersList() {
        // Test that engine works with empty players list (edge case)
        List<ParentPlayer> emptyPlayers = new ArrayList<>();
        assertDoesNotThrow(() -> new Engine(emptyPlayers));
    }

    @Test
    public void testPlayerNamesAreUnique() {
        // Test that each player gets a unique name
        String name1 = players.get(0).getName();
        String name2 = players.get(1).getName();
        
        assertNotEquals(name1, name2);
        assertTrue(name1.startsWith("ConsolePlayer-"));
        assertTrue(name2.startsWith("ConsolePlayer-"));
    }

    @Test
    public void testEngineStateConsistency() {
        // Test that calling getState multiple times returns consistent data
        String input = ""; // only 2 inputs, will run out during game
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new BigMoneyPlayer());
        players.add(new BigMoneyPlayer());

        Engine engine = new Engine(players);
        
        // Expect IllegalStateException when input runs out
        assertThrows(IllegalStateException.class, () -> {
            engine.play();
        });
        
        GameState state1 = engine.getState();
        GameState state2 = engine.getState();
        
        assertEquals(state1.currentPlayerName(), state2.currentPlayerName());
        assertEquals(state1.availableActions(), state2.availableActions());
        assertEquals(state1.spendableMoney(), state2.spendableMoney());
        assertEquals(state1.availableBuys(), state2.availableBuys());
    }

    @Test
    public void testEngineWithThreePlayers() {
        // Test with an odd number of players
        players.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        assertEquals(3, players.size());
        assertDoesNotThrow(() -> new Engine(players));
    }

    @Test
    public void testEnginePlayBigMoney(){
        // Test that engine can run a game without throwing exceptions
        List<ParentPlayer> testPlayers = new ArrayList<>();
        testPlayers.add(new BigMoneyPlayer());
        testPlayers.add(new BigMoneyPlayer());
        Engine engine = new Engine(testPlayers);
        
        assertDoesNotThrow(() -> {
            engine.play();
        });
        GameState state = engine.getState();
        assertNotNull(state);
        assertNotNull(state.currentPlayerName());
        assertNotNull(state.currentPlayerHand());
        assertEquals(GameState.TurnPhase.CLEANUP, state.phase());
        assertEquals(1, state.availableActions());
        assertEquals(0, state.spendableMoney());
        assertEquals(1, state.availableBuys());
        assertNotNull(state.buyableCards());

    }

    @Test
    public void testEnginePlayMix1(){
        // Test that engine throws exception when input runs out after 2 decisions
        String input = ""; // only 2 inputs, will run out during game
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new BigMoneyPlayer());
        players.add(new BigMoneyPlayer());
        Engine engine = new Engine(players);
        
        // Expect IllegalStateException when input runs out
        assertThrows(IllegalStateException.class, () -> {
            engine.play();
        });

        GameState state = engine.getState();
        assertNotNull(state);
        assertNotNull(state.currentPlayerName());
        assertNotNull(state.currentPlayerHand());
        assertEquals(GameState.TurnPhase.ACTION, state.phase());
        assertEquals(1, state.availableActions());
        assertEquals(1,state.availableBuys());
        assertNotNull(state.buyableCards());

    }

    @Test
    public void testEnginePlayMix2(){
        // Test that engine throws exception when input runs out after 2 decisions
        String input = "0\n"; // only 2 inputs, will run out during game
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new BigMoneyPlayer());
        players.add(new BigMoneyPlayer());
        Engine engine = new Engine(players);
        
        // Expect IllegalStateException when input runs out
        assertThrows(IllegalStateException.class, () -> {
            engine.play();
        });

        GameState state = engine.getState();
        assertNotNull(state);
        assertNotNull(state.currentPlayerName());
        assertNotNull(state.currentPlayerHand());
        assertEquals(GameState.TurnPhase.MONEY, state.phase());
        assertEquals(1, state.availableActions());
        assertEquals(1,state.availableBuys());
        assertNotNull(state.buyableCards());

    }

    @Test
    public void testEnginePlayMix3(){
        // Test that engine throws exception when input runs out after 2 decisions
        String input = "0\n0\n"; // only 2 inputs, will run out during game
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        
        List<ParentPlayer> players = new ArrayList<>();
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new ConsolePlayer(scanner, System.out));
        players.add(new BigMoneyPlayer());
        players.add(new BigMoneyPlayer());
        Engine engine = new Engine(players);
        
        // Expect IllegalStateException when input runs out
        assertThrows(IllegalStateException.class, () -> {
            engine.play();
        });

        GameState state = engine.getState();
        assertNotNull(state);
        assertNotNull(state.currentPlayerName());
        assertNotNull(state.currentPlayerHand());
        assertEquals(GameState.TurnPhase.BUY, state.phase());
        assertEquals(1, state.availableActions());
        assertEquals(1,state.availableBuys());
        assertNotNull(state.buyableCards());

    }

    
}
