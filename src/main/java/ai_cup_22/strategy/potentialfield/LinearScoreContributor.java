package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;

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
        var d = score.getPosition().getSquareDistanceTo(this.position);
        return d <= maxDist * maxDist && d >= minDist * minDist;
    }

    @Override
    public void contribute(Score score) {
        var delta = maxScore - ((this.position.getDistanceTo(score.getPosition()) - minDist) / (maxDist - minDist) * (maxScore - minScore));

        score.increaseScore(delta);
    }
}
