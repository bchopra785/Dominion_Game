package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConsolePlayerTest {

    private Decision makeDecisionStub() {
        return new EndPhaseDecision(GameState.TurnPhase.BUY);
    }

    private GameState makeState(String playerName, int spendableMoney) {
        return new GameState(
            playerName,
            new Hand(ImmutableList.of(), ImmutableList.of()),
            GameState.TurnPhase.BUY,
            1,
            spendableMoney,
            1,
            new CardStacks(ImmutableMap.of())
        );
    }

    @Test
    public void validSelection() {
        String input = "1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.<Decision>of(d0, d1);

        Decision chosen = p.makeDecision(null, options);
        assertSame(d1, chosen);
    }

    @Test
    public void nonNumericThenValid() {
        String input = "x\n0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.<Decision>of(d0, d1);

        Decision chosen = p.makeDecision(null, options);
        assertSame(d0, chosen);
    }

    @Test
    public void outOfRangeThenValid() {
        String input = "10\n0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.<Decision>of(d0, d1);

        Decision chosen = p.makeDecision(null, options);
        assertSame(d0, chosen);
    }

    @Test
    public void eofThrowsIllegalState() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.<Decision>of(d0, d1);

        assertThrows(IllegalStateException.class, () -> p.makeDecision(null, options));
    }

    @Test
    public void gameStateFieldsShown() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        GameState state = makeState("Bob", 5);
        ImmutableList<Decision> options = ImmutableList.<Decision>of(makeDecisionStub());

        p.makeDecision(state, options);
        String output = outBuf.toString();
        assertTrue(output.contains("Current Player: Bob"));
        assertTrue(output.contains("Phase: BUY"));
    }

    @Test
    public void nullOptionsReturnsNull() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision chosen = p.makeDecision(null, null);
        assertNull(chosen);
        assertTrue(outBuf.toString().contains("No available options; returning null"));
    }

    @Test
    public void emptyOptionsReturnsNull() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision chosen = p.makeDecision(null, ImmutableList.of());
        assertNull(chosen);
        assertTrue(outBuf.toString().contains("No available options; returning null"));
    }

    @Test
    public void constructorRejectsNullScanner() {
        assertThrows(IllegalArgumentException.class, () -> new ConsolePlayer((Scanner) null, System.out));
    }

    @Test
    public void constructorRejectsNullPrintStream() {
        assertThrows(IllegalArgumentException.class, () -> new ConsolePlayer(new Scanner(new ByteArrayInputStream(new byte[0])), null));
    }

    @Test
    public void showsOptionDescriptionsInPrompt() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.of(d0, d1);

        p.makeDecision(makeState("Alice", 3), options);

        String output = outBuf.toString();
        assertTrue(output.contains("Choose one of the following options:"));
        assertTrue(output.contains("[0]"));
        assertTrue(output.contains("[1]"));
    }
}
