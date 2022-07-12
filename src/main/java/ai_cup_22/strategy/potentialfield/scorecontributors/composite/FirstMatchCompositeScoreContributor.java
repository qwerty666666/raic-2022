package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.ArrayList;
import java.util.List;

public class FirstMatchCompositeScoreContributor extends BaseCompositeScoreContributor {
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
