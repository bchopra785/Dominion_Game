package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import java.util.List;
import java.util.Map;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;


public class EvergreenTest {
    
    public EvergreenTest() {
        
    }
    
    public GameState play(GameState state, ParentPlayer player, List<ParentPlayer> players, Map<ParentPlayer, PlayerCards> playerCardsMap, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();
        
        // Draw 2 cards
        PlayerCards playerCards = playerCardsMap.get(player);
        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();    //create new record class hand

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand()); //buyable cards

        // Give all other players a BUG card
        for (ParentPlayer otherPlayer : players) {
            if (!otherPlayer.getName().equals(player.getName())) {
                Card bugCard = boardCards.drawDeckCard(Card.Type.BUG); //draw a bug card from the board
                if (bugCard != null) {
                    playerCardsMap.get(otherPlayer).gainCard(bugCard);
                }
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
