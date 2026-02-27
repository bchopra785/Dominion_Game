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
import static org.mockito.Mockito.*;

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

    @Test
    public void testInitialState() {
        // Test that the initial state has expected placeholder values
        Engine engine = new Engine(players);
        GameState state = engine.getState();
        
        assertNotNull(state);
        assertEquals("placeholder", state.currentPlayerName());
        assertNotNull(state.currentPlayerHand());
        assertEquals(GameState.TurnPhase.ACTION, state.phase());
        assertEquals(-1, state.availableActions());
        assertEquals(-1, state.spendableMoney());
        assertEquals(-1, state.availableBuys());
        assertNotNull(state.buyableCards());
    }

    @Test
    public void testGetStateReturnsNonNullGameState() {
        // Test that getState returns a non-null GameState object
        Engine engine = new Engine(players);
        GameState state = engine.getState();
        assertNotNull(state);
    }

    @Test
    public void testEngineInitializesPlayerCardsMap() {
        // Test that engine properly initializes player cards for each player
        Engine engine = new Engine(players);
        // This will be implicitly tested through the play method or state
        assertDoesNotThrow(() -> engine.getState());
    }

    @Test
    public void testMultipleEnginesIndependence() {
        // Test that multiple engines are independent
        Engine engine1 = new Engine(players);
        
        List<ParentPlayer> players2 = new ArrayList<>();
        players2.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        players2.add(new ConsolePlayer(scanner, new PrintStream(outBuf)));
        Engine engine2 = new Engine(players2);
        
        GameState state1 = engine1.getState();
        GameState state2 = engine2.getState();
        
        assertNotNull(state1);
        assertNotNull(state2);
        // Both should have placeholder values but be independent objects
        assertEquals("placeholder", state1.currentPlayerName());
        assertEquals("placeholder", state2.currentPlayerName());
    }

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
        Engine engine = new Engine(players);
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
    public void testEngineWithMockedPlayer() {
        // Example Mockito test: use a mocked player
        when(mockPlayer.getName()).thenReturn("MockedPlayer");
        
        // Create a list with the mocked player
        List<ParentPlayer> mockPlayers = new ArrayList<>();
        mockPlayers.add(mockPlayer);
        
        // Create engine with mocked player
        Engine engine = new Engine(mockPlayers);
        
        // Verify the mock was used
        assertNotNull(engine);
        // Verify that getName was called (if needed in future implementation)
        verify(mockPlayer, atLeastOnce()).getName();
    }

    
}
