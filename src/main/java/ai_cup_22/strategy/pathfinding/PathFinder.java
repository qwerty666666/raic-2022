package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.List;

public interface PathFinder {
    Path findPath(Position startPosition, Position destination);
}
