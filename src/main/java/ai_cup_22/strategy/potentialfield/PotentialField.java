package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.List;

public interface PotentialField {
    double STEP_SIZE = 1;

    double MIN_VALUE = -100;
    double MAX_VALUE = 100;
    double UNREACHABLE_VALUE = -10000000;

    List<Score> getScores();

    default Graph getGraph() {
        throw new UnsupportedOperationException();
    }

    default Position getCenter() {
        throw new UnsupportedOperationException();
    }
}
