package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class Ransomware {

    public Ransomware() {}

    public GameState play(GameState state, ConsolePlayer player, List<ConsolePlayer> players, Map<ConsolePlayer, PlayerCards> playerCardsMap, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        PlayerCards playerCards = playerCardsMap.get(player);

        // +3 Cards

        for (int i = 0; i < 3; i++) {

            playerCards.drawToHand();

        }

        handObject = playerCards.getHand();

        totalMoney = playerCards.getCostInHand();

        buyableCards = boardCards.getPlayableCards(totalMoney);

        // Each other player chooses one: discard 2 cards; or gain a Bug

        for (ConsolePlayer other : players) {

            if (!other.getName().equals(player.getName())) {

                PlayerCards otherCards = playerCardsMap.get(other);

                // If the other player has a Monitoring card in hand, they avoid this attack
                if (hasMonitoring(otherCards)) {
                    continue;
                }

                ImmutableCollection<Card> unplayed = otherCards.getUnplayedCards();

                // For simplicity, if they have 2 or more cards, discard 2, else gain bug

                if (unplayed.size() >= 2) {

                    // Discard 2

                    for (int i = 0; i < 2; i++) {

                        ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();

                        for (Card card : unplayed) {

                            optionsBuilder.add(new DiscardCardDecision(card));

                        }

                        ImmutableList<Decision> options = optionsBuilder.build();

                        Decision decision = other.makeDecision(state, options);

                        if (decision instanceof DiscardCardDecision) {

                            Card toDiscard = ((DiscardCardDecision) decision).card();

                            try {
                                discardCard(otherCards, toDiscard);
                            } catch (Exception e) {
                                // Handle
                            }

                            unplayed = otherCards.getUnplayedCards();

                        }

                    }

                } else {

                    // Gain a Bug

                    Card bugCard = boardCards.drawDeckCard(Card.Type.BUG);

                    if (bugCard != null) {

                        otherCards.gainCard(bugCard);

                    }

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

    private boolean hasMonitoring(PlayerCards playerCards) {
        for (Card c : playerCards.getUnplayedCards()) {
            if (c.type().name().equals("MONITORING")) {
                return true;
            }
        }
        return false;
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