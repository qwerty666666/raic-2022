package ai_cup_22.strategy.potentialfield.scorecontributors.basic;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.BaseScoreContributor;
import ai_cup_22.strategy.potentialfield.Score;

public class LinearScoreContributor extends BaseScoreContributor {
    private final Position position;
    private final double nearestScore;
    private final double furthestScore;
    private final double minDist;
    private final double maxDist;

    public LinearScoreContributor(Position position, double nearestScore, double furthestScore, double maxDist) {
        this(position, nearestScore, furthestScore, 0, maxDist);
    }

    public LinearScoreContributor(String contributionReason, Position position, double nearestScore, double furthestScore, double maxDist) {
        this(contributionReason, position, nearestScore, furthestScore, 0, maxDist);
    }

    public LinearScoreContributor(Position position, double nearestScore, double furthestScore, double minDist, double maxDist) {
        this(null, position, nearestScore, furthestScore, minDist, maxDist);
    }

    public LinearScoreContributor(String contributionReason, Position position, double nearestScore, double furthestScore, double minDist, double maxDist) {
        super(contributionReason);

        this.position = position;
        this.nearestScore = nearestScore;
        this.furthestScore = furthestScore;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    public boolean shouldContribute(Score score) {
        if (!super.shouldContribute(score)) {
            return false;
        }

        var d = score.getPosition().getSquareDistanceTo(this.position);

        return d <= maxDist * maxDist && d >= minDist * minDist;
    }

    @Override
    public double getScoreValue(Score score) {
        if (!this.shouldContribute(score)) {
            return 0;
        }

        return nearestScore - ((this.position.getDistanceTo(score.getPosition()) - minDist) / (maxDist - minDist) * (nearestScore - furthestScore));
    }
}
