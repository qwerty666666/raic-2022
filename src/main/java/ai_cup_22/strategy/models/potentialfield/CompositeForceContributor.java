package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.List;

public class CompositeForceContributor implements ForceContributor {
    private final List<ForceContributor> contributors = new ArrayList<>();

    public CompositeForceContributor add(ForceContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    @Override
    public boolean shouldContribute(Position position) {
        return contributors.stream().anyMatch(c -> c.shouldContribute(position));
    }

    @Override
    public double getForce(Position position) {
        if (!shouldContribute(position)) {
            return 0;
        }

        return contributors.stream()
                .filter(contributor -> contributor.shouldContribute(position))
                .findFirst()
                .get()
                .getForce(position);
    }
}
