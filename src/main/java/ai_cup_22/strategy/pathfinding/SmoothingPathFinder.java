package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Rectangle;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.List;

public class SmoothingPathFinder implements PathFinder {
    private final PathFinder pathFinder;

    public SmoothingPathFinder(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    @Override
    public Path findPath(Position startPosition, Position destination) {
        var path = pathFinder.findPath(startPosition, destination);

        if (path == null || path.getScores().size() < 2) {
            return path;
        }

        return smoothPath(path);
    }

    private Path smoothPath(Path path) {
        var pathNodes = path.getScores();
        var pathRect = new Rectangle(pathNodes.get(0).getPosition(), pathNodes.get(pathNodes.size() - 1).getPosition())
                .increase(5);
        var unitRadius = World.getInstance().getConstants().getUnitRadius();
        var obstacles = World.getInstance().getObstacles().values().stream()
                .filter(obstacle -> pathRect.contains(obstacle.getCenter()))
                .map(obstacle -> obstacle.getCircle().enlarge(unitRadius))
                .toList();

        var newPath = new ArrayList<Score>();

        int from = 0;
        newPath.add(pathNodes.get(from));

        while (from < pathNodes.size() - 1) {
            int to = findFurtherDirectlyAccessiblePosition(pathNodes, obstacles, from);
            newPath.add(pathNodes.get(to));
            from = to;
        }

        return new Path(newPath);
    }

    private int findFurtherDirectlyAccessiblePosition(List<Score> path, List<Circle> obstacles, int from) {
        int left = from + 1;
        int right = path.size() - 1;

        while (left <= right) {
            int mid = (right + left) / 2;
            var line = new Line(path.get(from).getPosition(), path.get(mid).getPosition());

            if (obstacles.stream().anyMatch(obstacle -> obstacle.isIntersect(line))) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return Math.max(from + 1, left - 1);
    }
}
