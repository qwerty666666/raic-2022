package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Rectangle;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
    private List<Score> path;

    public Path(List<Score> path) {
        this.path = path;
    }

    public List<Position> getPathPositions() {
        return path.stream().map(Score::getPosition).collect(Collectors.toList());
    }

    public List<Score> getScores() {
        return path;
    }

    public double getDistance() {
        double dist = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            dist += path.get(i).getPosition().getDistanceTo(path.get(i + 1).getPosition());
        }

        return dist;
    }

    public static Path from(Node to, Graph graph) {
        if (to.getParent() == null) {
            var pos = to.getPosition();
            to = graph.getNodes().values().stream()
                    .filter(node -> node.getParent() != null)
                    .min(Comparator.comparingDouble(node -> node.getPosition().getSquareDistanceTo(pos)))
                    .orElseThrow();
        }

        var path = new ArrayList<Score>();
        var tmp = to;

        while (tmp != null) {
            path.add(tmp.getScore());
            tmp = tmp.getParent();
        }

        Collections.reverse(path);

        return new Path(path);
    }

    public Path smooth() {
        smooth(path.size() - 1);
        return this;
    }

    public Path smooth(int maxStep) {
        if (path.size() < 2) {
            return this;
        }

        var pathRect = new Rectangle(path.get(0).getPosition(), path.get(path.size() - 1).getPosition())
                .increase(5);
        var unitRadius = World.getInstance().getConstants().getUnitRadius();
        var obstacles = World.getInstance().getObstacles().values().stream()
                .filter(obstacle -> pathRect.contains(obstacle.getCenter()))
                .map(obstacle -> obstacle.getCircle().enlarge(unitRadius))
                .collect(Collectors.toList());

        var newPath = new ArrayList<Score>();

        int from = 0;
        newPath.add(path.get(from));

        while (from < path.size() - 1) {
            int to = findFurtherDirectlyAccessiblePosition(path, obstacles, from, Math.min(path.size() - 1, from + maxStep));
            newPath.add(path.get(to));
            from = to;
        }

        this.path = newPath;

        return this;
    }

    private int findFurtherDirectlyAccessiblePosition(List<Score> path, List<Circle> obstacles, int from, int maxRight) {
        int left = from + 1;
        int right = maxRight;

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
