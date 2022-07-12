package ai_cup_22.strategy.potentialfield;

public interface ScoreContributor {
    default boolean shouldContribute(Score score) {
        return score.getScore() != PotentialField.UNREACHABLE_VALUE;
    }

    double getScoreValue(Score score);

    default void contribute(Score score) {
        if (shouldContribute(score)) {
            score.increaseScore(getScoreValue(score));
        }
    }

    default void contribute(PotentialField potentialField) {
        potentialField.getScores().stream()
                .filter(this::shouldContribute)
                .forEach(this::contribute);
    }
}
