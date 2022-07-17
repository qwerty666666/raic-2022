package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.potentialfield.Score;

public class SumCompositeScoreContributor extends BaseCompositeScoreContributor {
    public SumCompositeScoreContributor(String contributionReason) {
        super(contributionReason);
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
        for (var c: contributors) {
            c.contribute(score);
        }
    }
}
