# group-b
COSI 103 - Software Engineering Group
Ananya Dalal - ananyadalal@brandeis.edu
Chloe Wahl-Dassule - cwahldassule@brandeis.edu
Jaile Estell - jestell@brandeis.edu
Bhoomika Chopra - bchopra@brandeis.edu

https://www.javadoc.io/doc/io.github.brandeis-cosi-103a/atg-api/1.5.2/index.html/

**Project Structure**

- **Root files:** Top-level files include [CARD_REFERENCE.txt](CARD_REFERENCE.txt) (card reference), [pom.xml](pom.xml) (Maven build file), [README.md](README.md) (this file), and [STATUS.md](STATUS.md) (project status and notes).
- **optimization_documentation/**: Contains optimization notes and metrics (e.g., V2_metrics.txt, V3_metrics.txt, WEIGHT_OPTIMIZATION_LOG.md).
- **src/**: Main source tree.
	- **src/main/java/**: Java source packages under `edu.brandeis.cosi103a.groupb`, implementation code for the engine, network, rating, etc.
	- **src/test/java/**: Unit tests mirroring the main package structure.
- **target/**: Build output produced by Maven (compiled classes, generated sources, surefire test reports).
	- **target/classes/**: Compiled classes used at runtime.
	- **target/surefire-reports/**: Test results and XML reports from Maven Surefire.

This README section documents the repository layout so developers can quickly find sources, tests, and build artifacts.

**Package Details**

- **edu.brandeis.cosi103a.groupb (root package):** Application entrypoints and player-facing classes. Contains `App.java` (main entry), `GameHarness.java` (local runner), player implementations (`ConsolePlayer.java`, `FlexiblePlayer.java`, `StrategyPlayer.java`, `V2StrategyPlayer.java`, `V3StrategyPlayer.java`, `BigMoneyPlayer.java`, `ParentPlayer.java`), networking endpoints (`PlayerClient.java`, `PlayerServer.java`), and helpers such as `CardInfo.java` and `RecordingGameObserver.java`.
- **engine:** Core game logic and mutable game state. Key classes:
	- `Engine.java`: central game loop and rule orchestration.
	- `MutableGameState.java`: in-memory representation of the current game state.
	- `PlayerCards.java`, `BoardCards.java`: card collections for players and the shared board.
	- `ActionCardHandler.java` and `CardFunctions/ActionCards.java`: implementations for action-type cards and their effects.
- **network:** Lightweight request/response types used for client-server interactions and logging (`DecisionRequest.java`, `DecisionResponse.java`, `LogEventRequest.java`). These DTOs enable remote player decisioning and event forwarding.
- **rating:** Utilities and harnesses for evaluating player performance and running tournaments. Includes `PlayerRatingHarness.java` (rating experiments), `GameRecord.java` (match/result persistence), `SelectedPlayer.java` (selected player metadata), and `TournamentScheduler.java` (scheduling round-robin or tournament runs).

**Source tree (trimmed)**

```
src/main/java/edu/brandeis/cosi103a/groupb/
├─ App.java
├─ BigMoneyPlayer.java
├─ CardInfo.java
├─ ConsolePlayer.java
├─ FlexiblePlayer.java
├─ GameHarness.java
├─ ParentPlayer.java
├─ PlayerClient.java
├─ PlayerServer.java
├─ RecordingGameObserver.java
├─ StrategyPlayer.java
├─ V2StrategyPlayer.java
├─ V3StrategyPlayer.java
├─ engine/
│  ├─ ActionCardHandler.java
│  ├─ BoardCards.java
│  ├─ CardFunctions/
│  │  └─ ActionCards.java
│  ├─ Engine.java
│  ├─ MutableGameState.java
│  └─ PlayerCards.java
├─ network/
│  ├─ DecisionRequest.java
│  ├─ DecisionResponse.java
│  └─ LogEventRequest.java
└─ rating/
	 ├─ GameRecord.java
	 ├─ PlayerRatingHarness.java
	 ├─ SelectedPlayer.java
	 └─ TournamentScheduler.java
```

The tree above is a concise snapshot of the primary source files and package subdirectories to help developers quickly find code responsibilities. For more detail, explore `src/main/java/edu/brandeis/cosi103a/groupb` and the corresponding tests in `src/test/java/edu/brandeis/cosi103a/groupb`.

