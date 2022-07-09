package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;

public class ConstantInCircleForceContributor implements ForceContributor {
    private final Circle circle;
    private final double force;

    public ConstantInCircleForceContributor(Circle circle, double force) {
        this.circle = circle;
        this.force = force;
    }

    @Override
    public boolean shouldContribute(Position position) {
        return circle.contains(position);
    }

    @Override
    public double getForce(Position position) {
        return force;
    }
}
