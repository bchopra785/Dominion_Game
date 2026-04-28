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
    List<Card.Type> deck = new ArrayList<>();
    
    float moneyCardProp = 0.6f;
    float actionCardProp = 0.3f;
    float pointCardProp = 0.1f;

    float moneyCardProp_end = 0.6f;
    float actionCardProp_end = 0.1f;
    float pointCardProp_end = 0.3f;

    int permutation_cap = 5;
    int end_game_threshold = 5;



    

    public V2StrategyPlayer() {
        super("StrategyPlayer-" + COUNTER.getAndIncrement());
        for(int i = 0; i < 7; i++) {
            deck.add(Card.Type.BITCOIN);
            if (i < 3) {
                deck.add(Card.Type.METHOD);
            }
        };

        
    }

    public V2StrategyPlayer(String name) {
        super(name);
        for(int i = 0; i < 7; i++) {
            deck.add(Card.Type.BITCOIN);
            if (i < 3) {
                deck.add(Card.Type.METHOD);
            }
        };
    }

    public V2StrategyPlayer(String name, PrintStream out) {
        super(name);
        for(int i = 0; i < 7; i++) {
            deck.add(Card.Type.BITCOIN);
            if (i < 3) {
                deck.add(Card.Type.METHOD);
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

        if(this.buyableCards.getNumAvailable(Card.Type.FRAMEWORK) <= end_game_threshold) {
            moneyCardProp = moneyCardProp_end;
            actionCardProp = actionCardProp_end;
            pointCardProp = pointCardProp_end;
        }

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
        List<Card.Type> handTypes = new ArrayList<>();
        List<Card.Type> actionHand = new ArrayList<>();
        for (Card c : hand) {
            handTypes.add(c.type());
            if(actionCards.contains(c.type())) {
                actionHand.add(c.type());
            }
        }

        //remove cards you definitely dont want to play
        // if(actionHand.contains(Card.Type.REFACTOR)) {
        //     if(!handTypes.contains(Card.Type.BUG)) {
        //         actionHand.remove(Card.Type.REFACTOR);
        //     }
        // }
        // if(actionHand.contains(Card.Type.MERGE_CONFLICT)) {
        //     if(!handTypes.contains(Card.Type.BUG)) {
        //         actionHand.remove(Card.Type.MERGE_CONFLICT);
        //     }
        // }
        

        if(actionHand.isEmpty()) {
            return findBestFallback(options, GameState.TurnPhase.ACTION);
        } else if (actionHand.size() > permutation_cap) {
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
            
            return findBestFallback(options, GameState.TurnPhase.ACTION);
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
        // Auto-buy FRAMEWORK whenever possible
        for (Decision d : options) {
            if (d instanceof BuyDecision) {
                BuyDecision bd = (BuyDecision) d;
                if (bd.cardType() == Card.Type.FRAMEWORK) {
                    deck.add(Card.Type.FRAMEWORK);
                    return d;
                }
            }
        }

        
        //evaluated deck prop
        int total = deck.size();
        int moneyCardsNum = 0;
        int actionCardsNum = 0;
        int pointCardsNum = 0;

        List<Type> actionCards = CardInfo.getActionCards();
        List<Type> moneyCards = CardInfo.getMoneyCards();
        List<Type> pointCards = Arrays.asList(Card.Type.METHOD, Card.Type.MODULE, Card.Type.FRAMEWORK);
        for (Card.Type c : deck) {
            if (actionCards.contains(c)) {
                actionCardsNum++;
            } else if (moneyCards.contains(c)) {
                moneyCardsNum++;
            } else if (pointCards.contains(c)) {
                pointCardsNum++;
            }
        }

        float moneyPropDiff = (float) (moneyCardProp - moneyCardsNum / (float) total);
        float actionPropDiff = (float) (actionCardProp - actionCardsNum / (float) total);
        float pointPropDiff = (float) (pointCardProp - pointCardsNum / (float) total);

        // Create category map and sort by diff value (highest to lowest)
        Map<String, Float> diffs = new HashMap<>();
        diffs.put("money", moneyPropDiff);
        diffs.put("action", actionPropDiff);
        diffs.put("point", pointPropDiff);

        List<String> categoryOrder = Arrays.asList("money", "action", "point");
        categoryOrder.sort((a, b) -> Float.compare(diffs.get(b), diffs.get(a))); // reverse order

        for (String category : categoryOrder) {
            //PlayCardDecision pcd = null;

            if("action".equals(category)){
                BuyDecision best = null;
                int degree = 0;
                for (Decision d : options) {
                    if (d instanceof BuyDecision) {
                        Card.Type type = ((BuyDecision) d).cardType();
                        if (type == Card.Type.IPO || type == Card.Type.EVERGREEN_TEST || type == Card.Type.MONITORING ) {
                            if (degree == 0 || degree > 1) {
                                degree = 1;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.CODE_REVIEW || type == Card.Type.RANSOMWARE || type == Card.Type.REFACTOR || type == Card.Type.PARALLELIZATION) {
                            if (degree == 0 || degree > 2) {
                                degree = 2;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.SPRINT_PLANNING || type == Card.Type.HACK || type == Card.Type.BACKLOG || type == Card.Type.DAILY_SCRUM || type == Card.Type.UNIT_TEST ) {
                            if (degree == 0 || degree > 3) {
                                degree = 3;
                                best = (BuyDecision) d;
                            }
                        }
                        
                    }
                    
                }

                if (best != null) {
                    deck.add(best.cardType());
                    return best;
                }

            } else if("money".equals(category)){
                BuyDecision best = null;
                
                for (Decision d : options) {
                    if (d instanceof BuyDecision) {
                        Card.Type type = ((BuyDecision) d).cardType();
                        int priority = 0;
                        
                        if (type == Card.Type.DOGECOIN) {
                            if(priority == 0 || priority > 1){
                                priority = 1;
                                best = (BuyDecision) d;
                            }
                            
                        } else if (type == Card.Type.ETHEREUM) {
                            if(priority == 0 || priority > 2){
                                priority = 2;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.BITCOIN) {
                           if(priority == 0 || priority > 3){
                                priority = 3;
                                best = (BuyDecision) d;
                            }
                        }
                    }
                }

                if (best != null) {
                    deck.add(best.cardType());
                    return best;
                }
            }
            else if("point".equals(category)){
                BuyDecision best = null;
                
                for (Decision d : options) {
                    if (d instanceof BuyDecision) {
                        Card.Type type = ((BuyDecision) d).cardType();
                        int priority = 0;
                        
                        if (type == Card.Type.FRAMEWORK) {
                            if(priority == 0 || priority > 1){
                                priority = 1;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.MODULE) {
                            if(priority == 0 || priority > 2){
                                priority = 2;
                                best = (BuyDecision) d;
                            }
                        } else if (type == Card.Type.METHOD) {
                            if(priority == 0 || priority > 3){
                                priority = 3;
                                best = (BuyDecision) d;
                            }
                        }
                    }
                }

                if (best != null) {
                    deck.add(best.cardType());
                    return best;
                }
            }
        }
        
        return findBestFallback(options, GameState.TurnPhase.BUY);

    }

    public Decision chooseTrashDecision(ImmutableList<Decision> options) {
        if (options == null || options.isEmpty()) {
            System.out.println(playerName + " No trash options available");
            return options.get(0);
        }

        // Try to find and trash the BUG card first
        for (Decision option : options) {
            if (option instanceof TrashCardDecision) {
                Card cardToTrash = ((TrashCardDecision) option).card();
                if (cardToTrash.type() == Card.Type.BUG) {
                    System.out.println(playerName + " TRASHING BUG card");
                    deck.remove(Card.Type.BUG);
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

        //if its possible to discard cards but no BUG or point cards, just end the phase
        for (Decision option : options) {
            if (option instanceof EndPhaseDecision) {
                System.out.println(playerName + " ENDING PHASE");
                return option;
            }
        }

        // Fallback: return first discard option
        System.out.println(playerName + " No BUG or point cards to discard, using fallback");
        return options.get(0);
    }

    public Decision chooseGainCardDecision(ImmutableList<Decision> options) {
        for (Decision option : options) {
            if (option instanceof GainCardDecision) {
                Card.Type type = ((GainCardDecision) option).cardType();
                if (type == Card.Type.FRAMEWORK) {
                    deck.add(Card.Type.FRAMEWORK);
                    return option;
                } else if (type == Card.Type.DOGECOIN) {
                    deck.add(Card.Type.DOGECOIN);
                    return option;
                } else if  (type == Card.Type.IPO) {
                    deck.add(Card.Type.IPO);
                    return option;
                } else if (type == Card.Type.ETHEREUM || type == Card.Type.EVERGREEN_TEST || type == Card.Type.MONITORING) {
                    deck.add(Card.Type.ETHEREUM);
                    return option;
                } else if (type == Card.Type.CODE_REVIEW || type == Card.Type.RANSOMWARE || type == Card.Type.REFACTOR || type == Card.Type.PARALLELIZATION) {
                    deck.add(type);
                    return option;
                } else if (type == Card.Type.SPRINT_PLANNING || type == Card.Type.HACK || type == Card.Type.BACKLOG || type == Card.Type.DAILY_SCRUM || type == Card.Type.UNIT_TEST ) {
                    deck.add(type);
                    return option;
                } else if (type == Card.Type.BITCOIN) {
                    deck.add(Card.Type.BITCOIN);
                    return option;
                }
            }
        }
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
