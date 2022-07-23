package ai_cup_22.strategy.potentialfield.scorecontributors;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;

public class TreeScoreContributor implements ScoreContributor {
    private final Circle circle;
    private final ScoreContributor scoreContributor;

    public TreeScoreContributor(Obstacle obstacle) {
        this.circle = obstacle.getCircle().enlarge(Constants.USER_RADIUS);

        this.scoreContributor = new LinearScoreContributor(
                circle.getCenter(),
                Constants.PF_TREE_MIN_SCORE,
                0,
                circle.getRadius(),
                circle.getRadius() + Constants.PF_TREE_DIST
        );
    }

    @Override
    public String getContributionReason(Score score) {
        return "Tree";
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void contribute(Score score) {
        if (circle.contains(score.getPosition())) {
            score.setIsUnreachable();
        }

        ScoreContributor.super.contribute(score);
    }

    @Override
    public double getScoreValue(Score score) {
        return scoreContributor.getScoreValue(score);
    }
}
