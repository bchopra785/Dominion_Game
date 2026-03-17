package edu.brandeis.cosi103a.groupb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.CodeReview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardTest {
    
    @Mock
    private PlayerCards mockPlayerCards;

    private PlayerCards createMockPlayerCards(){
        PlayerCards mockPlayerCards = mock(PlayerCards.class);
        
        // Track the current hand state
        List<Card> unplayedCards = new ArrayList<>();
        List<Card> playedCards = new ArrayList<>();
        
        // Start with 5 bitcoins
        for (int i = 0; i < 5; i++) {
            unplayedCards.add(new Card(Card.Type.BITCOIN, 1));
        }
        
        // When drawToHand is called, add a new bitcoin card
        when(mockPlayerCards.drawToHand()).thenAnswer(invocation -> {
            unplayedCards.add(new Card(Card.Type.BITCOIN, 1));
            return true;
        });
        
        // When getHand is called, return a Hand with current state
        when(mockPlayerCards.getHand()).thenAnswer(invocation -> {
            return new Hand(
                ImmutableList.copyOf(playedCards),
                ImmutableList.copyOf(unplayedCards)
            );
        });
        
        // Stub getCostInHand to return the number of bitcoin cards
        when(mockPlayerCards.getCostInHand()).thenAnswer(invocation -> {
            int cost = 0;
            for (Card card : unplayedCards) {
                if (card.type() == Card.Type.BITCOIN) {
                    cost += card.value();
                }
            }
            return cost;
        });

        return mockPlayerCards;
    }
    
    //unit test
    @Test
    public void testCodeReviewUnit(){

        ConsolePlayer player = null;
        BoardCards boardCards = new BoardCards();
        PlayerCards playerCards = createMockPlayerCards();
        Hand handObject = playerCards.getHand();    //create new record class hand

        GameState state = new GameState(
            "Player1",
            handObject,
            GameState.TurnPhase.ACTION,
            1,
            5,
            1,
            boardCards.getPlayableCards(5)
        );
        
        
        CodeReview codeReview = new CodeReview();
        GameState newState = codeReview.play(state, player, playerCards, boardCards);
        assertEquals(newState.currentPlayerName(), "Player1");
        assertEquals(newState.availableActions(), 3); // +2 actions from code review

    }

    @Test
    public void testCodeReviewIntegration(){
        BoardCards boardCards = new BoardCards();
        PlayerCards playerCards = new PlayerCards(boardCards);
        CodeReview codeReview = new CodeReview();
        Scanner scanner = new Scanner(System.in);
        ConsolePlayer player = new ConsolePlayer(scanner, System.out);
        GameState state = new GameState(
            "Player1",
            playerCards.getHand(),
            GameState.TurnPhase.ACTION,
            1,
            5,
            1,
            boardCards.getPlayableCards(5)
        );
        GameState newstate = codeReview.play(state, player, playerCards, boardCards);
        assertEquals(newstate.currentPlayerName(), "Player1");
        assertEquals(newstate.availableActions(), 3); // +2 actions from code review
        //assertTrue(newstate.spendableMoney() >= 5); // should still have 5 bitcoin in hand

        //need to figure out how to test what cards are in the hand


    }
    
 }
