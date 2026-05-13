# Inspired by the Dominion Card Game

This project was developed for **COSI 103a – Fundamentals of Software Engineering (Spring 2026)** at Brandeis University, taught by Joe Delfino.

It is a group software engineering project focused on building and extending a Java-based game engine with networking, player AI implementations, and rating/tournament infrastructure.

---

## API Reference

https://www.javadoc.io/doc/io.github.brandeis-cosi-103a/atg-api/1.5.2/index.html/

---

## Group Members

- Ananya Dalal 
- Chloe Wahl-Dassule  
- Jaile Estell 
- Bhoomika Chopra  

---

## Project Structure

### Root Files
- `CARD_REFERENCE.txt` — card reference documentation  
- `pom.xml` — Maven build configuration  
- `README.md` — project documentation  
- `STATUS.md` — current bugs and development status  

---

### Source Code

#### `src/main/java/`
Main application source code under package `edu.brandeis.cosi103a.groupb`.

#### `src/test/java/`
Unit tests mirroring the main package structure.

---

### Key Modules

#### `engine`
Core game engine and state management:
- `Engine.java` — main game loop and rule logic  
- `MutableGameState.java` — in-memory game state  
- `PlayerCards.java`, `BoardCards.java` — card management  
- `ActionCardHandler.java` + `CardFunctions/ActionCards.java` — action card logic  

#### `network`
Client-server communication layer:
- `DecisionRequest.java`
- `DecisionResponse.java`
- `LogEventRequest.java`

Used for remote player interactions and logging.

#### `rating`
Evaluation and tournament system:
- `PlayerRatingHarness.java` — performance evaluation  
- `GameRecord.java` — match tracking  
- `SelectedPlayer.java` — player metadata  
- `TournamentScheduler.java` — tournament execution  

---

## Source Tree (Simplified)

```
src/main/java/edu/brandeis/cosi103a/groupb/
├── App.java
├── BigMoneyPlayer.java
├── CardInfo.java
├── ConsolePlayer.java
├── FlexiblePlayer.java
├── GameHarness.java
├── ParentPlayer.java
├── PlayerClient.java
├── PlayerServer.java
├── RecordingGameObserver.java
├── StrategyPlayer.java
├── V2StrategyPlayer.java
├── V3StrategyPlayer.java
│
├── engine/
│   ├── ActionCardHandler.java
│   ├── BoardCards.java
│   ├── CardFunctions/
│   │   └── ActionCards.java
│   ├── Engine.java
│   ├── MutableGameState.java
│   └── PlayerCards.java
│
├── network/
│   ├── DecisionRequest.java
│   ├── DecisionResponse.java
│   └── LogEventRequest.java
│
└── rating/
    ├── GameRecord.java
    ├── PlayerRatingHarness.java
    ├── SelectedPlayer.java
    └── TournamentScheduler.java
```

---

## Notes

This repository reflects a collaborative group project and includes contributions from all team members. It has been organized for clarity and ease of navigation.
