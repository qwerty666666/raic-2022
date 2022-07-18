package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph {
    private final PotentialField potentialField;
    private final Map<Position, Node> nodes;

    public Graph(PotentialField potentialField) {
        this.potentialField = potentialField;
        nodes = buildGraph(potentialField.getScores().values());
    }

    private Map<Position, Node> buildGraph(Collection<Score> scores) {
        var graph = scores.stream()
                // remove nodes where we can't run
                .filter(score -> !score.isUnreachable())
                .collect(Collectors.toMap(Score::getPosition, Node::new, (x, y) -> y, LinkedHashMap::new));

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

        getNearestReachableNodes(position)
                .forEach(near -> {
                    near.addAdjacent(node);
                    node.addAdjacent(near);
                });

        return node;
    }

    public Node getNearestNode(Position position) {
        return getNearestReachableNodes(position).stream()
                .min(Comparator.comparingDouble(node -> node.getPosition().getSquareDistanceTo(position)))
                .orElseThrow();
    }

    private List<Node> getNearestReachableNodes(Position position) {
        // if nearest score is unreachable, we just find the nearest reachable node
        // from it by BFS
        var scoresAround = potentialField.getScoresAround(position);
        if (scoresAround.stream().allMatch(Score::isUnreachable)) {
            var queue = new LinkedList<Score>();
            queue.add(scoresAround.get(0));

            var used = new HashSet<Score>();
            used.add(scoresAround.get(0));

            while (!queue.isEmpty()) {
                var cur = queue.poll();

                if (!cur.isUnreachable() && nodes.containsKey(cur.getPosition())) {
                    scoresAround = List.of(cur);
                    break;
                }

                cur.getAdjacent().forEach(adj -> {
                    if (!used.contains(adj)) {
                        used.add(adj);
                        queue.add(adj);
                    }
                });
            }
        }
        return scoresAround.stream()
                .filter(score -> !score.isUnreachable())
                .map(score -> nodes.get(score.getPosition()))
                .collect(Collectors.toList());
    }

    public void removeNode(Node node) {
        node.getAdjacent().forEach(adj -> adj.getAdjacent().remove(node));
        nodes.remove(node.getPosition());
    }

    public static class Node {
        private Score score;
        private double priority;
        private double threatSumOnPath;
        private Node parent;
        private List<Node> adjacent = new ArrayList<>();
        private double dist;
        private int stepsUnderThreat;
        private int steps;

        public Node(Score score) {
            this.score = score;
        }

        public Node setThreatSumOnPath(double threatSumOnPath) {
            this.threatSumOnPath = threatSumOnPath;
            return this;
        }

        public int getStepsUnderThreat() {
            return stepsUnderThreat;
        }

        public Node setStepsUnderThreat(int stepsUnderThreat) {
            this.stepsUnderThreat = stepsUnderThreat;
            return this;
        }

        public double getThreatSumOnPath() {
            return threatSumOnPath;
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
            return (obj instanceof Node) && score.equals(((Node)obj).score);
        }
    }
}
