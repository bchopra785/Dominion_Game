package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.cards.Card.Type;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.DiscardCardDecision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.decisions.GainCardDecision;
import edu.brandeis.cosi.atg.decisions.PlayCardDecision;
import edu.brandeis.cosi.atg.decisions.TrashCardDecision;
import edu.brandeis.cosi.atg.event.Event;
import edu.brandeis.cosi.atg.state.CardStacks;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi.atg.state.Hand;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


public class V2StrategyPlayer extends ParentPlayer {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    String playerName = "";
    Hand currentPlayerHand = null;
    GameState.TurnPhase phase = null;
    int availableActions = 0;
    int spendableMoney = 0;
    int availableBuys = 0;
    CardStacks buyableCards = null;
    List<Card> deck = new ArrayList<>();
    
    float moneyCardProp = 0.5f;
    float actionCardProp = 0.4f;
    float pointCardProp = 0.1f;

    

    public V2StrategyPlayer() {
        super("StrategyPlayer-" + COUNTER.getAndIncrement());
        for(int i = 0; i < 7; i++) {
            deck.add(new Card(Card.Type.BITCOIN, i));
            if (i < 3) {
                deck.add(new Card(Card.Type.METHOD, i));
            }
        };

        
    }

    public V2StrategyPlayer(String name) {
        super(name);
        for(int i = 0; i < 7; i++) {
            deck.add(new Card(Card.Type.BITCOIN, i));
            if (i < 3) {
                deck.add(new Card(Card.Type.METHOD, i));
            }
        };
    }

    public V2StrategyPlayer(String name, PrintStream out) {
        super(name);
        for(int i = 0; i < 7; i++) {
            deck.add(new Card(Card.Type.BITCOIN, i));
            if (i < 3) {
                deck.add(new Card(Card.Type.METHOD, i));
            }
        };
    }

   @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options) {
        this.playerName = state.currentPlayerName();
        this.currentPlayerHand = state.currentPlayerHand();
        this.phase = state.phase();
        this.availableActions = state.availableActions();
        this.spendableMoney = state.spendableMoney();
        this.availableBuys = state.availableBuys();
        this.buyableCards = state.buyableCards();

        try {
            
            if (state == null || options == null || options.isEmpty()) {
                return null;
            }

            if (state.phase() == GameState.TurnPhase.ACTION) {
                return chooseActionDecision(state, options);
            }

            if (state.phase() == GameState.TurnPhase.MONEY) {
                return chooseMoneyDecision(state, options);
            }

            if (state.phase() == GameState.TurnPhase.BUY) {
                return chooseBuyDecision(state, options);
            }

            // Check if this is a trash decision (not tied to a specific phase)
            if (!options.isEmpty() && options.get(0) instanceof TrashCardDecision) {
                return chooseTrashDecision(options);
            }

            // Check if this is a discard decision (not tied to a specific phase)
            if (!options.isEmpty() && options.get(0) instanceof DiscardCardDecision) {
                return chooseDiscardDecision(options);
            }

            // Check if this is a gain card decision (not tied to a specific phase)
            if (!options.isEmpty() && options.get(0) instanceof GainCardDecision) {
                return chooseGainCardDecision(options);
            }

            return findBestFallback(options, state.phase());
        } catch (Exception e) {
            System.err.println("ERROR in " + playerName + " makeDecision: " + e.getMessage());
            e.printStackTrace();
            return (options != null && !options.isEmpty()) ? options.get(0) : null;
        }
    }


    public Decision chooseActionDecision(GameState state, ImmutableList<Decision> options) {
        List<Card.Type> actionCards = CardInfo.getActionCards();
        
        ImmutableCollection<Card> hand = state.currentPlayerHand().unplayedCards();
        List<Card.Type> actionHand = new ArrayList<>();
        for (Card c : hand) {
            if(actionCards.contains(c.type())) {
                actionHand.add(c.type());
            }
        }

        if(actionHand.isEmpty()) {
            return findBestFallback(options, GameState.TurnPhase.ACTION);
        } else if (actionHand.size() > 3) {
            Card.Type bestAction = null;
            int actions = 0;

            for (Card.Type t : actionHand) {
                int cardActions = CardInfo.ActionsReturned(t);
                if (cardActions > actions || bestAction == null) {
                    bestAction = t;
                    actions = cardActions;
                } else if (cardActions == actions) {
                    int bestCards = CardInfo.CardsReturned(bestAction);
                    int currentCards = CardInfo.CardsReturned(t);
                    if (currentCards > bestCards) {
                        bestAction = t;
                    }
                }
            
            }

            for (Decision d : options) {
                if (d instanceof PlayCardDecision) {
                    PlayCardDecision pcd = (PlayCardDecision) d;
                    if (pcd.card().type() == bestAction) {
                        return d;
                    }
                }
            }
        }

        Collection<List<Card.Type>> permutations = Collections2.permutations(actionHand);
        Map<List<Card.Type>, Integer> result = new HashMap<>();


        for (List<Card.Type> perm : permutations) {
            int actions = state.availableActions();
            int money = 0;
            int cards = 0;

            int actionsMultiplier = 1;
            int moneyMultiplier = 3;
            int cardsMultiplier = 2;

            List<Card.Type> current = perm;
            result.put(perm, 0);
            int i = 0;
            Card.Type next = current.get(i);

            while(actions > 0 && next != null) {
                i ++;
                actions--; // Playing a card costs 1 action
                actions += CardInfo.ActionsReturned(next);
                money += CardInfo.MoneyReturned(next);
                cards += CardInfo.CardsReturned(next);

                if(i == current.size()) {
                    int score = (actions * actionsMultiplier) + (money * moneyMultiplier) + (cards * cardsMultiplier);
                    result.put(perm, score);
                    break;
                }
                next = current.get(i);
                
            }
        }

        List<Card.Type> bestPerm = Collections.max(result.entrySet(), 
                                    Map.Entry.comparingByValue()).getKey();
        Card.Type bestFirstCard = bestPerm.get(0);
        
        for (Decision d : options) {
            if (d instanceof PlayCardDecision) {
                PlayCardDecision pcd = (PlayCardDecision) d;
                if (pcd.card().type() == bestFirstCard) {
                    return d;
                }
            }
        }

        return findBestFallback(options, GameState.TurnPhase.ACTION);
    }


    public Decision chooseMoneyDecision(GameState state, ImmutableList<Decision> options) {
        
        //play all money cards
        for (Decision d : options) {
            if (d instanceof PlayCardDecision) {
                return d;
            }
        }

        return findBestFallback(options, GameState.TurnPhase.MONEY);
    }

    /**
    * Person B: BUY-phase logic. Prefer card draw (DAILY_SCRUM > IPO), then best money (DOGECOIN > ETHEREUM > BITCOIN), then EndPhaseDecision. Tie-break: first matching BuyDecision in options.
     */
    public Decision chooseBuyDecision(GameState state, ImmutableList<Decision> options) {
        //evaluated deck prop
        int total = deck.size();
        int moneyCardsNum = 0;
        int actionCardsNum = 0;
        int pointCardsNum = 0;

        List<Type> actionCards = CardInfo.getActionCards();
        List<Type> moneyCards = CardInfo.getMoneyCards();
        List<Type> pointCards = Arrays.asList(Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK);
        for (Card c : deck) {
            if (actionCards.contains(c.type())) {
                actionCardsNum++;
            } else if (moneyCards.contains(c.type())) {
                moneyCardsNum++;
            } else if (pointCards.contains(c.type())) {
                pointCardsNum++;
            }
        }

        float moneyPropDiff = (float) (moneyCardProp - moneyCardsNum / (float) total);
        float actionPropDiff = (float) (actionCardProp - actionCardsNum / (float) total);
        float pointPropDiff = (float) (pointCardProp - pointCardsNum / (float) total);

        

        List<Float> priorities= Arrays.asList(moneyPropDiff, actionPropDiff, pointPropDiff);
        priorities.sort(null);
        Map<String, Float> categoryPriorityMap = new HashMap<>();
        categoryPriorityMap.put("money", moneyPropDiff);
        categoryPriorityMap.put("action", actionPropDiff);
        categoryPriorityMap.put("point", pointPropDiff);

        int index = 0;
        while(true){
            if(index >= priorities.size()) {
                break;
            }
            float category = priorities.get(index);
            PlayCardDecision pcd = null;

            if(category == moneyPropDiff){
                BuyDecision best = null;
                int degree = 0;
                for (Decision d : options) {
                    if (d instanceof BuyDecision) {
                        Card.Type type = ((BuyDecision) d).cardType();
                        if (type == Card.Type.IPO || type == Card.Type.EVERGREEN_TEST || type == Card.Type.MONITORING ) {
                            if (degree == 0 || degree < 1) {
                                degree = 1;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.CODE_REVIEW || type == Card.Type.RANSOMWARE || type == Card.Type.REFACTOR || type == Card.Type.PARALLELIZATION) {
                            if (degree == 0 || degree < 2) {
                                degree = 2;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.SPRINT_PLANNING || type == Card.Type.HACK || type == Card.Type.BACKLOG || type == Card.Type.DAILY_SCRUM || type == Card.Type.UNIT_TEST ) {
                            if (degree == 0 || degree < 3) {
                                degree = 3;
                                best = (BuyDecision) d;
                            }
                        }
                        
                    }
                    
                }

                if (best != null) {
                    return best;
                } else{
                    index++;
                    continue;
                }

            } else if(category == moneyPropDiff){
                BuyDecision best = null;
                for (Decision d : options) {
                    if (d instanceof BuyDecision) { // Add this check!
                        Card.Type type = ((BuyDecision) d).cardType();
                
                        if (type == Card.Type.DOGECOIN) {
                            if (best == null || best.cardType() != Card.Type.DOGECOIN) {
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.ETHEREUM) {
                            if (best == null || best.cardType() != Card.Type.ETHEREUM) {
                                best = (BuyDecision) d;
                            }
                        }
                    }
                     
                }

                if (best != null) {
                    return best;
                } else{
                    index++;
                    continue;
                }
            }
            else if(category == pointPropDiff){
                BuyDecision best = null;
                for (Decision d : options) {
                    if (d instanceof BuyDecision) { // Add this check!
                        Card.Type type = ((BuyDecision) d).cardType();
                        if (type == Card.Type.FRAMEWORK) {
                            if (best == null || best.cardType() != Card.Type.FRAMEWORK) {
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.MODULE) {
                            if (best == null || best.cardType() != Card.Type.FRAMEWORK) {
                                best = (BuyDecision) d;
                            }
                        } 
                    }
                    
                }

                if (best != null) {
                    return best;
                } else{
                    index++;
                    continue;
                }
            }
        }
        
        return findBestFallback(options, GameState.TurnPhase.BUY);

    }

    public Decision chooseTrashDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        // Try to find and trash the BUG card first
        for (Decision option : options) {
            if (option instanceof TrashCardDecision) {
                Card cardToTrash = ((TrashCardDecision) option).card();
                if (cardToTrash.type() == Card.Type.BUG) {
                    System.out.println(playerName + " TRASHING BUG card");
                    return option;
                }
            }
        }

        // Fallback: return first trash option
        System.out.println(playerName + " No BUG card to trash, using fallback");
        return options.get(0);
    }

    public Decision chooseDiscardDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        // Priority 1: Try to find and discard the BUG card first
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision) {
                Card cardToDiscard = ((DiscardCardDecision) option).card();
                if (cardToDiscard.type() == Card.Type.BUG) {
                    System.out.println(playerName + " DISCARDING BUG card");
                    return option;
                }
            }
        }

        // Priority 2: Try to discard point cards (low-value)
        List<Card.Type> pointCards = Arrays.asList(Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK);
        for (Decision option : options) {
            if (option instanceof DiscardCardDecision) {
                Card cardToDiscard = ((DiscardCardDecision) option).card();
                if (pointCards.contains(cardToDiscard.type())) {
                    System.out.println(playerName + " DISCARDING point card: " + cardToDiscard.type());
                    return option;
                }
            }
        }

        // Fallback: return first discard option
        System.out.println(playerName + " No BUG or point cards to discard, using fallback");
        return options.get(0);
    }

    public Decision chooseGainCardDecision(ImmutableList<Decision> options) {

        return findBestFallback(options, this.phase);

    }

    // Helper: extract Card.Type from Decision if possible
    private Card.Type getTypeFromDecision(Decision d) {
        if (d instanceof PlayCardDecision) {
            return ((PlayCardDecision) d).card().type();
        }
        // Prefer direct type check for BuyDecision if available
        if (d instanceof edu.brandeis.cosi.atg.decisions.BuyDecision) {
            return ((edu.brandeis.cosi.atg.decisions.BuyDecision) d).cardType();
        }
        return null;
    }

    private Decision findBestFallback(ImmutableList<Decision> options, GameState.TurnPhase preferredPhase) {
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                EndPhaseDecision end = (EndPhaseDecision) option;
                if (end.phase() == preferredPhase) {
                    return option;
                }
            }
        }

        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                return option;
            }
        }

        return options.get(0);
    }

    @Override
    public Decision makeDecision(GameState state, ImmutableList<Decision> options, Optional<Event> reason) {
        return makeDecision(state, options);
    }
}
