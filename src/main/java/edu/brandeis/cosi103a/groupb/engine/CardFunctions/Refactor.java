package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import com.google.common.collect.ImmutableList;


public class Refactor {
    
    public Refactor() {
        
    }
    
    public GameState play(GameState state, ConsolePlayer player, BoardCards boardCards) {
        PlayerCards playerCards = player.getPlayerCards();
        Hand currentHand = playerCards.getHand();

        // Create a list of TrashCardDecision for each card in hand
        ImmutableList.Builder<TrashCardDecision> trashOptionsBuilder = new ImmutableList.Builder<>();

        //go through all cards in hand and create a trash option for each one to present to the player as a decision
        for (Card card : currentHand.unplayedCards()) {
            trashOptionsBuilder.add(new TrashCardDecision(card));
        }

        ImmutableList<TrashCardDecision> trashOptions = trashOptionsBuilder.build();

        // Call makeDecision with the trash options
        Decision trashDecision = player.makeDecision(state, ImmutableList.copyOf(trashOptions));

        // Extract the card that was chosen and get its value
        Card cardToTrash = ((TrashCardDecision) trashDecision).card();
        int trashedValue = cardToTrash.value() + 2;

        // Trash the card
        playerCards.trashCard(cardToTrash);

        GameState newState = new GameState(
            state.currentPlayerName(),
            playerCards.getHand(),
            state.phase(),
            state.availableActions(),
            state.spendableMoney(),
            state.availableBuys(),
            state.buyableCards()
        );

        CardStacks newBuyableCards = boardCards.getPlayableCards(trashedValue);
        
        // Create a list of BuyDecision for each card type in newBuyableCards
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        for (Card.Type cardType : newBuyableCards.getCardTypes()) {
            optionsBuilder.add(new BuyDecision(cardType));
        }
        ImmutableList<Decision> options = optionsBuilder.build();

        // Call makeDecision with the buy options
        Decision buyDecision = player.makeDecision(newState, ImmutableList.copyOf(options));

        // Extract the card type from buyDecision and gain the card
        Card.Type cardTypeToBuy = ((BuyDecision) buyDecision).cardType();
        Card gainedCard = boardCards.drawDeckCard(cardTypeToBuy);
        playerCards.gainCard(gainedCard);

        return newState;
    }
}
