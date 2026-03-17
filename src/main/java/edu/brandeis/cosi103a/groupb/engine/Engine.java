package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.*;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.*;
import edu.brandeis.cosi.atg.state.*;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Backlog;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.DailyScrum;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.EvergreenTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Hack;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Ipo;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.MergeConflict;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Monitoring;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Parallelization;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Ransomware;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Refactor;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.SprintPlanning;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.TechDebt;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.UnitTest;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.DeploymentPipeline;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Engine(List<ParentPlayer> players) {
        
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
        this.boardCards = new BoardCards(players.size());
        this.playerCardsMap = new HashMap<>();

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
                this.buyableCards = boardCards.getPlayableCards(0);

                
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
        this.buyableCards = boardCards.getPlayableCards(0);    

        return getState();
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
            Decision decision = currentPlayer.makeDecision(getState(), options);
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

            this.spendableMoney += playedCard.value();
            this.handObject = playerCardsMap.get(currentPlayer).getHand();
            this.buyableCards = boardCards.getPlayableCards(this.spendableMoney);
        }

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

            this.spendableMoney -= gainedCard.cost() - (costReductionActive ? 1 : 0);
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
                newState = codeReview.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.EVERGREEN_TEST)) {
                EvergreenTest evergreenTest = new EvergreenTest();
                newState = evergreenTest.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.REFACTOR)) {
                Refactor refactor = new Refactor();
                newState = refactor.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.BACKLOG)) {
                Backlog backlog = new Backlog();
                newState = backlog.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.MONITORING)) {
                Monitoring monitoring = new Monitoring();
                newState = monitoring.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.IPO)) {
                Ipo ipo = new Ipo();
                newState = ipo.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.MERGE_CONFLICT)) {
                MergeConflict mergeConflict = new MergeConflict();
                newState = mergeConflict.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.SPRINT_PLANNING)) {
                SprintPlanning sprintPlanning = new SprintPlanning();
                newState = sprintPlanning.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.TECH_DEBT)) {
                TechDebt techDebt = new TechDebt();
                newState = techDebt.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.UNIT_TEST)) {
                UnitTest unitTest = new UnitTest();
                newState = unitTest.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.HACK)) {
                Hack hack = new Hack();
                newState = hack.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.RANSOMWARE)) {
                Ransomware ransomware = new Ransomware();
                newState = ransomware.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.DAILY_SCRUM)) {
                DailyScrum dailyScrum = new DailyScrum();
                newState = dailyScrum.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.PARALLELIZATION)) {
                Parallelization parallelization = new Parallelization();
                newState = parallelization.play(getState(), currentPlayer, players, playerCardsMap, boardCards);
            } else if (getCardType(playedCard).equals(Card.Type.DEPLOYMENT_PIPELINE)) {
                DeploymentPipeline deploymentPipeline = new DeploymentPipeline();
                newState = deploymentPipeline.play(getState(), currentPlayer, playerCardsMap.get(currentPlayer), boardCards);
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


    private Card.Type getCardType(Card card) {
        return card.type();
    }


    private Card.Type.Category getCardCategory(Card card) {
           return card.type().category();
    }
}