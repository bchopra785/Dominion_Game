package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.GameState.TurnPhase;
import edu.brandeis.cosi.atg.state.Hand;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class V2StrategyPlayerTest {
    //deck initializes with 7 bitcoin and 3 method cards
    //ChooseMoney picks money cards
    //ChooseAction picks the card with the highest weight (based on cards returned)
    
    

    @Test
    public void testDeckInitialization(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");
        List<Card.Type> deck = player.deck;

        assertEquals(10, deck.size(), "Deck should have 10 cards");

        long bitcoinCount = player.deck.stream()
            .filter(card -> card == Card.Type.BITCOIN)
            .count();
        assertEquals(7, bitcoinCount, "Deck should have 7 BITCOIN cards");
    
        long methodCount = player.deck.stream()
            .filter(card -> card == Card.Type.METHOD)
            .count();
        assertEquals(3, methodCount, "Deck should have 3 METHOD cards");
    }

    @Test
    public void testChooseMoney(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.MONEY;
        int availableActions = 0;
        int spendableMoney = 0;
        int availableBuys = 0;
        CardStacks buyableCards = createBuyableCards();

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseMoneyDecision(gameState, createPlayCardOptionsMoney(currentPlayerHand));
        assertTrue(chosen instanceof PlayCardDecision, "Chosen decision should be a PlayCardDecision");
        PlayCardDecision playCardDecision = (PlayCardDecision) chosen;
        assertTrue(playCardDecision.card().type() == Card.Type.BITCOIN || playCardDecision.card().type() == Card.Type.DOGECOIN, "Chosen card should be BITCOIN or DOGECOIN, but was: " + playCardDecision.card().type());

    }

    @Test
    public void testChooseMoneyProperty(){
        for(int i = 0; i < 100; i++){
            V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

            String playerName = "TestPlayer";
            Hand currentPlayerHand = randomHand();
            GameState.TurnPhase phase = GameState.TurnPhase.MONEY;
            int availableActions = 0;
            int spendableMoney = 0;
            int availableBuys = 0;
            CardStacks buyableCards = createBuyableCards();

            GameState gameState = new GameState(playerName, 
                currentPlayerHand, 
                phase, availableActions, 
                spendableMoney, availableBuys, 
                buyableCards);

            
            Decision chosen = player.chooseMoneyDecision(gameState, createPlayCardOptionsMoney(currentPlayerHand));
                assertTrue(chosen instanceof PlayCardDecision || chosen instanceof EndPhaseDecision, 
           "Decision should be either PlayCardDecision or EndPhaseDecision");
            if(chosen instanceof PlayCardDecision){
                PlayCardDecision playCardDecision = (PlayCardDecision) chosen;
                assertTrue(playCardDecision.card().type() == Card.Type.BITCOIN 
                || playCardDecision.card().type() == Card.Type.DOGECOIN
                || playCardDecision.card().type() == Card.Type.ETHEREUM, 
                "Chosen card should be BITCOIN, DOGECOIN, or ETHEREUM, but was: " + playCardDecision.card().type());
            }
        }
    }

    @Test
    public void testChooseAction(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHandAction();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int availableActions = 1;
        int spendableMoney = 0;
        int availableBuys = 0;
        CardStacks buyableCards = createBuyableCards();

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        // IPO, EVERGREEN -> +2 cards, +2 Money, +2 cards = 4x2 + 2x3 = 14
        // EVERGREEN, (NO MORE ACTIONS) -> +2 cards = 2X2 = 4

        
        Decision chosen = player.chooseActionDecision(gameState, createPlayCardOptionsActions(currentPlayerHand));
        assertTrue(chosen instanceof PlayCardDecision, "Chosen decision should be a PlayCardDecision");
        PlayCardDecision playCardDecision = (PlayCardDecision) chosen;
        assertTrue(playCardDecision.card().type() == Card.Type.IPO, "Chosen card should be IPO, but was: " + playCardDecision.card().type());
    }

    @Test
    public void testChooseAction2(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHandAction2();
        GameState.TurnPhase phase = GameState.TurnPhase.ACTION;
        int availableActions = 1;
        int spendableMoney = 0;
        int availableBuys = 0;
        CardStacks buyableCards = createBuyableCards();

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        // IPO, CODEREVIEW, MONITORING -> +2 Money, +5 Cards, +1 Action = 2X3 + 5X2 + 1X1 = 17
        // IPO, MONITORING, NA -> +2 Money, +4 Cards, +0 Action = 2X3 + 4X2 + 0X1 = 14
        // CODEREVIEW, IPO, MONITORING -> 5 Card, 2 Actions, 2 Money = 17
        // any start to monitoring is bad

        
        Decision chosen = player.chooseActionDecision(gameState, createPlayCardOptionsActions(currentPlayerHand));
        assertTrue(chosen instanceof PlayCardDecision, "Chosen decision should be a PlayCardDecision");
        PlayCardDecision playCardDecision = (PlayCardDecision) chosen;
        assertTrue(playCardDecision.card().type() == Card.Type.IPO || playCardDecision.card().type() == Card.Type.CODE_REVIEW, "Chosen card should be IPO or CODE_REVIEW, but was: " + playCardDecision.card().type());

    }

    @Test
    public void testChooseBuyFramework(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 10; //should be able to buy framework
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.FRAMEWORK, "Chosen card should be FRAMEWORK, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyPoints2(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.BITCOIN, 
            Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.IPO, Card.Type.IPO, 
            Card.Type.IPO, Card.Type.IPO));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.MODULE, "Chosen card should be MODULE, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyPoints3(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 2;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.BITCOIN, 
            Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.IPO, Card.Type.IPO, 
            Card.Type.IPO, Card.Type.IPO));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.METHOD, "Chosen card should be METHOD, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyMoney1(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, 
            Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, Card.Type.IPO, Card.Type.IPO, 
            Card.Type.IPO, Card.Type.IPO));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.DOGECOIN, "Chosen card should be DOGECOIN, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyMoney2(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 5;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, 
            Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, Card.Type.IPO, Card.Type.IPO, 
            Card.Type.IPO, Card.Type.IPO));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.ETHEREUM, "Chosen card should be ETHEREUM, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyMoney3(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 2;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, 
            Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, Card.Type.IPO, Card.Type.IPO, 
            Card.Type.IPO, Card.Type.IPO));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.BITCOIN, "Chosen card should be BITCOIN, but was: " + chosenCardType);
    }


    @Test
    public void testChooseBuyTier1Action(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7; //should be able to buy IPO
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.METHOD, Card.Type.METHOD)); //should prioritize actions

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.IPO || chosenCardType == Card.Type.EVERGREEN_TEST || chosenCardType == Card.Type.MONITORING, "Chosen card should be IPO, EVERGREEN_TEST, or MONITORING, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyTier2Action(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7; //should be able to buy Tier 2
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.METHOD, Card.Type.METHOD)); //should prioritize actions

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCardsTier2(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.CODE_REVIEW || chosenCardType == Card.Type.RANSOMWARE || 
            chosenCardType == Card.Type.REFACTOR || chosenCardType == Card.Type.PARALLELIZATION, 
            "Chosen card should be CODE_REVIEW, RANSOMWARE, PARALLELIZATION or REFACTOR, but was: " + chosenCardType);
    }


    @Test
    public void testChooseBuyTier3Action(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7; //should be able to buy Tier 3
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.BITCOIN, Card.Type.BITCOIN, Card.Type.METHOD, Card.Type.METHOD)); //should prioritize actions

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCardsTier3(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.SPRINT_PLANNING || chosenCardType == Card.Type.HACK || 
            chosenCardType == Card.Type.BACKLOG || chosenCardType == Card.Type.DAILY_SCRUM || chosenCardType == Card.Type.UNIT_TEST,
            "Chosen card should be SPRINT_PLANNING, HACK, BACKLOG, DAILY_SCRUM or UNIT_TEST, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyPriorityFallBack(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        player.deck = new ArrayList<>(Arrays.asList(Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE, //should fall back to money
            Card.Type.MODULE, Card.Type.BITCOIN, Card.Type.BITCOIN));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getFallbackDecisions(spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.DOGECOIN, "Chosen card should be DOGECOIN, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyPriorityFallBack2(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = 7;
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        //should fall back to money then points
        player.deck = new ArrayList<>(Arrays.asList(Card.Type.MODULE, Card.Type.MODULE, Card.Type.MODULE,
            Card.Type.MODULE, Card.Type.BITCOIN, Card.Type.BITCOIN));

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getFallbackDecisions2 (spendableMoney));
        assertTrue(chosen instanceof BuyDecision, "Chosen decision should be a BuyDecision");
        Card.Type chosenCardType = ((BuyDecision) chosen).cardType();
        assertTrue(chosenCardType == Card.Type.MODULE, "Chosen card should be MODULE, but was: " + chosenCardType);
    }

    @Test
    public void testChooseBuyNoAffordableCards(){
        V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");

        String playerName = "TestPlayer";
        Hand currentPlayerHand = createTestHand();
        GameState.TurnPhase phase = GameState.TurnPhase.BUY;
        int availableActions = 0;
        int spendableMoney = -1; // can't afford anything
        int availableBuys = 1;
        CardStacks buyableCards = createBuyableCards();

        GameState gameState = new GameState(playerName, 
            currentPlayerHand, 
            phase, availableActions, 
            spendableMoney, availableBuys, 
            buyableCards);

        
        Decision chosen = player.chooseBuyDecision(gameState, getAffordableCards(spendableMoney));
        assertTrue(chosen instanceof EndPhaseDecision, "Chosen decision should be an EndPhaseDecision when no affordable cards");
    }

    @Test
    public void testTrashCardDecision(){
        for(int i = 0; i < 50; i++){
            V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");
            Hand currentPlayerHand = randomHand();

            List<Card.Type> handCardTypes = new ArrayList<>();
            for (Card card : currentPlayerHand.unplayedCards()) {
                handCardTypes.add(card.type());
            }

            
            Decision chosen = player.chooseTrashDecision(createTrashOptions(currentPlayerHand));
            if(handCardTypes.contains(Card.Type.BUG)){
                assertTrue(chosen instanceof TrashCardDecision, "Chosen decision should be a TrashCardDecision when BUG is in hand");
                TrashCardDecision trashCardDecision = (TrashCardDecision) chosen;
                assertEquals(Card.Type.BUG, trashCardDecision.card().type(), "Chosen card to trash should be BUG when it is in hand");
            } else {
                assertTrue(chosen instanceof TrashCardDecision, "Chosen decision should be an TrashCardDecision when no BUG in hand");
            }
        }
    }

    @Test
    public void testDiscardCardDecision(){
        for(int i = 0; i < 50; i++){
            V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");
            Hand currentPlayerHand = randomHand();

            List<Card.Type> handCardTypes = new ArrayList<>();
            for (Card card : currentPlayerHand.unplayedCards()) {
                handCardTypes.add(card.type());
            }

            //version with endPhase
            Decision chosen = player.chooseDiscardDecision(createDiscardOptions(currentPlayerHand));
            if(handCardTypes.contains(Card.Type.BUG)){
                assertTrue(chosen instanceof DiscardCardDecision, "Chosen decision should be a DiscardCardDecision when BUG is in hand");
                DiscardCardDecision discardCardDecision = (DiscardCardDecision) chosen;
                assertEquals(Card.Type.BUG, discardCardDecision.card().type(), "Chosen card to discard should be BUG when it is in hand");
            } else if (handCardTypes.contains(Card.Type.METHOD) || handCardTypes.contains(Card.Type.MODULE) || handCardTypes.contains(Card.Type.FRAMEWORK)){
                assertTrue(chosen instanceof DiscardCardDecision, "Chosen decision should be a DiscardCardDecision when METHOD, MODULE, or FRAMEWORK is in hand");
                DiscardCardDecision discardCardDecision = (DiscardCardDecision) chosen;
                assertTrue(discardCardDecision.card().type() == Card.Type.METHOD || discardCardDecision.card().type() == Card.Type.MODULE || discardCardDecision.card().type() == Card.Type.FRAMEWORK);
            } else {
                assertTrue(chosen instanceof EndPhaseDecision, "Chosen decision should be an EndPhaseDecision when no BUG, METHOD, MODULE, or FRAMEWORK in hand");
            }
        }
    }

    @Test
    public void testDiscardCardDecision2(){
        for(int i = 0; i < 50; i++){
            V2StrategyPlayer player = new V2StrategyPlayer("TestPlayer");
            Hand currentPlayerHand = randomHand();

            List<Card.Type> handCardTypes = new ArrayList<>();
            for (Card card : currentPlayerHand.unplayedCards()) {
                handCardTypes.add(card.type());
            }

            //version without endPhase
            Decision chosen = player.chooseDiscardDecision(createDiscardOptions2(currentPlayerHand));
            if(handCardTypes.contains(Card.Type.BUG)){
                assertTrue(chosen instanceof DiscardCardDecision, "Chosen decision should be a DiscardCardDecision when BUG is in hand");
                DiscardCardDecision discardCardDecision = (DiscardCardDecision) chosen;
                assertEquals(Card.Type.BUG, discardCardDecision.card().type(), "Chosen card to discard should be BUG when it is in hand");
            } else if (handCardTypes.contains(Card.Type.METHOD) || handCardTypes.contains(Card.Type.MODULE) || handCardTypes.contains(Card.Type.FRAMEWORK)){
                assertTrue(chosen instanceof DiscardCardDecision, "Chosen decision should be a DiscardCardDecision when METHOD, MODULE, or FRAMEWORK is in hand");
                DiscardCardDecision discardCardDecision = (DiscardCardDecision) chosen;
                assertTrue(discardCardDecision.card().type() == Card.Type.METHOD || discardCardDecision.card().type() == Card.Type.MODULE || discardCardDecision.card().type() == Card.Type.FRAMEWORK);
            } else {
                assertTrue(chosen instanceof DiscardCardDecision, "Chosen decision should be a DiscardCardDecision when no BUG, METHOD, MODULE, or FRAMEWORK in hand");
            }
        }
    }


    // ---------------------------------------------------------------------------------------
    //                                     HELPER METHODS 
    // ---------------------------------------------------------------------------------------
    private ImmutableList<Decision> createDiscardOptions2(Hand hand) {
        List<Decision> options = new ArrayList<>();

        for (Card card : hand.unplayedCards()) {

                options.add(new DiscardCardDecision(card));
            
        }
        
        return ImmutableList.copyOf(options);
    }

    private ImmutableList<Decision> createDiscardOptions(Hand hand) {
        List<Decision> options = new ArrayList<>();

        for (Card card : hand.unplayedCards()) {

                options.add(new DiscardCardDecision(card));
            
        }
        options.add(new EndPhaseDecision(TurnPhase.ACTION)); // Always include option to end phase
        
        return ImmutableList.copyOf(options);
    }

    private ImmutableList<Decision> createTrashOptions(Hand hand) {
        List<Decision> options = new ArrayList<>();

        for (Card card : hand.unplayedCards()) {

                options.add(new TrashCardDecision(card));
            
        }
        options.add(new EndPhaseDecision(TurnPhase.ACTION)); // Always include option to end phase
        
        return ImmutableList.copyOf(options);
    }
    
    
    public static ImmutableList<Decision> getFallbackDecisions2 (int totalMoney) {
        List<Decision> affordableCards = new ArrayList<>();
        
        // Define all available card types (money, victory, action, and basic cards)
        Card.Type[] allCards = {
            // Money Cards
            // Victory Cards
            Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
            // Basic Card
            Card.Type.BUG,
            // Action Cards
        };
        
        // Filter cards that can be afforded and create BuyDecision objects
        for (Card.Type cardType : allCards) {
            if (cardType.cost() <= totalMoney) {
                affordableCards.add(new BuyDecision(cardType));
            }
        }
        
        affordableCards.add(new EndPhaseDecision(TurnPhase.BUY));
        return ImmutableList.copyOf(affordableCards);
    }
    
    
    public static ImmutableList<Decision> getFallbackDecisions(int totalMoney) {
        List<Decision> affordableCards = new ArrayList<>();
        
        // Define all available card types (money, victory, action, and basic cards)
        Card.Type[] allCards = {
            // Money Cards
            Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN,
            // Victory Cards
            Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
            // Basic Card
            Card.Type.BUG,
            // Action Cards
        };
        
        // Filter cards that can be afforded and create BuyDecision objects
        for (Card.Type cardType : allCards) {
            if (cardType.cost() <= totalMoney) {
                affordableCards.add(new BuyDecision(cardType));
            }
        }
        
        affordableCards.add(new EndPhaseDecision(TurnPhase.BUY));
        return ImmutableList.copyOf(affordableCards);
    }

    public static ImmutableList<Decision> getAffordableCardsTier3(int totalMoney) {
        List<Decision> affordableCards = new ArrayList<>();
        
        // Define all available card types (money, victory, action, and basic cards)
        Card.Type[] allCards = {
            // Money Cards
            Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN,
            // Victory Cards
            Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
            // Basic Card
            Card.Type.BUG,
            // Action Cards
            Card.Type.SPRINT_PLANNING,
            Card.Type.DEPLOYMENT_PIPELINE, Card.Type.TECH_DEBT, Card.Type.UNIT_TEST, Card.Type.HACK, Card.Type.MERGE_CONFLICT,
            Card.Type.BACKLOG, Card.Type.DAILY_SCRUM
        };
        
        // Filter cards that can be afforded and create BuyDecision objects
        for (Card.Type cardType : allCards) {
            if (cardType.cost() <= totalMoney) {
                affordableCards.add(new BuyDecision(cardType));
            }
        }
        
        affordableCards.add(new EndPhaseDecision(TurnPhase.BUY));
        return ImmutableList.copyOf(affordableCards);
    }



    public static ImmutableList<Decision> getAffordableCardsTier2(int totalMoney) {
        List<Decision> affordableCards = new ArrayList<>();
        
        // Define all available card types (money, victory, action, and basic cards)
        Card.Type[] allCards = {
            // Money Cards
            Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN,
            // Victory Cards
            Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
            // Basic Card
            Card.Type.BUG,
            // Action Cards
            Card.Type.CODE_REVIEW, Card.Type.SPRINT_PLANNING,
            Card.Type.DEPLOYMENT_PIPELINE, Card.Type.TECH_DEBT, Card.Type.UNIT_TEST, Card.Type.HACK, Card.Type.RANSOMWARE, Card.Type.MERGE_CONFLICT, Card.Type.REFACTOR,
            Card.Type.BACKLOG, Card.Type.DAILY_SCRUM, Card.Type.PARALLELIZATION
        };
        
        // Filter cards that can be afforded and create BuyDecision objects
        for (Card.Type cardType : allCards) {
            if (cardType.cost() <= totalMoney) {
                affordableCards.add(new BuyDecision(cardType));
            }
        }
        
        affordableCards.add(new EndPhaseDecision(TurnPhase.BUY));
        return ImmutableList.copyOf(affordableCards);
    }


    public static ImmutableList<Decision> getAffordableCards(int totalMoney) {
        List<Decision> affordableCards = new ArrayList<>();
        
        // Define all available card types (money, victory, action, and basic cards)
        Card.Type[] allCards = {
            // Money Cards
            Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN,
            // Victory Cards
            Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK,
            // Basic Card
            Card.Type.BUG,
            // Action Cards
            Card.Type.CODE_REVIEW, Card.Type.IPO, Card.Type.SPRINT_PLANNING,
            Card.Type.DEPLOYMENT_PIPELINE, Card.Type.TECH_DEBT, Card.Type.UNIT_TEST,
            Card.Type.EVERGREEN_TEST, Card.Type.HACK, Card.Type.RANSOMWARE,
            Card.Type.MONITORING, Card.Type.MERGE_CONFLICT, Card.Type.REFACTOR,
            Card.Type.BACKLOG, Card.Type.DAILY_SCRUM, Card.Type.PARALLELIZATION
        };
        
        // Filter cards that can be afforded and create BuyDecision objects
        for (Card.Type cardType : allCards) {
            if (cardType.cost() <= totalMoney) {
                affordableCards.add(new BuyDecision(cardType));
            }
        }
        
        affordableCards.add(new EndPhaseDecision(TurnPhase.BUY));
        return ImmutableList.copyOf(affordableCards);
    }



    /**
     * Helper method to create a list of PlayCardDecision options with BITCOIN, METHOD, and DOGECOIN cards
     */
    private ImmutableList<Decision> createPlayCardOptionsMoney(Hand hand) {
        List<Card.Type> moneyCardTypes = CardInfo.getMoneyCards();
        List<Decision> options = new ArrayList<>();

        for (Card card : hand.unplayedCards()) {
            if(moneyCardTypes.contains(card.type())){
                options.add(new PlayCardDecision(card));
            }
        }
        options.add(new EndPhaseDecision(TurnPhase.MONEY)); // Always include option to end phase
        
        return ImmutableList.copyOf(options);
    }

    private ImmutableList<Decision> createPlayCardOptionsActions(Hand hand) {
        List<Card.Type> actionCardTypes = CardInfo.getActionCards();
        List<Decision> options = new ArrayList<>();

        for (Card card : hand.unplayedCards()) {
            if(actionCardTypes.contains(card.type())){
                options.add(new PlayCardDecision(card));
            }
        }

        options.add(new EndPhaseDecision(TurnPhase.ACTION));
        
        return ImmutableList.copyOf(options);
    }
    
    /**
     * Helper method to create fake buyable cards for testing BUY phase
     */
    private CardStacks createBuyableCards() {
        // CardStacks is created from a Map of Card.Type to Integer counts
        ImmutableMap.Builder<Card.Type, Integer> builder = ImmutableMap.builder();
        
        builder.put(Card.Type.DAILY_SCRUM, 5);     // Card draw
        builder.put(Card.Type.IPO, 4);              // Bridging card
        builder.put(Card.Type.FRAMEWORK, 8);        // Victory points
        builder.put(Card.Type.DOGECOIN, 5);         // Money card
        builder.put(Card.Type.ETHEREUM, 5);         // Money card
        builder.put(Card.Type.BITCOIN, 5);          // Money card
        
        return new CardStacks(builder.build());
    }

    /**
     * Helper method to create a Hand with 1 METHOD, 1 DOGECOIN, and 1 BITCOIN card
     */
    private Hand createTestHand() {
        List<Card> unplayedCards = new ArrayList<>();
        unplayedCards.add(new Card(Card.Type.METHOD, 0));
        unplayedCards.add(new Card(Card.Type.DOGECOIN, 0));
        unplayedCards.add(new Card(Card.Type.BITCOIN, 0));
        
        List<Card> playedCards = new ArrayList<>();
        
        return new Hand(ImmutableList.copyOf(playedCards), ImmutableList.copyOf(unplayedCards));
    }

    private Hand createTestHandAction() {
        List<Card> unplayedCards = new ArrayList<>();
        unplayedCards.add(new Card(Card.Type.IPO, 0));
        unplayedCards.add(new Card(Card.Type.EVERGREEN_TEST, 0));
        unplayedCards.add(new Card(Card.Type.BITCOIN, 0));
        
        List<Card> playedCards = new ArrayList<>();
        
        return new Hand(ImmutableList.copyOf(playedCards), ImmutableList.copyOf(unplayedCards));
    }

    private Hand createTestHandAction2() {
        List<Card> unplayedCards = new ArrayList<>();
        unplayedCards.add(new Card(Card.Type.IPO, 0));
        unplayedCards.add(new Card(Card.Type.CODE_REVIEW, 0));
        unplayedCards.add(new Card(Card.Type.MONITORING, 0));
        
        List<Card> playedCards = new ArrayList<>();
        
        return new Hand(ImmutableList.copyOf(playedCards), ImmutableList.copyOf(unplayedCards));
    }
    
    /**
     * Helper method to create a random Hand with 5 cards from various categories.
     * Uses card reference from CARD_REFERENCE.txt
     */
    private Hand randomHand() {
        
        // Collect all card types
        List<Card.Type> allCardTypes = new ArrayList<>();
        allCardTypes.add(Card.Type.BITCOIN);
        allCardTypes.add(Card.Type.ETHEREUM);
        allCardTypes.add(Card.Type.DOGECOIN);

        allCardTypes.add(Card.Type.BUG);
        allCardTypes.add(Card.Type.METHOD);
        allCardTypes.add(Card.Type.MODULE);
        allCardTypes.add(Card.Type.FRAMEWORK);

        allCardTypes.add(Card.Type.BACKLOG);
        allCardTypes.add(Card.Type.CODE_REVIEW);
        allCardTypes.add(Card.Type.SPRINT_PLANNING);
        allCardTypes.add(Card.Type.DEPLOYMENT_PIPELINE);
        allCardTypes.add(Card.Type.TECH_DEBT);
        allCardTypes.add(Card.Type.UNIT_TEST);
        allCardTypes.add(Card.Type.DAILY_SCRUM);
        allCardTypes.add(Card.Type.PARALLELIZATION);

        allCardTypes.add(Card.Type.IPO);
        allCardTypes.add(Card.Type.MONITORING);
        allCardTypes.add(Card.Type.MERGE_CONFLICT);
        allCardTypes.add(Card.Type.REFACTOR);
        allCardTypes.add(Card.Type.EVERGREEN_TEST);
        allCardTypes.add(Card.Type.HACK);
        allCardTypes.add(Card.Type.RANSOMWARE);
        
        // Randomly select 5 cards
        Random random = new Random();
        List<Card> unplayedCards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Card.Type randomCardType = allCardTypes.get(random.nextInt(allCardTypes.size()));
            unplayedCards.add(new Card(randomCardType, i));
        }
        
        List<Card> playedCards = new ArrayList<>();
        
        return new Hand(ImmutableList.copyOf(playedCards), ImmutableList.copyOf(unplayedCards));
    }
}
