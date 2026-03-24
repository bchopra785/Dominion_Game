package edu.brandeis.cosi103a.groupb;

import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.decisions.BuyDecision;
import edu.brandeis.cosi.atg.decisions.Decision;
import edu.brandeis.cosi.atg.decisions.EndPhaseDecision;
import edu.brandeis.cosi.atg.state.GameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StrategyPlayerBuyTest {
    // Helper to create a BuyDecision for a given Card.Type
    private BuyDecision buy(Card.Type type) {
        return new BuyDecision(type);
    }

        @SuppressWarnings("nullness")
    @Test
    public void testPrefersDailyScrum() {
        StrategyPlayer player = new StrategyPlayer();
        Decision daily = buy(Card.Type.DAILY_SCRUM);
        Decision ipo = buy(Card.Type.IPO);
        Decision doge = buy(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(ipo, doge, daily);
        assertEquals(daily, player.chooseBuyDecision(null, options));
    }

        @SuppressWarnings("nullness")
    @Test
    public void testPrefersIpoIfNoDailyScrum() {
        StrategyPlayer player = new StrategyPlayer();
        Decision ipo = buy(Card.Type.IPO);
        Decision doge = buy(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(doge, ipo);
        assertEquals(ipo, player.chooseBuyDecision(null, options));
    }


        @SuppressWarnings("nullness")
    @Test
    public void testPrefersBestMoneyIfNoDraw() {
        StrategyPlayer player = new StrategyPlayer();
        Decision doge = buy(Card.Type.DOGECOIN);
        Decision eth = buy(Card.Type.ETHEREUM);
        Decision btc = buy(Card.Type.BITCOIN);
        ImmutableList<Decision> options = ImmutableList.of(btc, eth, doge);
        assertEquals(doge, player.chooseBuyDecision(null, options));
    }

        @SuppressWarnings("nullness")
    @Test
    public void testPrefersEndPhaseDecisionIfNoOther() {
        StrategyPlayer player = new StrategyPlayer();
        Decision end = new EndPhaseDecision(GameState.TurnPhase.BUY);
        ImmutableList<Decision> options = ImmutableList.of(end);
        assertEquals(end, player.chooseBuyDecision(null, options));
    }

        @SuppressWarnings("nullness")
    @Test
    public void testTieBreaksFirstBuyDecision() {
        StrategyPlayer player = new StrategyPlayer();
        Decision doge1 = buy(Card.Type.DOGECOIN);
        Decision doge2 = buy(Card.Type.DOGECOIN);
        ImmutableList<Decision> options = ImmutableList.of(doge1, doge2);
        assertEquals(doge1, player.chooseBuyDecision(null, options));
    }
}
