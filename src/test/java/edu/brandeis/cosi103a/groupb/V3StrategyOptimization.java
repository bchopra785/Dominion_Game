package edu.brandeis.cosi103a.groupb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.state.GameResult;
import edu.brandeis.cosi.atg.state.PlayerResult;
import edu.brandeis.cosi103a.groupb.engine.Engine;

public class V3StrategyOptimization {
    static Map<Card.Type, Float> actionCardValues = new HashMap<>();
    static{
        actionCardValues.put(Card.Type.BACKLOG, 0.077f);
        actionCardValues.put(Card.Type.CODE_REVIEW, 0.085f);
        actionCardValues.put(Card.Type.SPRINT_PLANNING, 0.081f);
        actionCardValues.put(Card.Type.UNIT_TEST, 0.054f);
        actionCardValues.put(Card.Type.HACK, 0.07f);
        actionCardValues.put(Card.Type.TECH_DEBT, 0.06f);
        actionCardValues.put(Card.Type.REFACTOR, 0.047f);
        actionCardValues.put(Card.Type.PARALLELIZATION, 0.055f);
        actionCardValues.put(Card.Type.MERGE_CONFLICT, 0.069f);
        actionCardValues.put(Card.Type.MONITORING, 0.077f);
        actionCardValues.put(Card.Type.IPO, 0.064f);
        actionCardValues.put(Card.Type.DEPLOYMENT_PIPELINE, 0.063f);
        actionCardValues.put(Card.Type.EVERGREEN_TEST, 0.076f);
        actionCardValues.put(Card.Type.DAILY_SCRUM, 0.067f);
        actionCardValues.put(Card.Type.RANSOMWARE, 0.055f);
    }

    static Map<Card.Type, Float> actionCardValuesMutate = new HashMap<>(actionCardValues);

    int generations = 10;
    int simulationsPerGeneration = 200;
    static List<Float> generationWins = new ArrayList<>();
    static{
        generationWins.add(0.0f); // Initial win rate for generation 0
    }
    
    @Disabled("This is a long-running optimization test, not a unit test")
    @Test
    public void runSimulation(){

        //how many times to mutate
        for(int i = 0; i < generations; i++){
            //mutate values by a small random amount
            for(Card.Type type : actionCardValues.keySet()){
                float currentValue = actionCardValues.get(type);
                float mutation = (float)Math.max(0.00f, Math.random() * 0.02 - 0.01); // Random mutation between -0.01 and 0.01
                actionCardValuesMutate.put(type, currentValue + mutation);
            }

            // Normalize and round to 3 decimal places
            double sum = 0.0;
            for(Float value : actionCardValuesMutate.values()){
                sum += value;
            }

            for(Card.Type type : actionCardValues.keySet()){
                float normalizedValue = (float)(actionCardValuesMutate.get(type) / sum);
                float rounded = (float) Math.round(normalizedValue * 1000) / 1000;
                actionCardValuesMutate.put(type, rounded);
            }

            // Simulate games with current values and evaluate performance
            int wins = 0;
            for(int j = 0; j < simulationsPerGeneration; j++){
                V3StrategyPlayer player = new V3StrategyPlayer("V3StrategyPlayer", actionCardValuesMutate);
                V2StrategyPlayer opponent = new V2StrategyPlayer("V2StrategyPlayer");
                List<ParentPlayer> players = Arrays.asList(player, opponent);

                Engine engine = new Engine(players);
                GameResult result = null;
                try {
                    result = engine.play();
                } catch (PlayerViolationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ImmutableList<PlayerResult> playerResults = result.playerResults();
                if(playerResults.get(0).playerName().contains("V3StrategyPlayer")){
                    wins++;
                }
            }
            if((float)wins / simulationsPerGeneration >= Collections.max(generationWins)){
                //if its better or equal to previous best, keep the mutation
                actionCardValues = new HashMap<>(actionCardValuesMutate);
            } else {
                // Otherwise, revert to previous values
                actionCardValuesMutate = new HashMap<>(actionCardValues);
            }
                generationWins.add((float) wins / simulationsPerGeneration);
        }
        printResults(generations, simulationsPerGeneration, generationWins, actionCardValues);
    }

    public void printResults(int generation, int simulations,List<Float> wins, Map<Card.Type, Float> actionCardValues){
        System.out.println("Generation " + generation + ": " + wins + " wins out of " + simulationsPerGeneration);
        System.out.println("Current Values: " + actionCardValues);
    }
}
