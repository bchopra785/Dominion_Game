package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;


public class CodeReview {
    
    public CodeReview() {
        
    }
    
    public GameState play(GameState state, ConsolePlayer player) {
        int actionAmt = state.availableActions();
        PlayerCards playerCards = player.getPlayerCards();
        
        //draw a card and give +2 actions
        playerCards.drawToHand();
        Hand currentHand = playerCards.getHand();    //create new record class hand
        actionAmt += 2;

        GameState newState = new GameState(
            state.currentPlayerName(),
            currentHand,
            state.phase(),
            actionAmt,
            state.spendableMoney(),
            state.availableBuys(),
            state.buyableCards()
        );

        return newState;
    }
}
