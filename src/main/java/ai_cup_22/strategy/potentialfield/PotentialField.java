package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    default double getScoreValue(Position position) {
        var scoresNear = getScores().stream()
                .filter(score -> !score.isUnreachable())
                .sorted(Comparator.comparingDouble(score -> score.getPosition().getDistanceTo(position)))
                .limit(4)
                .collect(Collectors.toList());

        var sumDist = scoresNear.stream()
                .mapToDouble(score -> 1. / score.getPosition().getDistanceTo(position))
                .sum();
        double mul = 1. / sumDist;

        return scoresNear.stream()
                .mapToDouble(score -> 1. / score.getPosition().getDistanceTo(position) * mul * score.getScore())
                .sum();
    }
}
