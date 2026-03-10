package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class MergeConflict {

    public MergeConflict() {}

    public GameState play(GameState state, ConsolePlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        // Trash a card from your hand

        ImmutableList<Card> unplayed = playerCards.getUnplayedCards();

        ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();

        for (Card card : unplayed) {

            optionsBuilder.add(new TrashCardDecision(card));

        }

        ImmutableList<Decision> options = optionsBuilder.build();

        Decision decision = player.makeDecision(state, options);

        if (decision instanceof TrashCardDecision) {

            Card trashedCard = ((TrashCardDecision) decision).card();

            playerCards.trashCard(trashedCard);

            // +1 Card per $1 it costs

            int cost = trashedCard.cost();

            for (int i = 0; i < cost; i++) {

                playerCards.drawToHand();

            }

            handObject = playerCards.getHand();

        }

        totalMoney = playerCards.getCostInHand();

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