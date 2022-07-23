package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.scorecontributors.TreeScoreContributor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StaticPotentialField implements PotentialField {
    private double startCoord;
    private double stepSize = PotentialField.STEP_SIZE;
    private int gridSize;
    private Score[][] scores;
    private StaticGraph graph = new StaticGraph(this);

    private Map<Position, Score> allScores;

    public StaticPotentialField(World world) {
        initValues();
//        fillStaticData(world);
//        buildGraph();
    }

    private void initValues() {
        var initRadius = World.getInstance().getConstants().getInitialZoneRadius();
        gridSize = (int) (initRadius * 2 / PotentialField.STEP_SIZE);

        startCoord = -initRadius;

        // init scores
        scores = new Score[gridSize][gridSize];
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                scores[x][y] = new Score(coordToPosition(x, y), x, y);
            }
        }
    }

    public void fillStaticData(World world) {
        // add trees penalty
        world.getObstacles().forEach((id, obstacle) -> {
            var circle = obstacle.getCircle().enlarge(Constants.USER_RADIUS + Constants.PF_TREE_DIST);
            var scoreContributor = new TreeScoreContributor(obstacle);
            getScoresInCircle(circle).values()
                    .forEach(scoreContributor::contribute);
        });

        // set initial scores
        for (var col: scores) {
            for (var score: col) {
                score.setInitialScore(score.getScore());
            }
        }
    }

    private Position coordToPosition(int x, int y) {
        return new Position(stepSize * x + startCoord, stepSize * y + startCoord);
    }

    public int getIndexByCoord(double coord) {
        return (int) Math.max(0, Math.min(gridSize, (coord - startCoord) / stepSize));
    }

    public Score getByIndex(int x, int y) {
        return scores[x][y];
    }

    /**
     * Scores must be sorted for binary search
     */
    public Map<Position, Score> getScoresInCircle(Circle circle) {
        var scores = new LinkedHashMap<Position, Score>();

        int minX = getIndexByCoord(circle.getCenter().getX() - circle.getRadius());
        int maxX = getIndexByCoord(circle.getCenter().getX() + circle.getRadius());
        int minY = getIndexByCoord(circle.getCenter().getY() - circle.getRadius());
        int maxY = getIndexByCoord(circle.getCenter().getY() + circle.getRadius());

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                var score = this.scores[x][y];

                if (circle.contains(score.getPosition())) {
                    scores.put(score.getPosition(), score);
                }
            }
        }

        return scores;
    }

    @Override
    public Map<Position, Score> getScores() {
        if (allScores == null) {
            allScores = Arrays.stream(scores)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toMap(Score::getPosition, s -> s, (x, y) -> y, LinkedHashMap::new));
        }
        return allScores;
    }

    public StaticGraph getStaticGraph() {
        return graph;
    }

    @Override
    public Score getScoreByIndex(int x, int y) {
        if (x < 0 || x >= gridSize) {
            return null;
        }
        if (y < 0 || y >= gridSize) {
            return null;
        }
        return scores[x][y];
    }
}
