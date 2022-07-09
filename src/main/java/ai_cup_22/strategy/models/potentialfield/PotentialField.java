package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import java.util.List;
import java.util.Map;

public interface PotentialField {
    double FIELD_RADIUS = 30;
    double STEP_SIZE = 1;

    double MIN_VALUE = -100;
    double MAX_VALUE = 100;
    double UNREACHABLE_VALUE = -10000000;

    Map<Position, Double> getForces();
}
