package edu.brandeis.cosi103a.groupb;

import edu.brandeis.cosi.atg.engine.PlayerViolationException;
import edu.brandeis.cosi103a.groupb.rating.GameRecord;
import edu.brandeis.cosi103a.groupb.rating.PlayerRatingHarness;
import edu.brandeis.cosi103a.groupb.rating.SelectedPlayer;
import edu.brandeis.cosi103a.groupb.rating.TournamentScheduler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerRatingHarnessTest {

    @Test
    public void scheduler_withFivePlayers_createsFivePodsOfFour() {
        TournamentScheduler scheduler = new TournamentScheduler();
        List<SelectedPlayer> selected = List.of(
            new SelectedPlayer("BigMoney#1", "BigMoney", BigMoneyPlayer::new),
            new SelectedPlayer("BigMoney#2", "BigMoney", BigMoneyPlayer::new),
            new SelectedPlayer("Strategy#1", "Strategy", StrategyPlayer::new),
            new SelectedPlayer("Strategy#2", "Strategy", StrategyPlayer::new),
            new SelectedPlayer("BigMoney#3", "BigMoney", BigMoneyPlayer::new)
        );

        List<List<SelectedPlayer>> pods = scheduler.schedule(selected);
        assertEquals(5, pods.size());
        for (List<SelectedPlayer> pod : pods) {
            assertEquals(4, pod.size());
        }
    }

    @Test
    public void harness_runTournament_collectsRawResults() throws PlayerViolationException {
        PlayerRatingHarness harness = new PlayerRatingHarness();
        List<SelectedPlayer> selected = List.of(
            new SelectedPlayer("BigMoney#1", "BigMoney", BigMoneyPlayer::new),
            new SelectedPlayer("BigMoney#2", "BigMoney", BigMoneyPlayer::new)
        );

        List<GameRecord> results = harness.runTournament(selected, 2);
        assertEquals(2, results.size());

        GameRecord first = results.get(0);
        assertEquals(2, first.matchupPlayers().size());
        assertEquals(2, first.playerResults().size());
        assertTrue(first.playerResults().stream().anyMatch(GameRecord.PlayerRecord::winner));
        assertFalse(first.playerResults().stream().anyMatch(r -> r.rank() < 1));
    }
}
