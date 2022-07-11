package ai_cup_22.strategy.potentialfield;

import java.util.ArrayList;
import java.util.List;

public class SumCompositeScoreContributor implements ScoreContributor {
    private final List<ScoreContributor> contributors = new ArrayList<>();

    public SumCompositeScoreContributor add(ScoreContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    @Override
    public boolean shouldContribute(Score score) {
        return contributors.stream().anyMatch(c -> c.shouldContribute(score));
    }

    @Override
    public double getScoreValue(Score score) {
        if (!shouldContribute(score)) {
            return 0;
        }

        return contributors.stream()
                .filter(contributor -> contributor.shouldContribute(score))
                .findFirst()
                .get()
                .getScoreValue(score);
    }

    @Override
    public void contribute(Score score) {
        score.increaseScore(getScoreValue(score));
    }
}
