package ai_cup_22.strategy.pathfinding;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph implements Cloneable {
    private final PotentialField potentialField;
    private final Map<Position, Node> nodes;

    public Graph(PotentialField potentialField) {
        this.potentialField = potentialField;
        nodes = buildGraph(potentialField.getScores());
    }

    private Graph(PotentialField potentialField, Map<Position, Node> nodes) {
        this.potentialField = potentialField;
        this.nodes = nodes;
    }

    private Map<Position, Node> buildGraph(Map<Position, Score> scores) {
        var globalGraph = World.getInstance().getStaticPotentialField().getStaticGraph();

        return scores.entrySet().stream()
                // remove nodes where we can't run
                .filter(e -> !e.getValue().isUnreachable())
                .map(e -> {
                    var node = globalGraph.getOrCreateNode(e.getKey());

                    node.refresh();

                    for (var adj: node.getStaticAdjacent()) {
                        // filter that score node is existed in given scores List
                        if (scores.containsKey(adj.getPosition())) {
                            node.addAdjacent(adj);
                        }
                    }

                    return node;
                })
                .collect(Collectors.toMap(Node::getPosition, node -> node));
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

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        var adj = potentialField.getScoreByIndex(cur.getX() + x, cur.getY() + y);
                        if (adj != null && !used.contains(adj)) {
                            used.add(adj);
                            queue.add(adj);
                        }
                    }
                }
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

    public PotentialField getPotentialField() {
        return potentialField;
    }

    public Graph clone() {
        var clones = nodes.values().stream()
                .map(node -> {
                    try {
                        return node.clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(Node::getPosition, node -> node));

        for (var node: clones.values()) {
            if (node.getParent() != null) {
                node.setParent(clones.get(node.getParent().getPosition()));
            }
            node.adjacent = node.adjacent.stream()
                    .map(adj -> clones.get(adj.getPosition()))
                    .collect(Collectors.toList());
            node.score = potentialField.getScores().get(node.getPosition());
        }

        return new Graph(potentialField, clones);
    }

    public static class Node implements Cloneable {
        private Score score;
        private double priority;
        private double threatSumOnPath;
        private Node parent;
        private List<Node> adjacent = new ArrayList<>(8);
        private List<Node> staticAdjacent;
        private double dist;
        private int stepsUnderThreat;
        private int steps;

        public Node(Score score) {
            this.score = score;
        }

        public void refresh() {
            priority = 0;
            threatSumOnPath = 0;
            parent = null;
            adjacent.clear();
            dist = 0;
            stepsUnderThreat = 0;
            steps = 0;
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

        public List<Node> getStaticAdjacent() {
            if (staticAdjacent == null) {
                staticAdjacent = new ArrayList<>(8);

                var field = World.getInstance().getStaticPotentialField();
                var graph = field.getStaticGraph();

                for (int x = score.getX() - 1; x <= score.getX() + 1; x++) {
                    for (int y = score.getY() - 1; y <= score.getY() + 1; y++) {
                        if (x == score.getX() && y == score.getY()) {
                            continue;
                        }

                        var adjScore = field.getScoreByIndex(x, y);
                        if (adjScore != null && !adjScore.isUnreachable()) {
                            staticAdjacent.add(graph.getOrCreateNode(adjScore.getPosition()));
                        }
                    }
                }
            }

            return staticAdjacent;
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
            return score.equals(((Node)obj).score);
        }

        @Override
        public Node clone() throws CloneNotSupportedException {
            return (Node) super.clone();
        }

        @Override
        public String toString() {
            return score.toString();
        }
    }
}
