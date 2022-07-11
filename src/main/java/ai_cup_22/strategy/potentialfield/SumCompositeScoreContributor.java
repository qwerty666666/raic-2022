package ai_cup_22.strategy.potentialfield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SumCompositeScoreContributor implements ScoreContributor {
    private final List<ScoreContributor> contributors = new ArrayList<>();

    public SumCompositeScoreContributor add(ScoreContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    public SumCompositeScoreContributor add(Collection<? extends ScoreContributor> contributors) {
        this.contributors.addAll(contributors);
        return this;
    }

    @Override
    public boolean shouldContribute(Score score) {
        return contributors.stream().anyMatch(c -> c.shouldContribute(score));
    }

    @Override
    public double getScoreValue(Score score) {
        return contributors.stream()
                .mapToDouble(contributor -> contributor.getScoreValue(score))
                .sum();
    }

    @Override
    public void contribute(Score score) {
        score.increaseScore(getScoreValue(score));
    }
}