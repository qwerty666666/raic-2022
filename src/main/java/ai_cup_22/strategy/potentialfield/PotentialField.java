package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph;
import ai_cup_22.strategy.utils.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PotentialField {
    double STEP_SIZE = 1;

    double MIN_VALUE = -100;
    double MAX_VALUE = 100;
    double UNREACHABLE_VALUE = -10000000;

    Map<Position, Score> getScores();

    default Graph getGraph() {
        throw new UnsupportedOperationException();
    }

    default Position getCenter() {
        throw new UnsupportedOperationException();
    }

    default Circle getCircle() {
        throw new UnsupportedOperationException();
    }

    default double getScoreValue(Position position) {
        var scoresNear = getScoresAround(position).stream()
                .filter(score -> !score.isUnreachable())
                .collect(Collectors.toList());

        var sumDist = scoresNear.stream()
                .mapToDouble(score -> 1. / score.getPosition().getDistanceTo(position))
                .sum();
        double mul = 1. / sumDist;

        return scoresNear.stream()
                .mapToDouble(score -> 1. / score.getPosition().getDistanceTo(position) * mul * score.getScore())
                .sum();
    }

    default List<Score> getScoresAround(Position position) {
        var scoresNear = new ArrayList<Score>();
        var scores = getScores();

        var scoresList = new ArrayList<>(scores.values());

        // assume GRID_SIZE == 1
        if (getCircle().enlarge(-Math.sqrt(2.1)).contains(position)) {
            var pos = Collections.binarySearch(
                    scoresList,
                    new Score(new Position((int) position.getX(), position.getY())),
                    Comparator.comparingDouble((Score score) -> score.getPosition().getX())
                            .thenComparing(score -> score.getPosition().getY())
            );

            Score scoreNear;
            if (pos >= 0) {
                scoreNear = scoresList.get(pos);
            } else if (pos == -1) {
                scoreNear = scoresList.get(0);
            } else {
                scoreNear = scoresList.get(-pos - 2);
            }

            scoresNear.add(scoreNear);
            if (scores.containsKey(new Position(scoreNear.getPosition().getX(), scoreNear.getPosition().getY() + 1))) {
                scoresNear.add(scores.get(new Position(scoreNear.getPosition().getX(), scoreNear.getPosition().getY() + 1)));
            }
            if (scores.containsKey(new Position(scoreNear.getPosition().getX() + 1, scoreNear.getPosition().getY()))) {
                scoresNear.add(scores.get(new Position(scoreNear.getPosition().getX() + 1, scoreNear.getPosition().getY())));
            }
            if (scores.containsKey(new Position(scoreNear.getPosition().getX() + 1, scoreNear.getPosition().getY() + 1))) {
                scoresNear.add(scores.get(new Position(scoreNear.getPosition().getX() + 1, scoreNear.getPosition().getY() + 1)));
            }
        } else {
            var intersectionPoint = new Line(getCenter(), position).getIntersectionPointsAsRay(getCircle()).stream()
                    .min(Comparator.comparingDouble(p -> p.getSquareDistanceTo(position)))
                    .orElseThrow();
            int x = (int) MathUtils.restrict(
                    scoresList.get(0).getPosition().getX(),
                    scoresList.get(scoresList.size() - 1).getPosition().getX(),
                    (int) intersectionPoint.getX()
            );
            int y = (int) intersectionPoint.getY();
            if (scores.containsKey(new Position(x, y))) {
                scoresNear.add(scores.get(new Position(x, y)));
            } else {
                int i = 0;
                while (true) {
                    if (scores.containsKey(new Position(x, y + i))) {
                        scoresNear.add(scores.get(new Position(x, y + i)));
                        break;
                    }
                    if (scores.containsKey(new Position(x, y - i))) {
                        scoresNear.add(scores.get(new Position(x, y - i)));
                        break;
                    }
                    i++;
                }
            }
        }

        return scoresNear;
    }
}
