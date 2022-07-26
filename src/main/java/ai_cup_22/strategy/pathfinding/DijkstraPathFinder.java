package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DijkstraPathFinder implements PathFinder {
    private final Graph graph;
    private final Position startPosition;

    public DijkstraPathFinder(PotentialField potentialField, Position startPosition) {
        this.graph = potentialField.getGraph();
        this.startPosition = startPosition;
    }

    public void calculateAllDistances() {

        // add nodes to graph

        var isFromExisted = graph.getNodes().containsKey(startPosition);
        var from = graph.getNodes().computeIfAbsent(startPosition, p -> graph.addNode(startPosition));

        // iterate over graph

        var minScoreValue = getMinScoreValue(graph);

        var queue = new PriorityQueue<>(
//                Comparator.comparingDouble(Node::getPriority)
//                        .thenComparing(Node::getSteps)
                (Node n1, Node n2) -> {
                    if (n1.getPriority() != n2.getPriority()) {
                        return n1.getPriority() < n2.getPriority() ? -1 : 1;
                    }
                    return n1.getSteps() - n2.getSteps();
                }
        );
        queue.add(from);

        var visited = new HashSet<Node>(graph.getNodes().size());
        visited.add(from);

        while (!queue.isEmpty()) {
            var cur = queue.poll();

            // do not use lambda for perf !!!
            for (var adj: cur.getAdjacent()) {
                if (cur.getParent() == adj) {
                    // OK, in theory in this case cur.parent priority will be lower
                    // than adj.priority + dist, but in practise it is wrong sometimes
                    // for big double.
                    continue;
                }

                boolean shouldUpdate;

                var newPriority = Math.max(0, cur.getPriority() + (-adj.getScoreValue() - minScoreValue));

                if (visited.contains(adj)) {
                    var cmp = Double.compare(adj.getPriority(), newPriority);

                    // if distances are the same then choose path with the fewer steps
                    if (cmp == 0) {
                        cmp = cur.getSteps() + 1 < adj.getSteps() ? 1 : -1;
                    }

                    shouldUpdate = cmp > 0;
                } else {
                    shouldUpdate = true;
                }

                if (shouldUpdate) {
                    adj.setDist(cur.getDist() + cur.getPosition().getDistanceTo(adj.getPosition()));
                    adj.setParent(cur);
                    adj.setSteps(cur.getSteps() + 1);
                    adj.setPriority(newPriority);
                    adj.setThreatSumOnPath(cur.getThreatSumOnPath() + adj.getScore().getThreatScore());
                    adj.setStepsUnderThreat(cur.getStepsUnderThreat() + (adj.getScore().getThreatScore() < 0 ? 1 : 0));

                    if (visited.contains(adj)) {
                        queue.remove(adj);
                    } else {
                        visited.add(adj);
                    }

                    queue.add(adj);
                }
            }
        }

        // remove nodes from graph

        if (!isFromExisted) {
            graph.removeNode(from);
        }

        // set threat to priority

        for (var node: graph.getNodes().values()) {
            node.setPriority(-1 * (node.getPriority() + node.getSteps() * minScoreValue));
        }
    }

    @Override
    public Path findPath(Position startPosition, Position destination) {
        return Path.from(graph.getNearestNode(destination), graph);
    }

    private double getMinScoreValue(Graph graph) {
        double min = 0;
        for (var node: graph.getNodes().values()) {
            var score = node.getScoreValue();
            if (score > 0) {
                min -= score;
            }
        }
        return min;
    }
}
