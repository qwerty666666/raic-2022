package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Rectangle;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.ArrayList;
import java.util.List;

public class SmoothingPathFinder implements PathFinder {
    private final PathFinder pathFinder;

    public SmoothingPathFinder(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    @Override
    public List<Position> findPath(PotentialField potentialField, Position startPosition, Position destination) {
        var path = pathFinder.findPath(potentialField, startPosition, destination);

        if (path == null || path.size() < 2) {
            return path;
        }

        return smoothPath(path);
    }

    private List<Position> smoothPath(List<Position> path) {
        var pathRect = new Rectangle(path.get(0), path.get(path.size() - 1)).increase(5);
        var unitRadius = World.getInstance().getConstants().getUnitRadius();
        var obstacles = World.getInstance().getObstacles().values().stream()
                .filter(obstacle -> pathRect.contains(obstacle.getCenter()))
                .map(obstacle -> obstacle.getCircle().enlarge(unitRadius))
                .toList();

        var newPath = new ArrayList<Position>();

        int from = 0;
        newPath.add(path.get(from));

        while (from < path.size() - 1) {
            int to = findFurtherDirectlyAccessiblePosition(path, obstacles, from);
            newPath.add(path.get(to));
            from = to;
        }

        return newPath;
    }

    private int findFurtherDirectlyAccessiblePosition(List<Position> path, List<Circle> obstacles, int from) {
        int left = from + 1;
        int right = path.size() - 1;

        while (left <= right) {
            int mid = (right + left) / 2;
            var line = new Line(path.get(from), path.get(mid));

            if (obstacles.stream().anyMatch(obstacle -> obstacle.isIntersect(line))) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return Math.max(from + 1, left - 1);
    }
}
