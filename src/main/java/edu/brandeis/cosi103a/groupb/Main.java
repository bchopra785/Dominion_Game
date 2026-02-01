package edu.brandeis.cosi103a.groupb;

import java.util.List;
import java.util.Random;

public class Main {

    public static void initialCards(Player player, Board board) {
        // Each player begins with a starter deck, consisting of 7 Bitcoins and 3 Methods. These
        // cards are distributed to each player at the beginning of the game, from the supply
        for (int i = 0; i < 7; i++) {
            player.getDeck().addToDiscard(board.removeCard("bitcoin"));
        }
        for (int i = 0; i < 3; i++) {
            player.getDeck().addToDiscard(board.removeCard("method"));
            player.addPoints(1);
        }
    }

    public static void turn(Player player, Board board, int strategy, int turnCount) {
        // At the start of each turn, the player draws 5 cards

        List<Card> hand = player.getHand();

        // Display drawn cards
        System.out.println(player.name + " drew: \n" + hand);

        // Calculate the total value of cards in hand
        int totalValue = 0;
        for (Card card : hand) {
            totalValue += card.getValue();
        }
        System.out.println("Total value of drawn cards: " + totalValue);
        //buy phase
        if (totalValue >= 8 && board.getCardsRemaining("framework") > 0) {
            System.out.println("Buying highest automation card: framework!");
            Card boughtCard = board.removeCard("framework");
            player.getDeck().addToDiscard(boughtCard);
            player.addPoints(boughtCard.getPoints());
            System.out.println("Player bought: " + boughtCard);
        }
        else if (board.getCardsRemaining("framework") <= 4) {
            if(strategy == 1){
                if(turnCount % 2 == 0){
                    // Purchase points
                    Card boughtCard = buyPoints(player, board, totalValue);
                    if (boughtCard != null) {
                        System.out.println("Player bought: " + boughtCard);
                    } else {
                        System.out.println("Player could not afford any automation cards");
                    }
                } else {
                    // Purchase money
                    Card boughtCard = buyMoney(player, board, totalValue);
                    if (boughtCard != null) {
                        System.out.println("Player bought: " + boughtCard);
                    } else {
                        System.out.println("Player could not afford any cryptocurrency cards");
                    }
                }
            } else {
                // Randomly choose between buying money (1) or buying points (2)
                Random random = new Random();
                int choice = random.nextInt(2) + 1; // Randomly choose 1 or 2
                
                if (choice == 1) {
                    Card boughtCard = buyMoney(player, board, totalValue);
                    if (boughtCard != null) {
                        System.out.println("Player bought: " + boughtCard);
                    } else {
                        System.out.println("Player could not afford any cryptocurrency cards");
                    }
                } else {
                    Card boughtCard = buyPoints(player, board, totalValue);
                    if (boughtCard != null) {
                        System.out.println("Player bought: " + boughtCard);
                    } else {
                        System.out.println("Player could not afford any automation cards");
                    }
                }
            }    
        }else{
            // Purchase the highest value cryptocurrency card the player can afford
            Card boughtCard = buyMoney(player, board, totalValue);
            if (boughtCard != null) {
                System.out.println("Player bought: " + boughtCard);
            } else {
                System.out.println("Player could not afford any cryptocurrency cards");
            }
        }
        
        player.clearHand();
        player.drawTurnHand();

    }

    public static Card buyPoints(Player player, Board board, int totalValue){
        System.out.println("Buying points! (Automation cards)");
        // Buy the automation card with the highest point value the player can afford
        if (totalValue >= 8 && board.getCardsRemaining("framework") > 0) {
            Card boughtCard = board.removeCard("framework");
            if (boughtCard != null) {
                player.addPoints(player.getDeck().addToDiscard(boughtCard));
                return boughtCard;
            }
        } else if (totalValue >= 5 && board.getCardsRemaining("module") > 0) {
            Card boughtCard = board.removeCard("module");
            if (boughtCard != null) {
                player.addPoints(player.getDeck().addToDiscard(boughtCard));
                return boughtCard;
            }
        } else if (totalValue >= 2 && board.getCardsRemaining("method") > 0) {
            Card boughtCard = board.removeCard("method");
            if (boughtCard != null) {
                player.addPoints(player.getDeck().addToDiscard(boughtCard));
                return boughtCard;
            }
        }
        return null; // No card was bought
    }

    public static Card buyMoney(Player player, Board board, int totalValue){
        System.out.println("Buying money! (Cryptocurrency cards)");
        // Buy the cryptocurrency card with the highest value the player can afford
       if (totalValue >= 6 && board.getCardsRemaining("dogecoin") > 0) {
                Card boughtCard = board.removeCard("dogecoin");
                if (boughtCard != null) {
                    player.addPoints(player.getDeck().addToDiscard(boughtCard));
                    return boughtCard;
                }
            } else if (totalValue >= 3 && board.getCardsRemaining("ethereum") > 0) {
                Card boughtCard = board.removeCard("ethereum");
                if (boughtCard != null) {
                    player.addPoints(player.getDeck().addToDiscard(boughtCard));
                    return boughtCard;
                }
            } else if (totalValue >= 0 && board.getCardsRemaining("bitcoin") > 0) {
                Card boughtCard = board.removeCard("bitcoin");
                if (boughtCard != null) {
                    player.addPoints(player.getDeck().addToDiscard(boughtCard));
                    return boughtCard;
                }
            }
        return null; // No card was bought
    }

    public static void winner(Player player1, Player player2) {
        System.out.println("Game Over!");
        System.out.println(player1.name + " has " + player1.getPoints() + " points.");
        System.out.println(player2.name + " has " + player2.getPoints() + " points.");
        if (player1.getPoints() > player2.getPoints()) {
            System.out.println(player1.name + " wins!");
        } else if (player2.getPoints() > player1.getPoints()) {
            System.out.println(player2.name + " wins!");
        } else {
            System.out.println("It's a tie!");
        }
    }

    public static void main(String[] args) {
        int turnCount = 0;
        Player player1 = new Player(new Deck(), "Player 1");
        Player player2 = new Player(new Deck(), "Player 2");

        Board board = new Board();

    // Each player begins with a starter deck, consisting of 7 Bitcoins and 3 Methods. These
    // cards are distributed to each player at the beginning of the game, from the supply
    
        initialCards(player1, board);
        initialCards(player2, board);

        player1.drawTurnHand();
        player2.drawTurnHand();

        // Randomly choose who starts the game
        Random random = new Random();
        boolean player1Turn = random.nextBoolean();
        String startingPlayer = player1Turn ? player1.name : player2.name;
        System.out.println(startingPlayer + " starts the game!");

        // Game loop - alternate between players
        boolean gameRunning = true;
        
        while (gameRunning) {
            if (player1Turn) {
                turn(player1, board, 1, turnCount);
            } else {
                turn(player2, board, 2, turnCount);
            }
            turnCount++;
            player1Turn = !player1Turn;
            if(board.getCardsRemaining("framework") == 0){
                gameRunning = false;
            }
        }

        // Determine and announce the winner
        winner(player1, player2);
    }
}
