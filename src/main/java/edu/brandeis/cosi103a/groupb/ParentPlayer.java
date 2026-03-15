package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
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
        sb.append("Current Player: ").append(state.currentPlayerName()).append('\n');
        sb.append("Hand: ").append(formatHand(state.currentPlayerHand())).append('\n');
        sb.append("Phase: ").append(state.phase()).append('\n');
        sb.append("Actions Left: ").append(state.availableActions()).append('\n');
        sb.append("Money Left: ").append(state.spendableMoney()).append('\n');
        sb.append("Buys Left: ").append(state.availableBuys()).append('\n');
        sb.append("Buyable Cards: ").append(formatBuyableCards(state.buyableCards())).append('\n');
        return sb.toString();
    }

    private String formatHand(Hand hand) {
        ImmutableCollection<Card> playedCards = hand.playedCards();
        ImmutableCollection<Card> unplayedCards = hand.unplayedCards();
        if(playedCards.isEmpty() && unplayedCards.isEmpty()) {
            return "<empty hand>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("  PLAYED: ");
        for (Card card : playedCards) {
            sb.append(card.description()).append(", ");
        }
        sb.append("\n\tUNPLAYED: ");
        for (Card card : unplayedCards) {
            sb.append(card.description()).append(", ");
        }
        return sb.toString();
    }

    private String formatBuyableCards(CardStacks buyableCards) {
        ImmutableSet<Card.Type> types = buyableCards.getCardTypes();

        if (types == null || types.isEmpty()) {
            return "<no buyable cards>";
        }

        StringBuilder sb = new StringBuilder();
        for (Card.Type type : types) {
            sb.append(type.name()).append(": ").append(buyableCards.getNumAvailable(type)).append(", ");
        }
        return sb.toString();
    }
}
