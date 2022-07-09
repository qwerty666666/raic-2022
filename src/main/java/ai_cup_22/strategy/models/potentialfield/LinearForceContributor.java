package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Position;

public class LinearForceContributor implements ForceContributor {
    private final Position position;
    private final double maxForce;
    private final double minForce;
    private final double minDist;
    private final double maxDist;

    public LinearForceContributor(Position position, double maxForce, double minForce, double maxDist) {
        this(position, maxForce, minForce, 0, maxDist);
    }

    public LinearForceContributor(Position position, double maxForce, double minForce, double minDist, double maxDist) {
        this.position = position;
        this.maxForce = maxForce;
        this.minForce = minForce;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    public boolean shouldContribute(Position position) {
        var d = position.getSquareDistanceTo(this.position);
        return d <= maxDist * maxDist && d >= minDist * minDist;
    }

    @Override
    public double getForce(Position position) {
        return maxForce - ((this.position.getDistanceTo(position) - minDist) / (maxDist - minDist) * (maxForce - minForce));
    }
}
