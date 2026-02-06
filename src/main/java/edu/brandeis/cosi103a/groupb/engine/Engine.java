package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi.atg.state.GameResult;
import java.util.List;

public class Engine implements edu.brandeis.cosi.atg.engine.Engine {

    private final List<Player> players;

    public Engine(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players list cannot be null");
        }
        if (players.size() > 4) {
            throw new IllegalArgumentException("Engine supports at most 4 players");
        }
        this.players = players;
    }

    @Override
    public GameResult play() throws PlayerViolationException {
        throw new UnsupportedOperationException("Game engine not implemented yet");
    }

    public void initializeBoard() throws PlayerViolationException {
        
        throw new UnsupportedOperationException("Initialize not implemented yet");
    }
}
