package ai_cup_22.strategy.potentialfield.scorecontributors.basic;

import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.function.Supplier;

public class DelegatingScoreContributor implements ScoreContributor {
    private final Supplier<Boolean> condition;
    private final ScoreContributor contributor;

    public DelegatingScoreContributor(Supplier<Boolean> condition, ScoreContributor contributor) {
        this.condition = condition;
        this.contributor = contributor;
    }

    @Override
    public boolean shouldContribute(Score score) {
        return condition.get();
    }

    @Override
    public double getScoreValue(Score score) {
        return contributor.getScoreValue(score);
    }
}
