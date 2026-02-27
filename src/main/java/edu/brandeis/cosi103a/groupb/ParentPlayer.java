package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

/**
 * Abstract parent class for all player types in ATG.  It implements
 * the ATG Player interface so the Engine can treat all players uniformly.
 *
 * This class holds shared state (name, points, card container) and
 * provides simple accessors.  Concrete subclasses are responsible for
 * implementing the decision logic 
 */
public abstract class ParentPlayer implements edu.brandeis.cosi.atg.player.Player {

    private final String name;          // immutable player name
    private int points;                 // accumulated victory points
    private PlayerCards playerCards;    // deck/hand manager (assigned by Engine)

    /**
     * Create a player with the given name.
     */
    public ParentPlayer(String name) {
        this.name = name;
        this.points = 0;
    }

    /**
     * Add victory points to this player.
     */
    public void addPoints(int amount) {
        this.points += amount;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public void setPlayerCards(PlayerCards cards) {
        this.playerCards = cards;
    }

    public PlayerCards getPlayerCards() {
        return playerCards;
    }

    /**
     * Subclasses must implement this to choose a Decision from the
     * list of options based on the provided game state.
     */
    public abstract Decision makeDecision(GameState state, ImmutableList<Decision> options);

    /**
     * Helper to format a GameState for human-readable output.  Includes all
     * standard components that players are expected to see when making a
     * decision.
     *
     * @param state the game state
     * @return multi-line description, or empty string if state is null
     */
    protected String describeGameState(GameState state) {
        if (state == null) {
            return "<no game state>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("currentPlayerName: ").append(state.currentPlayerName()).append('\n');
        sb.append("currentPlayerHand: ").append(state.currentPlayerHand()).append('\n');
        sb.append("phase: ").append(state.phase()).append('\n');
        sb.append("availableActions: ").append(state.availableActions()).append('\n');
        sb.append("spendableMoney: ").append(state.spendableMoney()).append('\n');
        sb.append("availableBuys: ").append(state.availableBuys()).append('\n');
        sb.append("buyableCards: ").append(state.buyableCards()).append('\n');
        return sb.toString();
    }
}
