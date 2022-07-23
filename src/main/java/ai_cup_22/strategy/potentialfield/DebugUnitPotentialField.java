package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class DebugUnitPotentialField extends UnitPotentialField {
    public DebugUnitPotentialField(Unit unit) {
        super(unit.getPosition());

        this.scores = this.scores.values().stream()
                .map(score -> {
                    try {
                        return score.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(Score::getPosition, score -> score, (x, y) -> x, LinkedHashMap::new));
    }

    @Override
    public Graph getGraph() {
        if (graph == null) {
            graph = super.getGraph().clone();
        }
        return graph;
    }
}
