package edu.brandeis.cosi103a.groupb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi103a.groupb.engine.BoardCards;
import edu.brandeis.cosi103a.groupb.engine.PlayerCards;

public class PlayerCardsTest {

	private static class TestPlayerCards extends PlayerCards {
		TestPlayerCards(BoardCards board) {
			super(board);
		}

		int getCostInHandPublic() {
			return getCostInHand();
		}

		void setUnplayedCards(List<Card> cards) throws Exception {
			Field field = PlayerCards.class.getDeclaredField("unplayedCards");
			field.setAccessible(true);
			field.set(this, cards);
		}
	}

	@Test
	public void testGetCostInHandCountsCurrencyOnly() throws Exception {
		BoardCards board = new BoardCards();
		TestPlayerCards playerCards = new TestPlayerCards(board);

		List<Card> unplayed = new ArrayList<>();
		unplayed.add(new Card(Card.Type.BITCOIN, 0)); //value of 1
		unplayed.add(new Card(Card.Type.ETHEREUM, 1)); //value of 2
		unplayed.add(new Card(Card.Type.DOGECOIN, 2)); //value of 3
		playerCards.setUnplayedCards(unplayed);

		int cost = playerCards.getCostInHandPublic();
		assertEquals(6, cost, "Cost in hand should count only currency cards");
	}
}
