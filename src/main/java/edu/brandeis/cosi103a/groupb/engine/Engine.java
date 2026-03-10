package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.*;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.*;
import edu.brandeis.cosi.atg.state.*;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.EvergreenTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


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

    public Engine(List<ParentPlayer> players) {
        
        //check for valid number of players
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > 4) {
            throw new IllegalArgumentException("Engine supports at most 4 players");
        }
        this.players = players;

        //initialize board cards and player cards
        this.boardCards = new BoardCards();
        this.playerCardsMap = new HashMap<>();

        Map<String, ParentPlayer> builder = new HashMap<>();

        for (ParentPlayer player : players) {
            PlayerCards playerCards = new PlayerCards(boardCards);
            playerCards.refreshHand(); // draw initial hand of 5 cards
            playerCardsMap.put(player, playerCards);
        }


        //should never see these values (initialized before play() starts)
        // if (!players.isEmpty()) {
        //     ParentPlayer firstPlayer = players.get(0);
        //     this.playerName = firstPlayer.getName();
        //     this.handObject = playerCardsMap.get(firstPlayer).getHand();
        // } else {
        //     this.playerName = "placeholder";
        //     this.handObject = new Hand(ImmutableList.of(), ImmutableList.of());
        // }
        // this.phase = GameState.TurnPhase.ACTION;
        // this.availableActions = -1;
        // this.spendableMoney = -1;
        // this.availableBuys = -1;
        // this.buyableCards = boardCards.getPlayableCards(-1);

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
                this.handObject = playerCardsMap.get(player).getHand();
                this.availableActions = 1;
                this.spendableMoney = playerCardsMap.get(player).getCostInHand();
                this.availableBuys = 1;
                this.buyableCards = boardCards.getPlayableCards(playerCardsMap.get(player).getCostInHand());

                
                //ACTION PHASE
                this.phase = GameState.TurnPhase.ACTION;
                GameState actionState = getState();
                Decision actionDecision = actionDecision(actionState);
                while (!(actionDecision instanceof EndPhaseDecision && ((EndPhaseDecision) actionDecision).phase().equals(GameState.TurnPhase.ACTION))) {
                    actionState = actionPhase(actionState, actionDecision);
                    actionDecision = actionDecision(actionState);
                }

                //BUY PHASE
                this.phase = GameState.TurnPhase.BUY;
                GameState buyState = actionState;
                Decision buyDecision = buyDecision(buyState);
                while (!(buyDecision instanceof EndPhaseDecision && ((EndPhaseDecision) buyDecision).phase().equals(GameState.TurnPhase.BUY))) {
                    buyState = buyPhase(buyState, buyDecision);
                    buyDecision = buyDecision(buyState);

                }

                //CLEANUP PHASE
                this.phase = GameState.TurnPhase.CLEANUP;
                GameState cleanup = cleanupPhase(player);

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
        this.spendableMoney = playerCards.getCostInHand();
        this.availableBuys = 1;
        this.buyableCards = boardCards.getPlayableCards(playerCards.getCostInHand());    

        return getState();
    }

    private Decision buyDecision(GameState oldState) {

        ParentPlayer currentPlayer = getPlayerByName(this.playerName);

        // Create a list of BuyDecision for each card type available
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        if (this.availableBuys > 0) {
            for (Card.Type cardType : this.buyableCards.getCardTypes()) {
                optionsBuilder.add(new BuyDecision(cardType));
            }
        }

        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        ImmutableList<Decision> options = optionsBuilder.build();

        //continue prompting until valid decision is made
        while(true) {
            Decision decision = currentPlayer.makeDecision(getState(), options);
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

            this.spendableMoney -= gainedCard.cost();
            this.availableBuys -= 1;
            this.buyableCards = boardCards.getPlayableCards(this.spendableMoney);
            
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
                    optionsBuilder.add(new PlayCardDecision(card));
                }
            }
        }

        //add option to end action phase
        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        ImmutableList<Decision> options = optionsBuilder.build();

        //continue prompting until valid decision is made
        while(true) {
            Decision decision = currentPlayer.makeDecision(getState(), options);
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
        }
        handObject = playerCardsMap.get(currentPlayer).getHand(); //create new record class hand after playing card

        GameState newState = null;
        if (playedCard != null) {
            if (getCardType(playedCard).equals(Card.Type.CODE_REVIEW)) {
                CodeReview codeReview = new CodeReview();
                newState = codeReview.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer),boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.EVERGREEN_TEST)) {
                EvergreenTest evergreenTest = new EvergreenTest();
                newState = evergreenTest.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.REFACTOR)) {
                Refactor refactor = new Refactor();
                newState = refactor.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
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


    private Card.Type getCardType(Card card) {
        return card.type();
    }


    private Card.Type.Category getCardCategory(Card card) {
           return card.type().category();
    }
}