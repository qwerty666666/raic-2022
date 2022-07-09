package ai_cup_22.strategy.potentialfield;

public interface ScoreContributor {
    default boolean shouldContribute(Score score) {
        return true;
    }

    void contribute(Score score);
}
