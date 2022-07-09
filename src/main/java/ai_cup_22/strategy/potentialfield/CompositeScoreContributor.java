package ai_cup_22.strategy.potentialfield;

import java.util.ArrayList;
import java.util.List;

public class CompositeScoreContributor implements ScoreContributor {
    private final List<ScoreContributor> contributors = new ArrayList<>();

    public CompositeScoreContributor add(ScoreContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    @Override
    public boolean shouldContribute(Score score) {
        return contributors.stream().anyMatch(c -> c.shouldContribute(score));
    }

    @Override
    public void contribute(Score score) {
        if (!shouldContribute(score)) {
            return;
        }

        contributors.stream()
                .filter(contributor -> contributor.shouldContribute(score))
                .findFirst()
                .get()
                .contribute(score);
    }
}
