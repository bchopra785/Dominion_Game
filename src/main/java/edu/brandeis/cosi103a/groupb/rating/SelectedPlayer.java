package edu.brandeis.cosi103a.groupb.rating;

import edu.brandeis.cosi103a.groupb.ParentPlayer;

import java.util.function.Function;

/**
 * Represents one selected competitor slot in the rating harness.
 * Multiple slots may use the same template/factory.
 */
public record SelectedPlayer(
    String slotLabel,
    String templateName,
    Function<String, ParentPlayer> factory
) {

    public ParentPlayer newInstance() {
        return factory.apply(slotLabel);
    }
}
