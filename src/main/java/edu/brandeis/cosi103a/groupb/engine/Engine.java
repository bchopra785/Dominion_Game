package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;


//client
public class Engine implements edu.brandeis.cosi.atg.engine.Engine {

    private final List<ParentPlayer> players;
    private final BoardCards boardCards;
    private final Map<ParentPlayer, PlayerCards> playerCardsMap;
    private final ActionCardHandler actionCardHandler;

    // Mutable state is internal only; external API still exposes immutable GameState snapshots.
    private MutableGameState mutableState;
    private boolean costReductionActive;
    private Optional<Event> latestEventReason;

    public Engine(List<ParentPlayer> players) {
        this(players, null);
    }

    /**
     * Create an Engine with optional forced board card exclusions.
     * Used for optimization where specific cards must be excluded from the board.
     * 
     * @param players List of players
     * @param forcedExcludeCards Set of card types to exclude from board (null for random selection)
     */
    public Engine(List<ParentPlayer> players, Set<Card.Type> forcedExcludeCards) {
        
        //check for valid number of players
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > 4) {
            throw new IllegalArgumentException("Engine supports at most 4 players");
        }

        this.players = players;
        this.costReductionActive = false;

        //initialize board cards and player cards
        if (forcedExcludeCards != null) {
            this.boardCards = new BoardCards(players.size(), forcedExcludeCards);
        } else {
            this.boardCards = new BoardCards(players.size());
        }
        this.playerCardsMap = new HashMap<>();
        this.actionCardHandler = new ActionCardHandler(players, playerCardsMap, boardCards);
        this.latestEventReason = Optional.empty();

        for (ParentPlayer player : players) {
            PlayerCards playerCards = new PlayerCards(boardCards);
            playerCards.refreshHand();
            playerCardsMap.put(player, playerCards);
        }
    }

    public GameState getState() {
        if (mutableState == null) {
            throw new IllegalStateException("Game has not started yet");
        }
        return mutableState.toGameState();
    }

    private void updateState(GameState newState) {
        this.mutableState = new MutableGameState(newState);
    }

    public GameResult play() throws PlayerViolationException {
        boolean gameOver = false;
        while (!gameOver) {
            for (ParentPlayer player : players) {
                // Initialize per-turn mutable snapshot
                startTurn(player);

                // ACTION phase
                mutableState.setPhase(GameState.TurnPhase.ACTION);
                GameState actionState = runActionPhase();

                // MONEY phase
                mutableState.setPhase(GameState.TurnPhase.MONEY);
                GameState moneyState = runMoneyPhase(actionState);

                // BUY phase
                mutableState.setPhase(GameState.TurnPhase.BUY);
                runBuyPhase(moneyState);

                // CLEANUP
                mutableState.setPhase(GameState.TurnPhase.CLEANUP);
                cleanupPhase(player);
                publishEvent(new EndTurnEvent());
            }

            gameOver = !boardCards.frameworksLeft();
        }

        List<PlayerResult> resultsList = new ArrayList<>();
        for (ParentPlayer player : players) {
            PlayerCards playerCards = playerCardsMap.get(player);
            ImmutableCollection<Card> allCards = playerCards.getDiscardPile();
            PlayerResult result = new PlayerResult(player.getName(), playerCards.getScore(), allCards);
            resultsList.add(result);
        }

        resultsList.sort((a, b) -> Integer.compare(b.score(), a.score()));
        ImmutableList<PlayerResult> playerResults = ImmutableList.copyOf(resultsList);
        return new GameResult(playerResults);
    }

    private void startTurn(ParentPlayer player) {
        this.costReductionActive = false;
        // Start from a fresh per-turn snapshot; all later mutations stay inside MutableGameState.
        this.mutableState = new MutableGameState(
            player.getName(),
            playerCardsMap.get(player).getHand(),
            GameState.TurnPhase.ACTION,
            1,
            0,
            1,
            boardCards.getPlayableCards(0)
        );
    }

    private GameState runActionPhase() {
        GameState state = getState();
        Decision decision = actionDecision(state);
        // Keep consuming actions until the player explicitly ends the ACTION phase.
        while (!isEndPhaseDecision(decision, GameState.TurnPhase.ACTION)) {
            state = actionPhase(state, decision);
            decision = actionDecision(state);
        }
        return state;
    }

    private GameState runMoneyPhase(GameState state) {
        Decision decision = moneyDecision(state);
        // MONEY phase mirrors ACTION: resolve plays until EndPhaseDecision(MONEY).
        while (!isEndPhaseDecision(decision, GameState.TurnPhase.MONEY)) {
            state = moneyPhase(state, decision);
            decision = moneyDecision(state);
        }
        return state;
    }

    private GameState runBuyPhase(GameState state) {
        Decision decision = buyDecision(state);
        // BUY phase resolves one buy decision at a time until the player ends the phase.
        while (!isEndPhaseDecision(decision, GameState.TurnPhase.BUY)) {
            state = buyPhase(state, decision);
            decision = buyDecision(state);
        }
        return state;
    }

    private boolean isEndPhaseDecision(Decision decision, GameState.TurnPhase expectedPhase) {
        return decision instanceof EndPhaseDecision && ((EndPhaseDecision) decision).phase().equals(expectedPhase);
    }

    private GameState cleanupPhase(ParentPlayer currentPlayer) {
        PlayerCards playerCards = playerCardsMap.get(currentPlayer);
        playerCards.refreshHand();

        mutableState.setCurrentPlayerHand(playerCards.getHand());
        mutableState.setAvailableActions(1);
        mutableState.setSpendableMoney(0);
        mutableState.setAvailableBuys(1);
        mutableState.setBuyableCards(boardCards.getPlayableCards(0));

        return getState();
    }

    private void publishEvent(Event event) {
        latestEventReason = Optional.ofNullable(event);
        GameState snapshot = getState();
        for (ParentPlayer player : players) {
            player.getObserver().ifPresent(observer -> observer.notifyEvent(snapshot, event));
        }
    }

    private Decision moneyDecision(GameState oldState) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        ImmutableCollection<Card> unplayedCards = playerCardsMap.get(currentPlayer).getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        for (Card card : unplayedCards) {
            if (getCardCategory(card).equals(Card.Type.Category.MONEY)) {
                optionsBuilder.add(new PlayCardDecision(card));
            }
        }

        ImmutableList<Decision> options = optionsBuilder.build();

        while (true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            }
            System.out.println("Invalid decision. Please choose a valid option.");
        }
    }

    private GameState moneyPhase(GameState oldState, Decision decision) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        if (decision instanceof PlayCardDecision) {
            Card playedCard = ((PlayCardDecision) decision).card();
            playerCardsMap.get(currentPlayer).playCard(playedCard);
            publishEvent(new PlayCardEvent(playedCard, currentPlayer.getName()));
            mutableState.setSpendableMoney(mutableState.getSpendableMoney() + playedCard.value());
            mutableState.setCurrentPlayerHand(playerCardsMap.get(currentPlayer).getHand());
            mutableState.setBuyableCards(boardCards.getPlayableCards(mutableState.getSpendableMoney()));
        }

        return getState();
    }

    private Decision buyDecision(GameState oldState) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        if (mutableState.getAvailableBuys() > 0) {
            for (Card.Type cardType : mutableState.getBuyableCards().getCardTypes()) {
                optionsBuilder.add(new BuyDecision(cardType));
            }
        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        ImmutableList<Decision> options = optionsBuilder.build();

        while (true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            }
            System.out.println("Invalid decision. Please choose a valid option.");
        }
    }

    private GameState buyPhase(GameState oldState, Decision decision) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        Card.Type cardTypeToBuy = null;
        if (decision instanceof BuyDecision) {
            cardTypeToBuy = ((BuyDecision) decision).cardType();
        }
        if (cardTypeToBuy != null) {
            Card gainedCard = boardCards.drawDeckCard(cardTypeToBuy);
            playerCardsMap.get(currentPlayer).gainCard(gainedCard);
            publishEvent(new GainCardEvent(cardTypeToBuy, currentPlayer.getName()));
            mutableState.setSpendableMoney(mutableState.getSpendableMoney() - (gainedCard.cost() - (costReductionActive ? 1 : 0)));
            mutableState.setAvailableBuys(mutableState.getAvailableBuys() - 1);
            mutableState.setBuyableCards(boardCards.getPlayableCards(mutableState.getSpendableMoney()));
            
        }

        return getState();
    }

    private Decision actionDecision(GameState oldState) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        ImmutableCollection<Card> unplayedCards = playerCardsMap.get(currentPlayer).getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();

        if (mutableState.getAvailableActions() > 0) {
            for (Card card : unplayedCards) {
                Card.Type.Category category = getCardCategory(card);
                if (category.equals(Card.Type.Category.ACTION)) {
                    if (card.type().equals(Card.Type.MERGE_CONFLICT) || card.type().equals(Card.Type.REFACTOR) || card.type().equals(Card.Type.PARALLELIZATION)) {
                        if(unplayedCards.size() ==1){
                            continue; //skip since these cards require at least 1 other card
                        }
                    } 
                    optionsBuilder.add(new PlayCardDecision(card));
                }
            }
        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));
        ImmutableList<Decision> options = optionsBuilder.build();

        while (true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            }
            System.out.println("Invalid decision. Please choose a valid option.");
        }
    }

    private GameState actionPhase(GameState oldState, Decision decision) {
        ParentPlayer currentPlayer = getPlayerByName(mutableState.getCurrentPlayerName());

        // The action being resolved always consumes one available action.
        mutableState.setAvailableActions(mutableState.getAvailableActions() - 1);

        Card playedCard = null;
        if (decision instanceof PlayCardDecision) {
            playedCard = ((PlayCardDecision) decision).card();
            playerCardsMap.get(currentPlayer).playCard(playedCard);
            publishEvent(new PlayCardEvent(playedCard, currentPlayer.getName()));
        }

        mutableState.setCurrentPlayerHand(playerCardsMap.get(currentPlayer).getHand());

        GameState newState = null;
        if (playedCard != null) {
            // Action-card effects are delegated to keep Engine focused on phase orchestration.
            newState = actionCardHandler.execute(playedCard, getState(), currentPlayer);
            if (actionCardHandler.activatesCostReduction(playedCard)) {
                this.costReductionActive = true;
            }
        }

        if (newState == null) {
            throw new IllegalStateException("Card function not implemented for card: " + playedCard);
        }

        updateState(newState);
        return getState();
    }

    private boolean checkDecision(Decision decision, ImmutableList<Decision> options) {
        for (Decision option : options) {
            if (option.equals(decision)) {
                return true;
            }
        }
        return false;
    }

    private ParentPlayer getPlayerByName(String playerName) {
        for (ParentPlayer player : players) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        throw new IllegalStateException("Current player not found: " + playerName);
    }

    private Card.Type.Category getCardCategory(Card card) {
        return card.type().category();
    }
}
