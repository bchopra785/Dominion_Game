package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("null")
public class ConsolePlayerTest {

    // Use concrete Decision implementations from the ATG API (EndPhaseDecision)
    private Decision makeDecisionStub() {
        // EndPhaseDecision is a record that requires a GameState.TurnPhase parameter.
        edu.brandeis.cosi.atg.state.GameState.TurnPhase phase;
        try {
            phase = edu.brandeis.cosi.atg.state.GameState.TurnPhase.values()[0];
        } catch (Exception e) {
            // fallback to null if reflection fails (shouldn't happen with API present)
            phase = null;
        }
        return new EndPhaseDecision(phase);
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
    public void eofSelectsDefault() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        Decision d0 = makeDecisionStub();
        Decision d1 = makeDecisionStub();
        ImmutableList<Decision> options = ImmutableList.<Decision>of(d0, d1);

        Decision chosen = p.makeDecision(null, options);
        assertSame(d0, chosen);
    }

    @Test
    public void gameStateFieldsShown() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ConsolePlayer p = new ConsolePlayer(in, new PrintStream(outBuf));

        GameState state = new GameState(
            "Bob",
            null,
            GameState.TurnPhase.BUY,
            2,
            5,
            1,
            null
        );
        ImmutableList<Decision> options = ImmutableList.<Decision>of(makeDecisionStub());

        p.makeDecision(state, options);
        String output = outBuf.toString();
        assertTrue(output.contains("currentPlayerName: Bob"));
        assertTrue(output.contains("phase: BUY"));
    }
}
