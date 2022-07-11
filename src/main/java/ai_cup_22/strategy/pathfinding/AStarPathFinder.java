package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class AStarPathFinder implements PathFinder {
    private final PotentialField potentialField;
    private final Map<Position, Node> graph;

    public AStarPathFinder(PotentialField potentialField) {
        this.potentialField = potentialField;
        graph = buildGraph(potentialField.getScores());
    }

    @Override
    public Path findPath(Position startPosition, Position destination) {
        var from = graph.computeIfAbsent(startPosition, p -> addNodeToGraph(graph, startPosition));
        var to = graph.computeIfAbsent(destination, p -> addNodeToGraph(graph, destination));

        var visited = new HashSet<Node>();
        visited.add(from);

        var queue = new PriorityQueue<Node>(Comparator.comparingDouble(a -> a.priority));
        queue.add(from);

        while (!queue.isEmpty()) {
            var cur = queue.poll();

            if (cur == to) {
                break;
            }

            cur.adjacent.forEach(adj -> {
                if (!visited.contains(adj)) {
                    adj.parent = cur;
                    adj.priority = cur.getPosition().getDistanceTo(adj.getPosition()) + to.getPosition().getDistanceTo(adj.getPosition());
                    queue.add(adj);
                    visited.add(adj);
                }
            });
        }

        return buildPath(from, to);
    }

    private Path buildPath(Node from, Node to) {
        if (to.parent == null) {
            return null;
        }

        var path = new ArrayList<Score>();
        var tmp = to;

        while (tmp != from) {
            path.add(tmp.score);
            tmp = tmp.parent;
        }

        Collections.reverse(path);

        return new Path(path);
    }

    private Map<Position, Node> buildGraph(List<Score> scores) {
        var graph = scores.stream()
                // remove nodes where we can't run
                .filter(score -> score.getScore() != PotentialField.UNREACHABLE_VALUE)
                .collect(Collectors.toMap(Score::getPosition, Node::new));

        graph.forEach((pos, node) -> {
            node.score.getAdjacent().stream()
                    .map(adj -> graph.get(adj.getPosition()))
                    // filter that score node is existed in given scores List
                    .filter(Objects::nonNull)
                    .forEach(adj -> node.adjacent.add(adj));
        });

        return graph;
    }

    private Node findNearest(Map<Position, Node> graph, Position target) {
        return graph.values().stream()
                .min(Comparator.comparingDouble(node -> node.getPosition().getDistanceTo(target)))
                .orElse(null);
    }

    private Node addNodeToGraph(Map<Position, Node> graph, Position position) {
        var node = new Node(new Score(position));

        graph.values().stream()
                .sorted(Comparator.comparingDouble(a -> a.getPosition().getSquareDistanceTo(position)))
                .limit(4)
                .forEach(near -> {
                    near.adjacent.add(node);
                    node.adjacent.add(near);
                });

        return node;
    }

    private static class Node {
        public Score score;
        public double priority;
        public Node parent;
        public List<Node> adjacent = new ArrayList<>();

        public Node(Score score) {
            this.score = score;
        }

        public Position getPosition() {
            return score.getPosition();
        }

        @Override
        public int hashCode() {
            return score.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Node n && score.equals(n.score);
        }
    }
}
