package edu.brandeis.cosi103a.groupb;

public class Card {
    private int cost;
    private int value;
    private String type;
    private int points; //victory points

    // Constructor
    public Card(int cost, int value, int points, String type) {
        this.cost = cost; //money cost
        this.value = value; //money value
        this.type = type;
        this.points = points; // Initialize points to 0
    }

    // Getters
    public int getCost() {
        return cost;
    }

    public int getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public int getPoints() {
        return points;
    }

    // Setters
    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    // toString method for easy printing
    @Override
    public String toString() {
        return "Card: " + type + "  " + "cost: " + cost + ", value: " + value + ", points: " + points + "\n";
    }
}
