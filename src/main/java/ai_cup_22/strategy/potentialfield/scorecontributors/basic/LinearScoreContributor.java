package ai_cup_22.strategy.potentialfield.scorecontributors.basic;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;

public class LinearScoreContributor implements ScoreContributor {
    private final Position position;
    private final double nearestScore;
    private final double furthestScore;
    private final double minDist;
    private final double maxDist;

    public LinearScoreContributor(Position position, double nearestScore, double furthestScore, double maxDist) {
        this(position, nearestScore, furthestScore, 0, maxDist);
    }

    public LinearScoreContributor(Position position, double nearestScore, double furthestScore, double minDist, double maxDist) {
        this.position = position;
        this.nearestScore = nearestScore;
        this.furthestScore = furthestScore;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    @Override
    public boolean shouldContribute(Score score) {
        if (!ScoreContributor.super.shouldContribute(score)) {
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

        if (World.getInstance().getCurrentTick() == 78 && score.getPosition().equals(new Position(120, -59))) {
            int a = 0;
        }

        return nearestScore - ((this.position.getDistanceTo(score.getPosition()) - minDist) / (maxDist - minDist) * (nearestScore - furthestScore));
    }
}
