package ai_cup_22.strategy.potentialfield;

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
        if (!ScoreContributor.super.shouldContribute(score)) {
            return false;
        }

        return !circle.contains(score.getPosition());
    }

    @Override
    public double getScoreValue(Score score) {
        if (!this.shouldContribute(score)) {
            return 0;
        }

        return this.score;
    }
}
