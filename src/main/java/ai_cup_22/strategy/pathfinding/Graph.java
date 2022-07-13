package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph {
    private final Map<Position, Node> nodes;

    public Graph(PotentialField potentialField) {
        nodes = buildGraph(potentialField.getScores());
    }

    private Map<Position, Node> buildGraph(List<Score> scores) {
        var graph = scores.stream()
                // remove nodes where we can't run
                .filter(score -> !score.isUnreachable())
                .collect(Collectors.toMap(Score::getPosition, Node::new));

        graph.forEach((pos, node) -> {
            for (var adj: node.getScore().getAdjacent()) {
                var adjNode = graph.get(adj.getPosition());
                // filter that score node is existed in given scores List
                if (adjNode != null) {
                    node.addAdjacent(adjNode);
                }
            }
        });

        return graph;
    }

    public Map<Position, Node> getNodes() {
        return nodes;
    }

    public Node addNode(Position position) {
        var node = new Node(new Score(position));

        nodes.values().stream()
                .sorted(Comparator.comparingDouble(a -> a.getPosition().getSquareDistanceTo(position)))
                .limit(4)
                .forEach(near -> {
                    near.addAdjacent(node);
                    node.addAdjacent(near);
                });

        return node;
    }

    public void removeNode(Node node) {
        node.getAdjacent().remove(node);
        nodes.remove(node.getPosition());
    }

    public static class Node {
        private Score score;
        private double priority;
        private Node parent;
        private List<Node> adjacent = new ArrayList<>();
        private double dist;
        private int steps;

        public Node(Score score) {
            this.score = score;
        }

        public Position getPosition() {
            return score.getPosition();
        }

        public double getPriority() {
            return priority;
        }

        public void setPriority(double priority) {
            this.priority = priority;
        }

        public int getSteps() {
            return steps;
        }

        public Node setSteps(int steps) {
            this.steps = steps;
            return this;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getAdjacent() {
            return adjacent;
        }

        public void addAdjacent(Node adjacent) {
            this.adjacent.add(adjacent);
        }

        public double getDist() {
            return dist;
        }

        public void setDist(double dist) {
            this.dist = dist;
        }

        public Score getScore() {
            return score;
        }

        public double getScoreValue() {
            return score.getScore();
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
