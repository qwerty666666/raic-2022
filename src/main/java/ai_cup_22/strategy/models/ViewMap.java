package ai_cup_22.strategy.models;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewMap {
    private static final int DIST = 40;
    private Map<Position, Node> nodes = new HashMap<>();

    public ViewMap(World world) {
        buildNodes();
    }

    public Map<Position, Node> getNodes() {
        return nodes;
    }

    private void buildNodes() {
        for (int i = -300; i < 300; i += DIST) {
            for (int j = -300; j < 300; j += DIST) {
                var pos = new Position(i, j);
                nodes.put(pos, new Node(pos));
            }
        }

//        for (var node: nodes.values()) {
//            var pos = node.getPosition();
//            if (nodes.containsKey(pos.move(new Vector(-DIST, 0)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(-DIST, 0))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(DIST, 0)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(DIST, 0))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(0, -DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(0, -DIST))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(0, DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(0, DIST))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(-DIST, -DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(-DIST, -DIST))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(-DIST, DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(-DIST, DIST))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(DIST, -DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(DIST, -DIST))));
//            }
//            if (nodes.containsKey(pos.move(new Vector(DIST, DIST)))) {
//                node.addAdjacent(nodes.get(pos.move(new Vector(DIST, DIST))));
//            }
//        }
    }

    public void updateTick() {
        if (World.getInstance().getCurrentTick() % 50 == 0) {
            removeNodesOutOfZone();
        }

        for (var node: nodes.values()) {
            if (World.getInstance().getMyUnits().values().stream()
                    .anyMatch(unit -> unit.getViewSegment().getCircleSegment().contains(node.getPosition()))) {
                node.lastSeenTick = World.getInstance().getCurrentTick();
            }
        }
    }

    private void removeNodesOutOfZone() {
        var zone = World.getInstance().getZone();

        for (var node: new ArrayList<>(nodes.values())) {
            if (node.getPosition().getDistanceTo(zone.getCenter()) > zone.getRadius() - 10) {
                nodes.remove(node.getPosition());
            }
        }
    }

    public static class Node {
        private Position position;
        private int lastSeenTick;

        public Node(Position position) {
            this.position = position;
        }

        public Position getPosition() {
            return position;
        }

        public int getLastSeenTick() {
            return lastSeenTick;
        }
    }
}
