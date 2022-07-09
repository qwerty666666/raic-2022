package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Position;

public interface ForceContributor {
    default boolean shouldContribute(Position position) {
        return true;
    }

    double getForce(Position position);
}
