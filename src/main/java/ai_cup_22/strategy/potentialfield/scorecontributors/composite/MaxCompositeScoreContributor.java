package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaxCompositeScoreContributor extends BaseCompositeScoreContributor {
    public MaxCompositeScoreContributor(String contributionReason) {
        this(contributionReason, false);
    }

    public MaxCompositeScoreContributor(String contributionReason, boolean isStatic) {
        super(contributionReason, isStatic);
    }

    @Override
    public boolean shouldContribute(Score score) {
        return contributors.stream().anyMatch(c -> c.shouldContribute(score));
    }

    @Override
    public double getScoreValue(Score score) {
        return contributors.stream()
                .mapToDouble(contributor -> contributor.getScoreValue(score))
                .max()
                .orElse(0);
    }
}
