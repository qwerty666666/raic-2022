package ai_cup_22.strategy.potentialfield;

import java.util.ArrayList;
import java.util.List;

public class FirstMatchCompositeScoreContributor implements ScoreContributor {
    private final List<ScoreContributor> contributors = new ArrayList<>();

    public FirstMatchCompositeScoreContributor add(ScoreContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    @Override
    public boolean shouldContribute(Score score) {
        for (var c: contributors) {
            if (c.shouldContribute(score)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getScoreValue(Score score) {
        for (var contributor: contributors) {
            if (contributor.shouldContribute(score)) {
                return contributor.getScoreValue(score);
            }
        }

        return 0;
    }
}
