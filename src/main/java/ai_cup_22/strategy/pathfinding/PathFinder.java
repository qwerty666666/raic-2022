package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;

public interface PathFinder {
    Path findPath(Position startPosition, Position destination);
}
