package ai_cup_22.strategy.potentialfield.scorecontributors.basic;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;

public class LinearScoreContributor implements ScoreContributor {
    private final Position position;
    private final double maxScore;
    private final double minScore;
    private final double minDist;
    private final double maxDist;

    public LinearScoreContributor(Position position, double maxScore, double minScore, double maxDist) {
        this(position, maxScore, minScore, 0, maxDist);
    }

    public LinearScoreContributor(Position position, double maxScore, double minScore, double minDist, double maxDist) {
        this.position = position;
        this.maxScore = maxScore;
        this.minScore = minScore;
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

        return maxScore - ((this.position.getDistanceTo(score.getPosition()) - minDist) / (maxDist - minDist) * (maxScore - minScore));
    }
}
