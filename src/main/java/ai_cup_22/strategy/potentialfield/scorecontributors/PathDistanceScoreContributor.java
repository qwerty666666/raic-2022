package ai_cup_22.strategy.potentialfield.scorecontributors;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class PathDistanceScoreContributor implements ScoreContributor {
    private final Position source;
    private final PotentialField potentialField;
    private final double maxValue;
    private final double minValue;
    private final double maxDistance;
    private final Map<Score, Double> distances = new HashMap<>();

    public PathDistanceScoreContributor(Position source, PotentialField potentialField, double maxValue, double minValue, double maxDistance) {
        this.source = source;
        this.potentialField = potentialField;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.maxDistance = maxDistance;

        calculateDistances();
    }

    private void calculateDistances() {
        var existedNodes = potentialField.getScores().values().stream()
                .filter(score -> score.getScore() != PotentialField.UNREACHABLE_VALUE)
                .collect(Collectors.toSet());

        var start = getNearestScoreField();

        var used = new HashSet<Score>();
        used.add(start);
        distances.put(start, source.getDistanceTo(start.getPosition()));

        var queue = new LinkedList<Score>();
        queue.add(start);

        while (!queue.isEmpty()) {
            var cur = queue.poll();
            cur.getAdjacent().stream()
                    .filter(existedNodes::contains)
                    .forEach(adj -> {
                        var dist = distances.get(cur) + cur.getPosition().getDistanceTo(adj.getPosition());
                        if (!used.contains(adj) || distances.get(adj) > dist) {
                            used.add(adj);
                            distances.put(adj, dist);
                            queue.add(adj);
                        }
                    });
        }
    }

    private Score getNearestScoreField() {
        return potentialField.getScores().values().stream()
                .min(Comparator.comparingDouble(s -> s.getPosition().getDistanceTo(source)))
                .orElse(null);
    }

    @Override
    public boolean shouldContribute(Score score) {
        return distances.containsKey(score) && distances.get(score) < maxDistance;
    }

    @Override
    public double getScoreValue(Score score) {
        if (!shouldContribute(score)) {
            return 0;
        }

        return maxValue - (distances.get(score) / maxDistance) * (maxValue - minValue);
    }
}
