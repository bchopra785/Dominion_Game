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
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
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
        assertTrue(playCardDecision.card().type() == Card.Type.BITCOIN || playCardDecision.card().type() == Card.Type.DOGECOIN, "Chosen card should be BITCOIN or DOGECOIN");

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
                "Chosen card should be BITCOIN or DOGECOIN");
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
        assertTrue(playCardDecision.card().type() == Card.Type.IPO);
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
        assertTrue(playCardDecision.card().type() == Card.Type.IPO || playCardDecision.card().type() == Card.Type.CODE_REVIEW);

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
