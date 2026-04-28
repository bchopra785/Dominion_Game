package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;

/**
 * Internal mutable mirror for GameState to reduce repeated record allocations
 * while Engine updates fields multiple times within a phase.
 */
final class MutableGameState {

    private String currentPlayerName;
    private Hand currentPlayerHand;
    private GameState.TurnPhase phase;
    private int availableActions;
    private int spendableMoney;
    private int availableBuys;
    private CardStacks buyableCards;

    MutableGameState(GameState source) {
        this(
            source.currentPlayerName(),
            source.currentPlayerHand(),
            source.phase(),
            source.availableActions(),
            source.spendableMoney(),
            source.availableBuys(),
            source.buyableCards()
        );
    }

    MutableGameState(
        String currentPlayerName,
        Hand currentPlayerHand,
        GameState.TurnPhase phase,
        int availableActions,
        int spendableMoney,
        int availableBuys,
        CardStacks buyableCards
    ) {
        this.currentPlayerName = currentPlayerName;
        this.currentPlayerHand = currentPlayerHand;
        this.phase = phase;
        this.availableActions = availableActions;
        this.spendableMoney = spendableMoney;
        this.availableBuys = availableBuys;
        this.buyableCards = buyableCards;
    }

    String getCurrentPlayerName() {
        return currentPlayerName;
    }

    void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    Hand getCurrentPlayerHand() {
        return currentPlayerHand;
    }

    void setCurrentPlayerHand(Hand currentPlayerHand) {
        this.currentPlayerHand = currentPlayerHand;
    }

    GameState.TurnPhase getPhase() {
        return phase;
    }

    void setPhase(GameState.TurnPhase phase) {
        this.phase = phase;
    }

    int getAvailableActions() {
        return availableActions;
    }

    void setAvailableActions(int availableActions) {
        this.availableActions = availableActions;
    }

    int getSpendableMoney() {
        return spendableMoney;
    }

    void setSpendableMoney(int spendableMoney) {
        this.spendableMoney = spendableMoney;
    }

    int getAvailableBuys() {
        return availableBuys;
    }

    void setAvailableBuys(int availableBuys) {
        this.availableBuys = availableBuys;
    }

    CardStacks getBuyableCards() {
        return buyableCards;
    }

    void setBuyableCards(CardStacks buyableCards) {
        this.buyableCards = buyableCards;
    }

    GameState toGameState() {
        return new GameState(
            currentPlayerName,
            currentPlayerHand,
            phase,
            availableActions,
            spendableMoney,
            availableBuys,
            buyableCards
        );
    }
}
