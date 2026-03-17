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
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.EvergreenTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Refactor;

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

            // Play it once to move to played area

            playerCards.playCard(chosenCard);

            handObject = playerCards.getHand();

            // Execute the effect twice for supported cards

            GameState currentState = new GameState(

                playerName,

                handObject,

                phase,

                actionAmt,

                totalMoney,

                availableBuys,

                buyableCards

            );

            for (int i = 0; i < 2; i++) {

                if (chosenCard.type().equals(Card.Type.CODE_REVIEW)) {

                    CodeReview cr = new CodeReview();

                    currentState = cr.play(currentState, (ParentPlayer) player, playerCards, boardCards);

                } else if (chosenCard.type().equals(Card.Type.REFACTOR)) {

                    Refactor r = new Refactor();

                    currentState = r.play(currentState, (ParentPlayer) player, playerCards, boardCards);

                } else if (chosenCard.type().equals(Card.Type.EVERGREEN_TEST)) {

                    // Can't execute effect twice because signature requires more parameters

                    // For now, do nothing extra

                }

            }

            // Update state from the final currentState

            playerName = currentState.currentPlayerName();

            handObject = currentState.currentPlayerHand();

            phase = currentState.phase();

            actionAmt = currentState.availableActions();

            totalMoney = currentState.spendableMoney();

            availableBuys = currentState.availableBuys();

            buyableCards = currentState.buyableCards();

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