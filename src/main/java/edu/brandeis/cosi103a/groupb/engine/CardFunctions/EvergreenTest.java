package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import java.util.List;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;


public class EvergreenTest {
    
    public EvergreenTest() {
        
    }
    
    public GameState play(GameState state, ConsolePlayer player, List<ConsolePlayer> players, BoardCards boardCards) {
        PlayerCards playerCards = player.getPlayerCards();
        
        // Draw 2 cards
        playerCards.drawToHand();
        playerCards.drawToHand();
        Hand currentHand = playerCards.getHand();    //create new record class hand

        // Give all other players a BUG card
        for (ConsolePlayer otherPlayer : players) {
            if (!otherPlayer.getName().equals(player.getName())) {
                Card bugCard = boardCards.drawDeckCard(Card.Type.BUG); //draw a bug card from the board
                if (bugCard != null) {
                    otherPlayer.getPlayerCards().gainCard(bugCard);
                }
            }
        }

        GameState newState = new GameState(
            state.currentPlayerName(),
            currentHand,
            state.phase(),
            state.availableActions(),
            state.spendableMoney(),
            state.availableBuys(),
            state.buyableCards()
        );

        return newState;
    }
}
