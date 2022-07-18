package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.DijkstraPathFinder;
import ai_cup_22.strategy.pathfinding.Graph;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.pathfinding.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MoveByPotentialFieldAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        new MoveByPathAction(getBestPathToGo(unit))
                .apply(unit, order);
    }

    private Path getBestPathToGo(Unit unit) {
        new DijkstraPathFinder(unit.getPotentialField(), unit.getPosition());

        var graph = unit.getPotentialField().getGraph();

        var bestNode = getNodeScores(graph).entrySet().stream()
                .max(Comparator.comparingDouble((Entry<Node, Double> e) -> e.getValue())
                        .thenComparing(
                                Comparator.comparingDouble((Entry<Node, Double> e) -> e.getKey().getDist())
                                        .reversed()
                        )
                )
                .orElseThrow()
                .getKey();

        return Path.from(bestNode,graph);
    }

    public static Map<Node, Double> getNodeScores(Graph graph) {
        // we need minScoreValue for (node.getPriority() - minScoreValue) be positive
//        var minScoreValue = graph.getNodes().values().stream()
//                .mapToDouble(Node::getScoreValue)
//                .min()
//                .orElse(0.);

        var maxDist = graph.getNodes().values().stream()
                .mapToDouble(Node::getDist)
                .max()
                .orElseThrow();

        return graph.getNodes().values().stream()
                .collect(Collectors.toMap(
                        node -> node,
                        node -> {
                            return (node.getThreatSumOnPath() + node.getScoreValue() * (maxDist - node.getDist())) *
                                    (node.getDist() > 2.72 ? Math.log(node.getDist()) : 1);
//                            // +1 to consider stay at the same node
//                            return (node.getScoreValue() - minScoreValue) /
//                                    Math.max(1, -node.getThreatSumOnPath() / Math.max(1, node.getStepsUnderThreat())) /
//                                    (node.getDist() > 2.72 ? Math.log(node.getDist()) : 1);
////                            (1 + node.getDist() *
////                            (node.getThreatSumOnPath() > -1 ? 1 : -node.getThreatSumOnPath()));
                        })
                );
    }
}
