package edu.brandeis.cosi103a.groupb.engine;

import edu.brandeis.cosi.atg.cards.Card;
import edu.brandeis.cosi.atg.state.GameState;
import edu.brandeis.cosi103a.groupb.ParentPlayer;
import edu.brandeis.cosi103a.groupb.engine.CardFunctions.ActionCards;
import java.util.List;
import java.util.Map;

/**
 * Isolates action-card dispatch so Engine action-phase flow stays focused on phase control.
 */
final class ActionCardHandler {

    private final List<ParentPlayer> players;
    private final Map<ParentPlayer, PlayerCards> playerCardsMap;
    private final BoardCards boardCards;

    ActionCardHandler(
        List<ParentPlayer> players,
        Map<ParentPlayer, PlayerCards> playerCardsMap,
        BoardCards boardCards
    ) {
        this.players = players;
        this.playerCardsMap = playerCardsMap;
        this.boardCards = boardCards;
    }

    GameState execute(Card playedCard, GameState state, ParentPlayer currentPlayer) {
        return ActionCards.playActionCard(
            playedCard,
            state,
            currentPlayer,
            players,
            playerCardsMap,
            boardCards
        );
    }

    boolean activatesCostReduction(Card playedCard) {
        return ActionCards.activatesCostReduction(playedCard);
    }
}
