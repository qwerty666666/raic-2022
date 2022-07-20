package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.ConstantInCircleScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StaticPotentialField implements PotentialField {
    public static final double TREE_MAX_INFLUENCE_RADIUS = 1.5;
    public static final double TREE_MIN_SCORE = -10;

    private double startCoord;
    private double stepSize = PotentialField.STEP_SIZE;
    private int gridSize;
    private Score[][] scores;
    private Graph graph;

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
                scores[x][y] = new Score(coordToPosition(x, y));
            }
        }
    }

    public void fillStaticData(World world) {
        var unitRadius = World.getInstance().getConstants().getUnitRadius();

        // add trees penalty
        world.getObstacles().forEach((id, obstacle) -> {
            var circle = obstacle.getCircle().enlarge(unitRadius);
            var influenceRadius = circle.getRadius() + TREE_MAX_INFLUENCE_RADIUS;

            var obstaclesContributor = new FirstMatchCompositeScoreContributor("Tree", true)
                    .add(new ConstantInCircleScoreContributor(circle, PotentialField.UNREACHABLE_VALUE))
                    .add(new LinearScoreContributor(circle.getCenter(), TREE_MIN_SCORE, 0, circle.getRadius(), influenceRadius));

            getScoresInCircle(new Circle(circle.getCenter(), influenceRadius)).values()
                    .forEach(obstaclesContributor::contribute);
        });

        // set initial scores
        for (var col: scores) {
            for (var score: col) {
                score.setInitialScore(score.getScore());
            }
        }
    }

    public void buildGraph(int step, int maxSteps) {
        if (step == 0) {
            var nodes = new HashMap<Position, Node>();

            for (int x = 0; x < gridSize; x++) {
                for (int y = step; y < gridSize; y++) {
                    nodes.put(coordToPosition(x, y), new Node(scores[x][y]));
                }
            }

            this.graph = new Graph(this, nodes);
        } else {
            var nodes = this.graph.getNodes();
            int stepSize = gridSize / (maxSteps - 1);
            int start = (step - 1) * stepSize;
            int end = step == maxSteps - 1 ? gridSize : start + stepSize;

            // add scores adjacent graph
            for (int x = 0; x < gridSize; x++) {
                for (int y = start; y < end; y++) {
                    var node = nodes.get(coordToPosition(x, y));

                    if (x > 0) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x - 1, y)));
                    }
                    if (x < gridSize - 1) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x + 1, y)));
                    }
                    if (y > 0) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x, y - 1)));
                    }
                    if (y < gridSize - 1) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x, y + 1)));
                    }
                    if (x > 0 && y > 0) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x - 1, y - 1)));
                    }
                    if (x > 0 && y < gridSize - 1) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x - 1, y + 1)));
                    }
                    if (x < gridSize - 1 && y > 0) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x + 1, y - 1)));
                    }
                    if (x < gridSize - 1 && y < gridSize - 1) {
                        node.addStaticAdjacent(nodes.get(coordToPosition(x + 1, y + 1)));
                    }
                }
            }
        }
    }

    private Position coordToPosition(int x, int y) {
        return new Position(stepSize * x + startCoord, stepSize * y + startCoord);
    }

    private int getIndex(double coord) {
        return (int) Math.max(0, Math.min(gridSize, (coord - startCoord) / stepSize));
    }

    /**
     * Scores must be sorted for binary search
     */
    public Map<Position, Score> getScoresInCircle(Circle circle) {
        var scores = new LinkedHashMap<Position, Score>();

        int minX = getIndex(circle.getCenter().getX() - circle.getRadius());
        int maxX = getIndex(circle.getCenter().getX() + circle.getRadius());
        int minY = getIndex(circle.getCenter().getY() - circle.getRadius());
        int maxY = getIndex(circle.getCenter().getY() + circle.getRadius());

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

    @Override
    public Graph getGraph() {
        return graph;
    }
}
