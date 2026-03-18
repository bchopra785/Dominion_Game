package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class Parallelization {

    public Parallelization() {}

    public GameState play(GameState state, ConsolePlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        // Prompt to choose an Action card from hand to play twice

         ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();

        ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();

        for (Card card : unplayed) {

            if (card.type().category() == Card.Type.Category.ACTION) {

                optionsBuilder.add(new PlayCardDecision(card));

            }

        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION)); // Don't play

        ImmutableList<Decision> options = optionsBuilder.build();

        Decision decision = player.makeDecision(state, options);

        if (decision instanceof PlayCardDecision) {

            Card chosenCard = ((PlayCardDecision) decision).card();

            // Play it once (since can't play twice without recursion)

            playerCards.playCard(chosenCard);

            handObject = playerCards.getHand();

            // Note: To play twice, would need to call the card's play method, but for now, just once

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