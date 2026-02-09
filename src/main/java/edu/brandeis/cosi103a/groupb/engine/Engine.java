package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.*;
import edu.brandeis.cosi.atg.decisions.*;
import edu.brandeis.cosi.atg.engine.*;
import edu.brandeis.cosi.atg.state.*;
import edu.brandeis.cosi103a.groupb.ConsolePlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.EvergreenTest;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.Refactor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;



public class Engine implements edu.brandeis.cosi.atg.engine.Engine {

    //initialize players and board cards
    private final List<ConsolePlayer> players;
    private final BoardCards boardCards;

    public Engine(List<ConsolePlayer> players) {
        
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

        for (ConsolePlayer player : players) {
            PlayerCards playerCards = new PlayerCards(boardCards);
            player.setPlayerCards(playerCards);
        }

    }

    public GameResult play() throws PlayerViolationException {
    
        boolean gameOver = false;
        while (!gameOver) {

            //loop through each player
            for (ConsolePlayer player : players) {

                //CLEANUP PHASE
                GameState startState = cleanupPhase(player);

                //ACTION PHASE
                GameState actionState = startState;
                Decision actionDecision = actionDecision(startState);
                while (!(actionDecision instanceof EndPhaseDecision && ((EndPhaseDecision) actionDecision).phase().equals(GameState.TurnPhase.ACTION))) {
                    actionState = actionPhase(actionState, actionDecision);
                    actionDecision = actionDecision(actionState);
                }

                //BUY PHASE
                GameState buyState = actionState;
                Decision buyDecision = buyDecision(buyState);
                while (!(buyDecision instanceof EndPhaseDecision && ((EndPhaseDecision) buyDecision).phase().equals(GameState.TurnPhase.BUY))) {
                    buyState = buyPhase(buyState, buyDecision);
                    buyDecision = buyDecision(buyState);
                }

            }   

            gameOver = !boardCards.frameworksLeft(); //placeholder
        }   

        List<PlayerResult> resultsList = new ArrayList<>();
        for (ConsolePlayer player : players) {
            ImmutableCollection<Card> allCards = player.getPlayerCards().getDiscardPile();
            PlayerResult result = new PlayerResult(player.getName(), player.getPlayerCards().getScore(), allCards);
            resultsList.add(result);
        }
        
        // Sort in descending order by score
        resultsList.sort((a, b) -> Integer.compare(b.score(), a.score()));
        
        ImmutableList<PlayerResult> playerResults = ImmutableList.copyOf(resultsList);
        return new GameResult(playerResults); //placeholder
    }

    public GameState cleanupPhase(ConsolePlayer currentPlayer){
        currentPlayer.getPlayerCards().refreshHand();       

        GameState newState = new GameState(
                currentPlayer.getName(),
                currentPlayer.getPlayerCards().getHand(),
                GameState.TurnPhase.CLEANUP,
                1,
                currentPlayer.getPlayerCards().getCostInHand(),
                1,
                boardCards.getPlayableCards(currentPlayer.getPlayerCards().getCostInHand()) //buyable cards
            );

        return newState;
    }

    public Decision buyDecision(GameState oldState) {
        String playerName = oldState.currentPlayerName();
        Hand handObject = oldState.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int actionAmt = oldState.availableActions();
        int totalMoney = oldState.spendableMoney();
        int availableBuys = oldState.availableBuys();
        CardStacks buyableCards = oldState.buyableCards();

        ConsolePlayer currentPlayer = getPlayerByName(playerName);
        ImmutableCollection<Card> hand = handObject.getAllCards();

        // Create a list of BuyDecision for each card type available
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();
        if (availableBuys > 0) {
            for (Card.Type cardType : buyableCards.getCardTypes()) {
                optionsBuilder.add(new BuyDecision(cardType));
            }
        }

        
        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.BUY));
        ImmutableList<Decision> options = optionsBuilder.build();

        GameState newState = new GameState(
            playerName,
            handObject,
            GameState.TurnPhase.BUY,
            actionAmt,
            totalMoney,
            availableBuys,
            buyableCards
        );

        return currentPlayer.makeDecision(newState, options);
    }

    public GameState buyPhase(GameState oldState, Decision decision) {
        String playerName = oldState.currentPlayerName();
        Hand handObject = oldState.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int actionAmt = oldState.availableActions();
        int totalMoney = oldState.spendableMoney();
        int availableBuys = oldState.availableBuys();
        CardStacks buyableCards = oldState.buyableCards();

        ConsolePlayer currentPlayer = getPlayerByName(playerName);
        ImmutableCollection<Card> hand = handObject.getAllCards();

        Card.Type cardTypeToBuy = null;
        if (decision instanceof BuyDecision) {
            cardTypeToBuy = ((BuyDecision) decision).cardType();
        }
        if (cardTypeToBuy != null) {
            // Gain the card
            Card gainedCard = boardCards.drawDeckCard(cardTypeToBuy);
            currentPlayer.getPlayerCards().gainCard(gainedCard);

            totalMoney -= gainedCard.cost();
            availableBuys -= 1;
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
    
    
    public Decision actionDecision(GameState oldState){

        String playerName = oldState.currentPlayerName();
        Hand handObject = oldState.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = oldState.availableActions();
        int totalMoney = oldState.spendableMoney();
        int availableBuys = oldState.availableBuys();
        CardStacks buyableCards = oldState.buyableCards();

        ConsolePlayer currentPlayer = getPlayerByName(playerName);

        //get all cards from hand and create options list
        ImmutableCollection<Card> unplayedCards = currentPlayer.getPlayerCards().getUnplayedCards();
        ImmutableList.Builder<Decision> optionsBuilder = new ImmutableList.Builder<>();

        //determine actionDecisions if actions available
        if (actionAmt > 0) {
            for (Card card : unplayedCards) {
                Card.Type.Category category = getCardCategory(card);
                if (category.equals(Card.Type.Category.ACTION)) {
                    optionsBuilder.add(new PlayCardDecision(card));
                }
            }
        }

        //add option to end action phase
        optionsBuilder.add(new EndPhaseDecision(GameState.TurnPhase.ACTION));

        GameState newState = new GameState(
            playerName,
            handObject,
            phase,
            actionAmt,
            totalMoney,
            availableBuys,
            buyableCards
        );

        ImmutableList<Decision> options = optionsBuilder.build();
        return currentPlayer.makeDecision(newState, options);
    }


    public GameState actionPhase(GameState oldState, Decision decision){
        String playerName = oldState.currentPlayerName();
        Hand handObject = oldState.currentPlayerHand();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int actionAmt = oldState.availableActions();
        int totalMoney = oldState.spendableMoney();
        int availableBuys = oldState.availableBuys();
        CardStacks buyableCards = oldState.buyableCards();

        ConsolePlayer currentPlayer = getPlayerByName(playerName);

        actionAmt--; //action has been played

        Card playedCard = null;
        if (decision instanceof PlayCardDecision) {
            playedCard = ((PlayCardDecision) decision).card();
            currentPlayer.getPlayerCards().playCard(playedCard); //move card from hand to play area
        }
        handObject = currentPlayer.getPlayerCards().getHand(); //create new record class hand after playing card

        GameState state = new GameState(
                playerName,
                handObject,
                phase,
                actionAmt,
                totalMoney,
                availableBuys,
                buyableCards
        );

        GameState newState = null;
        if (playedCard != null) {
            if (getCardType(playedCard).equals(Card.Type.CODE_REVIEW)) {
                CodeReview codeReview = new CodeReview();
                newState = codeReview.play(state, currentPlayer, boardCards);

            } else if (getCardType(playedCard).equals(Card.Type.EVERGREEN_TEST)) {
                EvergreenTest evergreenTest = new EvergreenTest();
                newState = evergreenTest.play(state, currentPlayer, players, boardCards);
                
            } else if (getCardType(playedCard).equals(Card.Type.REFACTOR)) {
                Refactor refactor = new Refactor();
                newState = refactor.play(state, currentPlayer, boardCards);
                
            }
        }

        if (newState == null) {
            throw new IllegalStateException("Card function not implemented for card: " + playedCard);
        }
        
        return newState;
    }


    private ConsolePlayer getPlayerByName(String playerName) {
        for (ConsolePlayer player : players) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        throw new IllegalStateException("Current player not found: " + playerName);
    }


    public Card.Type getCardType(Card card) {
        return card.type();
    }


    public Card.Type.Category getCardCategory(Card card) {
           return card.type().category();
    }


    public static void main(String[] args) {
        Card card = new Card(Card.Type.BUG, 1);
        System.out.println(card + " " + card.value() + " " + card.cost());

        Card card1 = new Card(Card.Type.METHOD, 1);
        System.out.println(card1 + " " + card1.value() + " " + card1.cost());

        Card card2 = new Card(Card.Type.MODULE, 1);
        System.out.println(card2 + " " + card2.value() + " " + card2.cost());

        Card card3 = new Card(Card.Type.FRAMEWORK, 1);
        System.out.println(card3 + " " + card3.value() + " " + card3.cost());

        Card card4 = new Card(Card.Type.BITCOIN, 1);
        System.out.println(card4 + " " + card4.value() + " " + card4.cost());

        Card card5 = new Card(Card.Type.ETHEREUM, 1);
        System.out.println(card5 + " " + card5.value() + " " +card5.cost());

        Card card6 = new Card(Card.Type.DOGECOIN, 1);
        System.out.println(card6 + " " + card6.value() + " " + card6.cost());

        Card card7 = new Card(Card.Type.REFACTOR , 1);
        System.out.println(card7 + " " + card7.value() + " " + card7.cost());
        
        Card card8 = new Card(Card.Type.CODE_REVIEW , 1);
        System.out.println(card8 + " " + card8.value() + " " + card8.cost());

        Card card9 = new Card(Card.Type.EVERGREEN_TEST , 1);
        System.out.println(card9 + " " + card9.value() + " " + card9.cost());

        //TO DO: instantiate players
        ConsolePlayer player1 = new ConsolePlayer();
        ConsolePlayer player2 = new ConsolePlayer();
        ConsolePlayer player3 = new ConsolePlayer();
        ConsolePlayer player4 = new ConsolePlayer();

        List<ConsolePlayer> players = List.of(player1, player2, player3, player4);
        
        Engine engine = new Engine(players);
        GameResult result = null;
        try {
            result = engine.play();
        } catch (PlayerViolationException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }     
    
}
