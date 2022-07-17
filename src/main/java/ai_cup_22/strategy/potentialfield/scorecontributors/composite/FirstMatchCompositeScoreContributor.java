package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FirstMatchCompositeScoreContributor extends BaseCompositeScoreContributor {
    public FirstMatchCompositeScoreContributor(String contributionReason) {
        this(contributionReason, false);
    }

    public FirstMatchCompositeScoreContributor(String contributionReason, boolean isStatic) {
        super(contributionReason, isStatic);
    }

    @Override
    public boolean shouldContribute(Score score) {
        return getFirstMatchScoreContributor(score).isPresent();
    }

    @Override
    public double getScoreValue(Score score) {
        return getFirstMatchScoreContributor(score)
                .map(c -> c.getScoreValue(score))
                .orElse(0.);
    }

    private Optional<ScoreContributor> getFirstMatchScoreContributor(Score score) {
        for (var c: contributors) {
            if (c.shouldContribute(score)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }
}
