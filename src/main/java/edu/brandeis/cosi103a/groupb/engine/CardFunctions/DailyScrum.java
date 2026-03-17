package edu.brandeis.cosi103a.groupb.engine.CardFunctions;
import edu.brandeis.cosi103a.groupb.ParentPlayer;

import java.util.List;
import java.util.Map;

import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class DailyScrum {
    
    public DailyScrum() {
        
    }
    
    public GameState play(GameState state, ParentPlayer player, List<ParentPlayer> players, Map<ParentPlayer, PlayerCards> playerCardsMap, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys() + 1; // +1 Buy
        CardStacks buyableCards = state.buyableCards();
        
        // +4 Cards
        PlayerCards playerCards = playerCardsMap.get(player);
        for (int i = 0; i < 4; i++) {
            playerCards.drawToHand();
        }
        handObject = playerCards.getHand();

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        // Each other player draws a card
        for (ParentPlayer otherPlayer : players) {
            if (!otherPlayer.getName().equals(player.getName())) {
                playerCardsMap.get(otherPlayer).drawToHand();
            }
        }

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