package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;


public class CodeReview {
    
    public CodeReview() {
        
    }
    
    public GameState play(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();
        
            //draw two cards and give +2 actions
            playerCards.drawToHand();
            playerCards.drawToHand();
        handObject = playerCards.getHand();    //create new record class hand
        actionAmt += 2;
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand());

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
