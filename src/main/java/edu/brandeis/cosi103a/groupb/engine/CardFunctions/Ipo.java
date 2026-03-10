package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class Ipo {
    
    public Ipo() {
        
    }
    
    public GameState play(GameState state, ConsolePlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions() + 1; // +1 Action
        int totalMoney = state.spendableMoney() + 2; // +2 Money
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();
        
        // +2 Cards
        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();

        buyableCards = boardCards.getPlayableCards(totalMoney);

        GameState newState = new GameState(
            playerName,
            handObject,
            phase,
            actionAmt,
            totalMoney,
            availableBuys,
            buyableCards
        );

        return newState;
    }
}