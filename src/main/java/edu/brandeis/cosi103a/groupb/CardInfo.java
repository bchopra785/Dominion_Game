package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.brandeis.cosi.atg.cards.Card;

public class CardInfo {
    
    public static List<Card.Type> getActionCards(){

        List<Card.Type> actionCards = Arrays.asList(Card.Type.CODE_REVIEW, Card.Type.IPO,
            Card.Type.SPRINT_PLANNING , Card.Type.DEPLOYMENT_PIPELINE, Card.Type.TECH_DEBT, Card.Type.UNIT_TEST,
            Card.Type.EVERGREEN_TEST, Card.Type.HACK, Card.Type.RANSOMWARE, Card.Type.MONITORING,
            Card.Type.MERGE_CONFLICT, Card.Type.REFACTOR, Card.Type.BACKLOG, Card.Type.DAILY_SCRUM, Card.Type.PARALLELIZATION);
  
            return actionCards;
    }

    public static List<Card.Type> getMoneyCards(){
        List<Card.Type> moneyCards = Arrays.asList(Card.Type.BITCOIN, Card.Type.ETHEREUM, Card.Type.DOGECOIN);
        return moneyCards;
    }

    /**
     * Return the number of cards drawn when this card is played.
     * Based on CARD_REFERENCE.txt
     */
    public static int CardsReturned(Card.Type cardType) {
        // Based on card type, return estimated cards drawn
        switch (cardType) {
            // Money Cards
            case BITCOIN: return 0;              // No cards
            case ETHEREUM: return 0;             // No cards
            case DOGECOIN: return 0;             // No cards
            
            // Basic Action Cards
            case BACKLOG: return 1;              // Discard any number, draw that many (baseline 1)
            case CODE_REVIEW: return 1;          // +1 Card
            case SPRINT_PLANNING: return 1;      // +1 Card
            case UNIT_TEST: return 2;            // Choose +2 Cards
            
            // Mid-tier Action Cards
            case HACK: return 0;                 // +2 Money (not cards)
            case TECH_DEBT: return 1;            // +1 Card
            case REFACTOR: return 0;             // No cards drawn
            case PARALLELIZATION: return 0;      // No cards drawn
            case MERGE_CONFLICT: return 0;       // Draws cards = trashed card cost (variable)
            case MONITORING: return 2;           // +2 Cards
            
            // Premium Action Cards
            case IPO: return 2;                  // +2 Cards
            case DEPLOYMENT_PIPELINE: return 0;  // No cards
            case EVERGREEN_TEST: return 2;       // +2 Cards
            case DAILY_SCRUM: return 4;          // +4 Cards
            
            // Ultimate Attack Card
            case RANSOMWARE: return 3;           // +3 Cards
            
            default: return 0;
        }
    }
    
    /**
     * Return the amount of money generated when this card is played.
     * Based on CARD_REFERENCE.txt
     */
    public static int MoneyReturned(Card.Type cardType) {
        switch (cardType) {
            // Money Cards
            case BITCOIN: return 1;              // 1 money
            case ETHEREUM: return 2;             // 2 money
            case DOGECOIN: return 3;             // 3 money
            
            // Basic Action Cards
            case BACKLOG: return 0;              // No money
            case CODE_REVIEW: return 0;          // No money
            case SPRINT_PLANNING: return 0;      // No money
            case UNIT_TEST: return 2;            // Choose +2 Money
            
            // Mid-tier Action Cards
            case HACK: return 2;                 // +2 Money (Attack)
            case TECH_DEBT: return 1;            // +1 Money
            case REFACTOR: return 0;             // No money
            case PARALLELIZATION: return 0;      // No money
            case MERGE_CONFLICT: return 0;       // No money
            case MONITORING: return 0;           // No money
            
            // Premium Action Cards
            case IPO: return 2;                  // +2 Money
            case DEPLOYMENT_PIPELINE: return 1;  // +1 Money (+ 1 per empty pile)
            case EVERGREEN_TEST: return 0;       // No money
            case DAILY_SCRUM: return 0;          // No money
            
            // Ultimate Attack Card
            case RANSOMWARE: return 0;           // No money
            
            default: return 0;
        }
    }
    
    /**
     * Return the number of action points generated when this card is played.
     * Based on CARD_REFERENCE.txt
     */
    public static int ActionsReturned(Card.Type cardType) {
        switch (cardType) {
            // Money Cards
            case BITCOIN: return 0;              // No actions
            case ETHEREUM: return 0;             // No actions
            case DOGECOIN: return 0;             // No actions
            
            // Basic Action Cards
            case BACKLOG: return 1;              // +1 Action
            case CODE_REVIEW: return 2;          // +2 Actions
            case SPRINT_PLANNING: return 1;      // +1 Action
            case UNIT_TEST: return 2;            // Choose +2 Actions
            
            // Mid-tier Action Cards
            case HACK: return 0;                 // No extra actions
            case TECH_DEBT: return 1;            // +1 Action
            case REFACTOR: return 0;             // No actions
            case PARALLELIZATION: return 0;      // No actions (plays card twice)
            case MERGE_CONFLICT: return 0;       // No actions
            case MONITORING: return 0;           // No actions
            
            // Premium Action Cards
            case IPO: return 1;                  // +1 Action
            case DEPLOYMENT_PIPELINE: return 0;  // No actions (generates +1 Buy)
            case EVERGREEN_TEST: return 0;       // No actions
            case DAILY_SCRUM: return 0;          // No actions
            
            // Ultimate Attack Card
            case RANSOMWARE: return 0;           // No actions
            
            default: return 0;
        }
    }
}
