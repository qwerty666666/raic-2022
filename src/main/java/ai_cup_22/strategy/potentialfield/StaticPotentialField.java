package ai_cup_22.strategy.potentialfield;

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

        // init scores
        scores = new LinkedHashMap<>(gridSize);
        for (var x: xCoordinates) {
            var row = scores.computeIfAbsent(x, xx -> new LinkedHashMap<>(gridSize));

            for (var y: yCoordinates) {
                row.put(y, new Score(new Position(x, y)));
            }
        }
    }

    private Score getScoreAtInd(int x, int y) {
        return scores.get(xCoordinates[x]).get(yCoordinates[y]);
    }

    private void fillStaticData(World world) {
        var unitRadius = World.getInstance().getConstants().getUnitRadius();

        // add trees penalty
        world.getObstacles().forEach((id, obstacle) -> {
            var circle = obstacle.getCircle().enlarge(unitRadius);
            var influenceRadius = circle.getRadius() + 2.5;

            var obstaclesContributor = new CompositeScoreContributor()
//                    .add(new ConstantOutCircleForceContributor(circle.enlargeToRadius(radius), 0))
                    .add(new ConstantInCircleScoreContributor(circle, PotentialField.UNREACHABLE_VALUE))
                    .add(new LinearScoreContributor(circle.getCenter(), -50, -10, circle.getRadius(), influenceRadius));

            getScoresInCircle(new Circle(circle.getCenter(), influenceRadius))
                    .forEach(obstaclesContributor::contribute);
        });

        // set initial scores
        scores.values().stream()
                .flatMap(s -> s.values().stream())
                .forEach(score -> score.setInitialScore(score.getScore()));
    }

    public void buildGraph() {
        // add scores adjacent graph
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                var score = getScoreAtInd(x, y);
                if (x > 0) {
                    score.addAdjacent(getScoreAtInd(x - 1, y));
                }
                if (x < gridSize - 1) {
                    score.addAdjacent(getScoreAtInd(x + 1, y));
                }
                if (y > 0) {
                    score.addAdjacent(getScoreAtInd(x, y - 1));
                }
                if (y < gridSize - 1) {
                    score.addAdjacent(getScoreAtInd(x, y + 1));
                }
                if (x > 0 && y > 0) {
                    score.addAdjacent(getScoreAtInd(x - 1, y - 1));
                }
                if (x > 0 && y < gridSize - 1) {
                    score.addAdjacent(getScoreAtInd(x - 1, y + 1));
                }
                if (x < gridSize - 1 && y > 0) {
                    score.addAdjacent(getScoreAtInd(x + 1, y - 1));
                }
                if (x < gridSize - 1 && y < gridSize - 1) {
                    score.addAdjacent(getScoreAtInd(x + 1, y + 1));
                }
            }
        }
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
