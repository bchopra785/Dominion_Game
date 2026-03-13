package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

import java.lang.reflect.Field;
import java.util.List;

/** Check the changing to public, try to work around that!!! */
public class Backlog {

    public Backlog() {}

    public GameState play(GameState state, ConsolePlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions() + 1; // +1 Action
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        // Discard phase

        int discarded = 0;

        while (true) {

             ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();

            ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();

            for (Card card : unplayed) {

                optionsBuilder.add(new DiscardCardDecision(card));

            }

            optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION)); // End discard

            ImmutableList<Decision> options = optionsBuilder.build();

            Decision decision = player.makeDecision(state, options);

            if (decision instanceof EndPhaseDecision) {

                break;

            } else if (decision instanceof DiscardCardDecision) {

                Card toDiscard = ((DiscardCardDecision) decision).card();

                try {
                    discardCard(playerCards, toDiscard);
                } catch (Exception e) {
                    // Handle exception
                }

                discarded++;

                handObject = playerCards.getHand();

            }

        }

        // Draw discarded number of cards

        for (int i = 0; i < discarded; i++) {

            playerCards.drawToHand();

        }

        handObject = playerCards.getHand();

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

    private void discardCard(PlayerCards playerCards, Card card) throws Exception {

        Field unplayedField = PlayerCards.class.getDeclaredField("unplayedCards");

        unplayedField.setAccessible(true);

        List<Card> unplayed = (List<Card>) unplayedField.get(playerCards);

        if (unplayed.contains(card)) {

            unplayed.remove(card);

            Field discardField = PlayerCards.class.getDeclaredField("discard");

            discardField.setAccessible(true);

            List<Card> discard = (List<Card>) discardField.get(playerCards);

            discard.add(card);

        }

    }

}