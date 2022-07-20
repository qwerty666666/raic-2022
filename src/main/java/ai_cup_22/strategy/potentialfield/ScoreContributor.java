package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.potentialfield.Score.Contribution;

public interface ScoreContributor {
    default boolean shouldContribute(Score score) {
        return score.getScore() != PotentialField.UNREACHABLE_VALUE;
    }

    double getScoreValue(Score score);

    default void contribute(Score score) {
        if (shouldContribute(score)) {
            var value = getScoreValue(score);
            if (DebugData.isEnabled) {
                score.increaseScore(new Contribution(getContributionReason(score), value, isStatic()));
            } else {
                score.increaseScore(value);
            }
        }
    }

    default String getContributionReason(Score score) {
        return getClass().getSimpleName();
    }

    default boolean isStatic() {
        return false;
    }

    default void contribute(PotentialField potentialField) {
        potentialField.getScores().values().stream()
                .filter(this::shouldContribute)
                .forEach(this::contribute);
    }
}
