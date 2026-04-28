package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.*;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.*;
import edu.brandeis.cosi.atg.event.EndTurnEvent;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.event.GainCardEvent;
import edu.brandeis.cosi.atg.event.PlayCardEvent;
import edu.brandeis.cosi.atg.state.*;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.ActionCards;
import java.util.UUID;

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

    //initialize players and board cards
    private final List<ParentPlayer> players;
    private final BoardCards boardCards;
    private final Map<ParentPlayer, PlayerCards> playerCardsMap;
    
    //initialize values for game state
    private String playerName;
    private Hand handObject;
    private GameState.TurnPhase phase;
    private int availableActions;
    private int spendableMoney;
    private int availableBuys;
    private CardStacks buyableCards;
    private boolean costReductionActive; // for DeploymentPipeline
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
        this.latestEventReason = Optional.empty();

        for (ParentPlayer player : players) {
            PlayerCards playerCards = new PlayerCards(boardCards);
            playerCards.refreshHand(); // draw initial hand of 5 cards
            playerCardsMap.put(player, playerCards);
        }

    }

    public GameState getState() {
        return new GameState(
            playerName,
            handObject,
            phase,
            availableActions,
            spendableMoney,
            availableBuys,
            buyableCards
        );
    }

      private void updateState(GameState newState) {
        this.playerName = newState.currentPlayerName();
        this.handObject = newState.currentPlayerHand();
        this.phase = newState.phase();
        this.availableActions = newState.availableActions();
        this.spendableMoney = newState.spendableMoney();
        this.availableBuys = newState.availableBuys();
        this.buyableCards = newState.buyableCards();
    }

    public GameResult play() throws PlayerViolationException {
        
        boolean gameOver = false;
        while (!gameOver) {

            //loop through each player
            for (ParentPlayer player : players) {
                this.playerName = player.getName();
                this.costReductionActive = false; // reset cost reduction each turn
                this.handObject = playerCardsMap.get(player).getHand();
                this.availableActions = 1;
                this.spendableMoney = 0;
                this.availableBuys = 1;
                this.buyableCards = boardCards.getCardsLeft();

                
                //ACTION PHASE
                this.phase = GameState.TurnPhase.ACTION;
                GameState actionState = getState();
                Decision actionDecision = actionDecision(actionState);
                while (!(actionDecision instanceof EndPhaseDecision && ((EndPhaseDecision) actionDecision).phase().equals(GameState.TurnPhase.ACTION))) {
                    actionState = actionPhase(actionState, actionDecision);
                    actionDecision = actionDecision(actionState);
                }

                //MONEY PHASE
                this.phase = GameState.TurnPhase.MONEY;
                GameState moneyState = actionState;
                Decision moneyDecision = moneyDecision(moneyState);
                while (!(moneyDecision instanceof EndPhaseDecision && ((EndPhaseDecision) moneyDecision).phase().equals(GameState.TurnPhase.MONEY))) {
                    moneyState = moneyPhase(moneyState, moneyDecision);
                    moneyDecision = moneyDecision(moneyState);
                }

                //BUY PHASE
                this.phase = GameState.TurnPhase.BUY;
                GameState buyState = moneyState;
                Decision buyDecision = buyDecision(buyState);
                while (!(buyDecision instanceof EndPhaseDecision && ((EndPhaseDecision) buyDecision).phase().equals(GameState.TurnPhase.BUY))) {
                    buyState = buyPhase(buyState, buyDecision);
                    buyDecision = buyDecision(buyState);

                }

                //CLEANUP PHASE
                this.phase = GameState.TurnPhase.CLEANUP;
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
        
        // Sort in descending order by score
        resultsList.sort((a, b) -> Integer.compare(b.score(), a.score()));
        
        ImmutableList<PlayerResult> playerResults = ImmutableList.copyOf(resultsList);
        return new GameResult(playerResults);
    }

   

    private GameState cleanupPhase(ParentPlayer currentPlayer){
        PlayerCards playerCards = playerCardsMap.get(currentPlayer);
        playerCards.refreshHand(); 
        this.handObject = playerCards.getHand();  
        this.availableActions = 1;
        this.spendableMoney = 0;
        this.availableBuys = 1;
        this.buyableCards = boardCards.getCardsLeft();    

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

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        ImmutableCollection<Card> unplayedCards = playerCardsMap.get(currentPlayer).getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();

        // End phase first so console input index 0 can always skip MONEY phase
        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.MONEY));

        for (Card card : unplayedCards) {
            if (getCardCategory(card).equals(Card.Type.Category.MONEY)) {
                optionsBuilder.add(new PlayCardDecision(card));
            }
        }

        ImmutableList<Decision> options = optionsBuilder.build();

        while(true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            } else {
                System.out.println("Invalid decision. Please choose a valid option.");
            }
        }
    }


    private GameState moneyPhase(GameState oldState, Decision decision) {

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        if (decision instanceof PlayCardDecision) {
            Card playedCard = ((PlayCardDecision) decision).card();
            playerCardsMap.get(currentPlayer).playCard(playedCard);
            publishEvent(new PlayCardEvent(playedCard, currentPlayer.getName()));

            this.spendableMoney += playedCard.value();
            this.handObject = playerCardsMap.get(currentPlayer).getHand();
            this.buyableCards = boardCards.getCardsLeft();
        }

        return getState();
    }

    private Decision buyDecision(GameState oldState) {

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        // Create a list of BuyDecision for each card type available
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        CardStacks affordableCards = boardCards.getAffordableCards(this.spendableMoney);
        if (this.availableBuys > 0) {
            for (Card.Type cardType : affordableCards.getCardTypes()) {
                optionsBuilder.add(new BuyDecision(cardType));
            }
        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        ImmutableList<Decision> options = optionsBuilder.build();

        //continue prompting until valid decision is made
        while(true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            } else {
                System.out.println("Invalid decision. Please choose a valid option.");
            }
        }
    }


    private GameState buyPhase(GameState oldState, Decision decision) {

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        Card.Type cardTypeToBuy = null;
        if (decision instanceof BuyDecision) {
            cardTypeToBuy = ((BuyDecision) decision).cardType();
        }
        if (cardTypeToBuy != null) {
            // Gain the card
            Card gainedCard = boardCards.drawDeckCard(cardTypeToBuy);
            playerCardsMap.get(currentPlayer).gainCard(gainedCard);
            publishEvent(new GainCardEvent(cardTypeToBuy, currentPlayer.getName()));

            this.spendableMoney -= gainedCard.cost() - (costReductionActive ? 1 : 0);
            this.availableBuys -= 1;
            this.buyableCards = boardCards.getCardsLeft();
            
        }

        return getState();
    }
    
    
    private Decision actionDecision(GameState oldState){

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        //get all cards from hand and create options list
        ImmutableCollection<Card> unplayedCards = playerCardsMap.get(currentPlayer).getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();

        //determine actionDecisions if actions available
        if (availableActions > 0) {
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

        //add option to end action phase
        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        ImmutableList<Decision> options = optionsBuilder.build();

        //continue prompting until valid decision is made
        while(true) {
            Decision decision = currentPlayer.makeDecision(getState(), options, latestEventReason);
            if (checkDecision(decision, options)) {
                return decision;
            } else {
                System.out.println("Invalid decision. Please choose a valid option.");
            }
        }
    }


    private GameState actionPhase(GameState oldState, Decision decision){

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        this.availableActions--; //action has been played

        Card playedCard = null;
        if (decision instanceof PlayCardDecision) {
            playedCard = ((PlayCardDecision) decision).card();
            playerCardsMap.get(currentPlayer).playCard(playedCard); //move card from hand to play area
            publishEvent(new PlayCardEvent(playedCard, currentPlayer.getName()));
        }
        handObject = playerCardsMap.get(currentPlayer).getHand(); //create new record class hand after playing card

        GameState newState = null;
        if (playedCard != null) {
            newState = ActionCards.playActionCard(playedCard, getState(), currentPlayer, players, playerCardsMap, boardCards);
            if (ActionCards.activatesCostReduction(playedCard)) {
                this.costReductionActive = true; // activate cost reduction for this turn
            }
        }

        if (newState == null) {
            throw new IllegalStateException("Card function not implemented for card: " + playedCard);
        } else{
            updateState(newState);
        }
        
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