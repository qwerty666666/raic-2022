package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class AStarPathFinder implements PathFinder {
    private final Graph graph;

    public AStarPathFinder(PotentialField potentialField) {
        this.graph = potentialField.getGraph();
    }

    @Override
    public Path findPath(Position startPosition, Position destination) {

        // add nodes to graph

        var isFromExisted = graph.getNodes().containsKey(startPosition);
        var from = graph.getNodes().computeIfAbsent(startPosition, p -> graph.addNode(startPosition));
        
        var isToExisted = graph.getNodes().containsKey(destination);
        var to = graph.getNodes().computeIfAbsent(destination, p -> graph.addNode(destination));

        // iterate over graph

        var visited = new HashSet<Node>();
        visited.add(from);

        var queue = new PriorityQueue<>(Comparator.comparingDouble(Node::getPriority));
        queue.add(from);

        while (!queue.isEmpty()) {
            var cur = queue.poll();

            if (cur == to) {
                break;
            }

            // do not use lambda for perf !!!
            for (var adj: cur.getAdjacent()) {
                if (!visited.contains(adj)) {
                    adj.setParent(cur);
                    adj.setPriority(cur.getPosition().getDistanceTo(adj.getPosition()) + to.getPosition().getDistanceTo(adj.getPosition()));
                    adj.setSteps(cur.getSteps() + 1);
                    queue.add(adj);
                    visited.add(adj);
                }
            }
        }

        // remove nodes from graph

        if (!isFromExisted) {
            graph.removeNode(from);
        }
        if (!isToExisted) {
            graph.removeNode(to);
        }

        // build path

        return Path.from(to, graph);
    }
}
