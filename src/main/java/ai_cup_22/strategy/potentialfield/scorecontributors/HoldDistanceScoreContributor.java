package ai_cup_22.strategy.potentialfield.scorecontributors;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.CircularWithAvoidObstaclesContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;

public class HoldDistanceScoreContributor implements ScoreContributor {
    private final Unit targetEnemy;
    private final ScoreContributor scoreContributor;

    public HoldDistanceScoreContributor(Unit targetEnemy, double safeDist) {
        this.targetEnemy = targetEnemy;
        this.scoreContributor = new CircularWithAvoidObstaclesContributor(
                "Target Enemy Hold Distance",
                targetEnemy.getPosition(),
                new LinearScoreContributor(
                        targetEnemy.getPosition(),
                        Constants.PF_ENEMY_HOLD_DISTANCE_MAX_SCORE,
                        Constants.PF_ENEMY_HOLD_DISTANCE_MIN_SCORE,
                        safeDist,
                        safeDist + Constants.PF_ENEMY_HOLD_DISTANCE_DIST
                ),
                safeDist + Constants.PF_ENEMY_HOLD_DISTANCE_DIST,
                0
        );
    }

    @Override
    public boolean shouldContribute(Score score) {
        return scoreContributor.shouldContribute(score);
    }

    @Override
    public double getScoreValue(Score score) {
        if (targetEnemy.isPhantom()) {
            return scoreContributor.getScoreValue(score);
        }

        var angle = targetEnemy.getDirection().getAngleTo(new Vector(targetEnemy.getPosition(), score.getPosition()));

        return scoreContributor.getScoreValue(score) *
                new LinearDistributor(0, Math.PI, 1, 1.2).get(angle);
    }
}
