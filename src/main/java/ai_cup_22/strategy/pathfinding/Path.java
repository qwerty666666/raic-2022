package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
    private final List<Score> path;

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
}
