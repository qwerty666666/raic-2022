package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.potentialfield.Score;

public class SumCompositeScoreContributor extends BaseCompositeScoreContributor {
    public SumCompositeScoreContributor(String contributionReason) {
        super(contributionReason);
    }

    @Override
    public boolean shouldContribute(Score score) {
        for (var contributor: contributors) {
            if (contributor.shouldContribute(score)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getScoreValue(Score score) {
        var sum = 0.;
        for (var contributor: contributors) {
            sum += contributor.getScoreValue(score);
        }
        return sum;
    }

    @Override
    public void contribute(Score score) {
        for (var contributor: contributors) {
            contributor.contribute(score);
        }
    }
}
