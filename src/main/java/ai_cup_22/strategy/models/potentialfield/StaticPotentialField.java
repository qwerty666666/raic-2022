package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaticPotentialField implements PotentialField {
    private double startX;
    private double startY;
    private double stepSize = PotentialField.STEP_SIZE;
    private Map<Double, Map<Double, Score>> scores;
    private double[] yCoordinates;
    private double[] xCoordinates;
    private int gridSize;

    public StaticPotentialField(World world) {
        initValues();
        fillStaticData(world);
    }

    private void initValues() {
        var initRadius = World.getInstance().getConstants().getInitialZoneRadius();
        gridSize = (int) (initRadius * 2 / PotentialField.STEP_SIZE);

        startX = -initRadius;
        startY = -initRadius;

        xCoordinates = new double[gridSize];
        yCoordinates = new double[gridSize];
        for (int i = 0; i < gridSize; i++) {
            xCoordinates[i] = yCoordinates[i] = -initRadius + i * stepSize;
        }

        scores = new LinkedHashMap<>(gridSize);
        for (var x: xCoordinates) {
            var row = scores.computeIfAbsent(x, xx -> new LinkedHashMap<>(gridSize));

            for (var y: yCoordinates) {
                row.put(y, new Score(new Position(x, y)));
            }
        }
    }

    private void fillStaticData(World world) {
        world.getObstacles().forEach((id, obstacle) -> {
            var circle = obstacle.getCircle();
            var influenceRadius = circle.getRadius() + 2.5;

            var obstaclesContributor = new CompositeScoreContributor()
//                    .add(new ConstantOutCircleForceContributor(circle.enlargeToRadius(radius), 0))
                    .add(new ConstantInCircleScoreContributor(circle, PotentialField.UNREACHABLE_VALUE))
                    .add(new LinearScoreContributor(circle.getCenter(), -50, -10, circle.getRadius(), influenceRadius));

            getScoresInCircle(new Circle(circle.getCenter(), influenceRadius))
                    .forEach(obstaclesContributor::contribute);
        });
    }

    public List<Score> getScoresInCircle(Circle circle) {
        var list = new ArrayList<Score>();

        var bottom = circle.getCenter().getY() - circle.getRadius();
        var bottomInd = Arrays.binarySearch(yCoordinates, bottom);
        bottomInd = bottomInd >= 0 ? bottomInd : Math.min(gridSize - 1, -bottomInd - 1);

        var top = circle.getCenter().getY() + circle.getRadius();
        var topInd = Arrays.binarySearch(yCoordinates, top);
        topInd = topInd >= 0 ? topInd : Math.min(gridSize - 1, -topInd - 1);

        var left = circle.getCenter().getX() - circle.getRadius();
        var leftInd = Arrays.binarySearch(xCoordinates, left);
        leftInd = leftInd >= 0 ? leftInd : Math.min(gridSize - 1, -leftInd - 1);

        var right = circle.getCenter().getX() + circle.getRadius();
        var rightInd = Arrays.binarySearch(xCoordinates, right);
        rightInd = rightInd >= 0 ? rightInd : Math.min(gridSize - 1, -rightInd - 1);

        for (int x = leftInd; x <= rightInd; x++) {
            for (int y = bottomInd; y <= topInd; y++) {
                var score = scores.get(xCoordinates[x]).get(yCoordinates[y]);
                if (circle.contains(score.getPosition())) {
                    list.add(score);
                }
            }
        }

        return list;
    }

    @Override
    public List<Score> getScores() {
        return scores.values().stream()
                .flatMap(s -> s.values().stream())
                .toList();
    }
}
