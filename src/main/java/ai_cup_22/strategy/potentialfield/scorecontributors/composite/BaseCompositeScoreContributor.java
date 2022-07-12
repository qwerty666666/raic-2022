package ai_cup_22.strategy.potentialfield.scorecontributors.composite;

import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.DelegatingScoreContributor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseCompositeScoreContributor implements ScoreContributor {
    protected final List<ScoreContributor> contributors = new ArrayList<>();

    public BaseCompositeScoreContributor add(ScoreContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    public BaseCompositeScoreContributor add(Collection<? extends ScoreContributor> contributors) {
        this.contributors.addAll(contributors);
        return this;
    }

    public BaseCompositeScoreContributor add(Supplier<Boolean> condition, ScoreContributor contributor) {
        return add(new DelegatingScoreContributor(condition, contributor));
    }
}
