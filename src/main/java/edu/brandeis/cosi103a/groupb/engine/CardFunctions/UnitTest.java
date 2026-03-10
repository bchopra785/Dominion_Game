package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class UnitTest {

    public UnitTest() {}

    public GameState play(GameState state, ConsolePlayer player, PlayerCards playerCards, BoardCards boardCards) {

        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        // Choose one: +2 Actions; +$2; +2 Cards

        // Create custom decisions, but since no, use EndPhaseDecision for each

        // For simplicity, prompt with 3 options

        // But since makeDecision takes options, I can create a list with 3 EndPhaseDecision with different names, but no.

        // Since Decision is interface, but to make it, perhaps use the options as 3 different decisions.

        // For simplicity, I'll use 3 EndPhaseDecision, but they are the same.

        // Perhaps the player chooses by index.

        // So, options = ImmutableList.of(

        // new EndPhaseDecision(GameState.TurnPhase.ACTION), // 0: +2 Actions

        // new EndPhaseDecision(GameState.TurnPhase.BUY), // 1: +$2

        // new EndPhaseDecision(GameState.TurnPhase.CLEANUP) // 2: +2 Cards

        // );

        // Then, based on index.

        // Yes.

        ImmutableList<Decision> options = ImmutableList.of(

            new EndPhaseDecision(GameState.TurnPhase.ACTION), // 0: +2 Actions

            new EndPhaseDecision(GameState.TurnPhase.BUY), // 1: +$2

            new EndPhaseDecision(GameState.TurnPhase.CLEANUP) // 2: +2 Cards

        );

        Decision decision = player.makeDecision(state, options);

        int index = options.indexOf(decision);

        if (index == 0) {

            actionAmt += 2;

        } else if (index == 1) {

            totalMoney += 2;

            buyableCards = boardCards.getPlayableCards(totalMoney);

        } else if (index == 2) {

            playerCards.drawToHand();

            playerCards.drawToHand();

            handObject = playerCards.getHand();

            totalMoney = playerCards.getCostInHand();

            buyableCards = boardCards.getPlayableCards(totalMoney);

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