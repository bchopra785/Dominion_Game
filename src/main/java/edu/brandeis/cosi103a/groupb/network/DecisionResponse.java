package edu.brandeis.cosi103a.groupb.network;

import edu.brandeis.cosi.atg.decisions.Decision;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class DecisionResponse {
    // This is the decision object you picked from the 'options' list 
    // in the DecisionRequest.
    private Decision decision;

    // A comment or string explaining WHY you made this move (often used for debugging)
    private String metadata;

    // Default constructor (required for JSON tools)
    public DecisionResponse() {}

    // Constructor to make creating the response easy
    public DecisionResponse(Decision decision, String metadata) {
        this.decision = decision;
        this.metadata = metadata;
    }

    // Getters and Setters (so Spring can read the data and also for testing)
    public Decision getDecision() { return decision; }

    public void setDecision(Decision decision) { this.decision = decision; }
    
    public String getMetadata() { return metadata; }
    
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
