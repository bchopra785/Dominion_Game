package edu.brandeis.cosi103a.groupb.engine.CardFunctions;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public final class ActionCards {

    private ActionCards() {
    }

    public static GameState playActionCard(
        Card playedCard,
        GameState state,
        ParentPlayer currentPlayer,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        if (playedCard == null) {
            throw new IllegalStateException("No action card was played");
        }

        Card.Type type = playedCard.type();
        switch (type) {
            case CODE_REVIEW:
                return playCodeReview(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case EVERGREEN_TEST:
                return playEvergreenTest(state, currentPlayer, players, playerCardsMap, boardCards);
            case REFACTOR:
                return playRefactor(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case BACKLOG:
                return playBacklog(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case MONITORING:
                return playMonitoring(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case IPO:
                return playIpo(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case MERGE_CONFLICT:
                return playMergeConflict(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case SPRINT_PLANNING:
                return playSprintPlanning(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case TECH_DEBT:
                return playTechDebt(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case UNIT_TEST:
                return playUnitTest(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            case HACK:
                return playHack(state, currentPlayer, players, playerCardsMap, boardCards);
            case RANSOMWARE:
                return playRansomware(state, currentPlayer, players, playerCardsMap, boardCards);
            case DAILY_SCRUM:
                return playDailyScrum(state, currentPlayer, players, playerCardsMap, boardCards);
            case PARALLELIZATION:
                return playParallelization(state, currentPlayer, players, playerCardsMap, boardCards);
            case DEPLOYMENT_PIPELINE:
                return playDeploymentPipeline(state, currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            default:
                throw new IllegalStateException("Card function not implemented for card: " + playedCard);
        }
    }

    public static boolean activatesCostReduction(Card playedCard) {
        return playedCard != null && playedCard.type().equals(Card.Type.DEPLOYMENT_PIPELINE);
    }

    static GameState playCodeReview(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();
        actionAmt += 2;
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand());

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playEvergreenTest(
        GameState state,
        ParentPlayer player,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        PlayerCards playerCards = playerCardsMap.get(player);
        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand());

        for (ParentPlayer otherPlayer : players) {
            if (!otherPlayer.getName().equals(player.getName())) {
                PlayerCards otherCards = playerCardsMap.get(otherPlayer);
                if (hasMonitoring(otherCards)) {
                    continue;
                }
                Card bugCard = boardCards.drawDeckCard(Card.Type.BUG);
                if (bugCard != null) {
                    otherCards.gainCard(bugCard);
                }
            }
        }

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playRefactor(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        handObject = playerCards.getHand();

        ImmutableList.Builder<TrashCardDecision> trashOptionsBuilder = new ImmutableList.Builder<>();
        for (Card card : handObject.unplayedCards()) {
            trashOptionsBuilder.add(new TrashCardDecision(card));
        }

        ImmutableList<TrashCardDecision> trashOptions = trashOptionsBuilder.build();
        Decision trashDecision = player.makeDecision(state, ImmutableList.copyOf(trashOptions));

        Card cardToTrash = ((TrashCardDecision) trashDecision).card();
        int trashedValue = cardToTrash.value() + 2;

        playerCards.trashCard(cardToTrash);
        handObject = playerCards.getHand();
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand());

        GameState newState = new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);

        CardStacks newBuyableCards = boardCards.getPlayableCards(trashedValue);
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        for (Card.Type cardType : newBuyableCards.getCardTypes()) {
            optionsBuilder.add(new GainCardDecision(cardType));
        }
        ImmutableList<Decision> options = optionsBuilder.build();

        if (options.isEmpty()) {
            return newState;
        }

        Decision gainCardDecision = player.makeDecision(newState, ImmutableList.copyOf(options));
        Card.Type cardTypeToBuy = ((GainCardDecision) gainCardDecision).cardType();
        Card gainedCard = boardCards.drawDeckCard(cardTypeToBuy);
        playerCards.gainCard(gainedCard);

        return newState;
    }

    static GameState playBacklog(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions() + 1;
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        int discarded = 0;
        while (true) {
            ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();
            ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();

            for (Card card : unplayed) {
                optionsBuilder.add(new DiscardCardDecision(card));
            }

            optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
            ImmutableList<Decision> options = optionsBuilder.build();
            Decision decision = player.makeDecision(state, options);

            if (decision instanceof EndPhaseDecision) {
                break;
            } else if (decision instanceof DiscardCardDecision) {
                Card toDiscard = ((DiscardCardDecision) decision).card();
                try {
                    discardCard(playerCards, toDiscard);
                } catch (Exception e) {
                    // Keep behavior identical to prior implementation.
                }
                discarded++;
                handObject = playerCards.getHand();
            }
        }

        for (int i = 0; i < discarded; i++) {
            playerCards.drawToHand();
        }

        handObject = playerCards.getHand();
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playMonitoring(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playIpo(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions() + 1;
        int totalMoney = state.spendableMoney() + 2;
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        playerCards.drawToHand();
        playerCards.drawToHand();
        handObject = playerCards.getHand();

        buyableCards = boardCards.getPlayableCards(totalMoney);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playMergeConflict(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();
        for (Card card : unplayed) {
            optionsBuilder.add(new TrashCardDecision(card));
        }

        ImmutableList<Decision> options = optionsBuilder.build();
        Decision decision = player.makeDecision(state, options);

        if (decision instanceof TrashCardDecision) {
            Card trashedCard = ((TrashCardDecision) decision).card();
            playerCards.trashCard(trashedCard);

            int cost = trashedCard.cost();
            for (int i = 0; i < cost; i++) {
                playerCards.drawToHand();
            }

            handObject = playerCards.getHand();
        }

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playSprintPlanning(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions() + 1;
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys() + 1;
        CardStacks buyableCards = state.buyableCards();

        playerCards.drawToHand();
        handObject = playerCards.getHand();

        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playTechDebt(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions() + 1;
        int totalMoney = state.spendableMoney() + 1;
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        playerCards.drawToHand();
        handObject = playerCards.getHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        Map<Card.Type, Integer> stacks = boardCards.getCardStacks();
        int empty = 0;
        for (int count : stacks.values()) {
            if (count == 0) {
                empty++;
            }
        }

        for (int i = 0; i < empty; i++) {
            ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();
            if (unplayed.isEmpty()) {
                break;
            }

            ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();
            for (Card card : unplayed) {
                optionsBuilder.add(new DiscardCardDecision(card));
            }

            ImmutableList<Decision> options = optionsBuilder.build();
            Decision decision = player.makeDecision(state, options);
            if (decision instanceof DiscardCardDecision) {
                Card toDiscard = ((DiscardCardDecision) decision).card();
                try {
                    discardCard(playerCards, toDiscard);
                } catch (Exception e) {
                    // Keep behavior identical to prior implementation.
                }
                handObject = playerCards.getHand();
            }
        }

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playUnitTest(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        ImmutableList<Decision> options = ImmutableList.of(
            new EndPhaseDecision(GameState.TurnPhase.ACTION),
            new EndPhaseDecision(GameState.TurnPhase.BUY),
            new EndPhaseDecision(GameState.TurnPhase.CLEANUP)
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

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playHack(
        GameState state,
        ParentPlayer player,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney() + 2;
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        buyableCards = boardCards.getPlayableCards(totalMoney);

        for (ParentPlayer other : players) {
            if (!other.getName().equals(player.getName())) {
                PlayerCards otherCards = playerCardsMap.get(other);
                if (hasMonitoring(otherCards)) {
                    continue;
                }

                while (otherCards.getUnplayedCards().size() > 3) {
                    ImmutableCollection<Card> unplayed = otherCards.getUnplayedCards();
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
                            // Keep behavior identical to prior implementation.
                        }
                    }
                }
            }
        }

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playRansomware(
        GameState state,
        ParentPlayer player,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        PlayerCards playerCards = playerCardsMap.get(player);
        for (int i = 0; i < 3; i++) {
            playerCards.drawToHand();
        }

        handObject = playerCards.getHand();
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        for (ParentPlayer other : players) {
            if (!other.getName().equals(player.getName())) {
                PlayerCards otherCards = playerCardsMap.get(other);
                if (hasMonitoring(otherCards)) {
                    continue;
                }

                ImmutableCollection<Card> unplayed = otherCards.getUnplayedCards();
                if (unplayed.size() >= 2) {
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
                                // Keep behavior identical to prior implementation.
                            }
                            unplayed = otherCards.getUnplayedCards();
                        }
                    }
                } else {
                    Card bugCard = boardCards.drawDeckCard(Card.Type.BUG);
                    if (bugCard != null) {
                        otherCards.gainCard(bugCard);
                    }
                }
            }
        }

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playDailyScrum(
        GameState state,
        ParentPlayer player,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = state.phase();
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys() + 1;
        CardStacks buyableCards = state.buyableCards();

        PlayerCards playerCards = playerCardsMap.get(player);
        for (int i = 0; i < 4; i++) {
            playerCards.drawToHand();
        }

        handObject = playerCards.getHand();
        totalMoney = playerCards.getCostInHand();
        buyableCards = boardCards.getPlayableCards(totalMoney);

        for (ParentPlayer otherPlayer : players) {
            if (!otherPlayer.getName().equals(player.getName())) {
                playerCardsMap.get(otherPlayer).drawToHand();
            }
        }

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playParallelization(
        GameState state,
        ParentPlayer player,
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney();
        int availableBuys = state.availableBuys();
        CardStacks buyableCards = state.buyableCards();

        PlayerCards playerCards = playerCardsMap.get(player);
        ImmutableCollection<Card> unplayed = playerCards.getUnplayedCards();

        ImmutableList.Builder<Decision> optionsBuilder = ImmutableList.builder();
        for (Card card : unplayed) {
            if (card.type().category() == Card.Type.Category.ACTION) {
                optionsBuilder.add(new PlayCardDecision(card));
            }
        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        ImmutableList<Decision> options = optionsBuilder.build();
        Decision decision = player.makeDecision(state, options);

        if (decision instanceof PlayCardDecision) {
            Card chosenCard = ((PlayCardDecision) decision).card();
            playerCards.playCard(chosenCard);
            handObject = playerCards.getHand();

            GameState currentState = new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
            for (int i = 0; i < 2; i++) {
                if (chosenCard.type().equals(Card.Type.CODE_REVIEW)) {
                    currentState = playCodeReview(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.REFACTOR)) {
                    currentState = playRefactor(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.EVERGREEN_TEST)) {
                    currentState = playEvergreenTest(currentState, player, players, playerCardsMap, boardCards);
                } else if (chosenCard.type().equals(Card.Type.HACK)) {
                    currentState = playHack(currentState, player, players, playerCardsMap, boardCards);
                } else if (chosenCard.type().equals(Card.Type.RANSOMWARE)) {
                    currentState = playRansomware(currentState, player, players, playerCardsMap, boardCards);
                } else if (chosenCard.type().equals(Card.Type.DAILY_SCRUM)) {
                    currentState = playDailyScrum(currentState, player, players, playerCardsMap, boardCards);
                } else if (chosenCard.type().equals(Card.Type.BACKLOG)) {
                    currentState = playBacklog(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.MONITORING)) {
                    currentState = playMonitoring(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.IPO)) {
                    currentState = playIpo(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.MERGE_CONFLICT)) {
                    currentState = playMergeConflict(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.SPRINT_PLANNING)) {
                    currentState = playSprintPlanning(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.TECH_DEBT)) {
                    currentState = playTechDebt(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.UNIT_TEST)) {
                    currentState = playUnitTest(currentState, player, playerCards, boardCards);
                } else if (chosenCard.type().equals(Card.Type.DEPLOYMENT_PIPELINE)) {
                    currentState = playDeploymentPipeline(currentState, player, playerCards, boardCards);
                }
            }

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

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    static GameState playDeploymentPipeline(GameState state, ParentPlayer player, PlayerCards playerCards, BoardCards boardCards) {
        String playerName = state.currentPlayerName();
        Hand handObject = state.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = state.availableActions();
        int totalMoney = state.spendableMoney() + 1;
        int availableBuys = state.availableBuys() + 1;
        CardStacks buyableCards = boardCards.getPlayableCards(totalMoney + 1);

        return new GameState(playerName, handObject, phase, actionAmt, totalMoney, availableBuys, buyableCards);
    }

    private static boolean hasMonitoring(PlayerCards playerCards) {
        for (Card c : playerCards.getUnplayedCards()) {
            if (c.type().name().equals("MONITORING")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static void discardCard(PlayerCards playerCards, Card card) throws Exception {
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