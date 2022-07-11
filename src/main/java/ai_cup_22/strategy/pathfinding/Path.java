package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.List;

public class Path {
    private final List<Score> path;

    public Path(List<Score> path) {
        this.path = path;
    }

    public List<Position> getPathPositions() {
        return path.stream().map(Score::getPosition).toList();
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
}
