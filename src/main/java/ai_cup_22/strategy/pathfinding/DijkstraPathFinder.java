package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DijkstraPathFinder implements PathFinder {
    public static final DistanceFunction NON_STATIC_SCORE_DIST_FUNCTION = (from, to) -> to.getScore().getNonStaticScore();
    public static final Comparator<Double> MAX_DIST_BEST_DIST_FUNCTION = (d1, d2) -> Double.compare(d2, d1);

    private final Graph graph;
    private final Position startPosition;
    private final DistanceFunction distFunction;
    private final Comparator<Double> distanceComparator;

    public DijkstraPathFinder(PotentialField potentialField, Position startPosition, DistanceFunction distFunction,
            Comparator<Double> distanceComparator) {
        this.graph = potentialField.getGraph();
        this.startPosition = startPosition;
        this.distFunction = distFunction;
        this.distanceComparator = distanceComparator;

        calculateAllDistances();
    }

    public static DijkstraPathFinder minThreatPathFinder(PotentialField potentialField) {
        return new DijkstraPathFinder(potentialField, potentialField.getCenter(), NON_STATIC_SCORE_DIST_FUNCTION,
                MAX_DIST_BEST_DIST_FUNCTION);
    }

    private void calculateAllDistances() {

        // add nodes to graph

        var isFromExisted = graph.getNodes().containsKey(startPosition);
        var from = graph.getNodes().computeIfAbsent(startPosition, p -> graph.addNode(startPosition));

        // iterate over graph

        var queue = new PriorityQueue<Node>((a, b) -> distanceComparator.compare(a.getDist(), b.getDist()));
        queue.add(from);

        var visited = new HashSet<Node>();
        visited.add(from);

        while (!queue.isEmpty()) {
            var cur = queue.poll();

            cur.getAdjacent().forEach(adj -> {
                boolean shouldUpdate;

                if (visited.contains(adj)) {
                    // we need this because we can search for furthest path
                    if (adj.getParent() != cur) {
                        shouldUpdate = false;
                    } else {
                        var cmp = distanceComparator.compare(adj.getDist(), adj.getDist() + distFunction.getDistanceTo(cur, adj));

                        // if distances are the same then choose path with the fewer steps
                        if (cmp == 0) {
                            cmp = cur.getSteps() + 1 < adj.getSteps() ? -1 : 1;
                        }

                        shouldUpdate = cmp < 0;
                    }
                } else {
                    shouldUpdate = true;
                }

                if (shouldUpdate) {
                    adj.setDist(adj.getDist() + distFunction.getDistanceTo(cur, adj));
                    adj.setParent(cur);
                    adj.setSteps(cur.getSteps() + 1);

                    queue.add(adj);

                    visited.add(adj);
                }
            });
        }

        // remove nodes from graph

        if (!isFromExisted) {
            graph.removeNode(from);
        }
    }

    @Override
    public Path findPath(Position startPosition, Position destination) {
        return Path.from(getNearestNode(destination));
    }

    private Node getNearestNode(Position p) {
        return graph.getNodes().values().stream()
                .min(Comparator.comparingDouble(node -> node.getPosition().getSquareDistanceTo(p)))
                .orElseThrow();
    }

    public interface DistanceFunction {
        double getDistanceTo(Node from, Node to);
    }
}
