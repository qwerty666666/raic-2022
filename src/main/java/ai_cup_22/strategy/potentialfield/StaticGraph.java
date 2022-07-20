package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import java.util.HashMap;
import java.util.Map;

public class StaticGraph {
    private final StaticPotentialField staticPotentialField;
    private final Map<Position, Node> nodes = new HashMap<>();

    public StaticGraph(StaticPotentialField staticPotentialField) {
        this.staticPotentialField = staticPotentialField;
    }

    public Node getOrCreateNode(Position position) {
        // do not use lambda for perf !!!
        if (nodes.containsKey(position)) {
            return nodes.get(position);
        }

        var node = new Node(staticPotentialField.getScores().get(position));
        nodes.put(position, node);

        return node;
    }
}
