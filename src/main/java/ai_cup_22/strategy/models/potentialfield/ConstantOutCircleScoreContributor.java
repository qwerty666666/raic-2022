package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Circle;

public class ConstantOutCircleScoreContributor implements ScoreContributor {
    private final Circle circle;
    private final double score;

    public ConstantOutCircleScoreContributor(Circle circle, double score) {
        this.circle = circle;
        this.score = score;
    }

    @Override
    public boolean shouldContribute(Score score) {
        return !circle.contains(score.getPosition());
    }

    @Override
    public void contribute(Score score) {
        score.increaseScore(this.score);
    }
}
