package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.DijkstraPathFinder;
import ai_cup_22.strategy.pathfinding.Graph.Node;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.pathfinding.PathFinder;
import java.util.Comparator;

public class MoveByPotentialFieldAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        new MoveByPathAction(getBestPathToGo(unit))
                .apply(unit, order);
    }

    private Path getBestPathToGo(Unit unit) {
        new DijkstraPathFinder(unit.getPotentialField(), unit.getPosition());

        var graph = unit.getPotentialField().getGraph();

        // we need minScoreValue for (node.getPriority() - minScoreValue) be positive
        var minScoreValue = graph.getNodes().values().stream()
                .mapToDouble(Node::getScoreValue)
                .min()
                .orElse(0.);

        var bestNode = graph.getNodes().values().stream()
                .max(Comparator.comparingDouble(node -> {
                    // +1 to consider stay at the same node
                    return (node.getScoreValue() - minScoreValue) /
                            Math.max(1, -node.getThreatSumOnPath() / Math.max(1, node.getDist())) /
                            (node.getDist() > 2 ? Math.log(node.getDist()) : 1);
//                            (1 + node.getDist() *
//                            (node.getThreatSumOnPath() > -1 ? 1 : -node.getThreatSumOnPath()));
                }))
                .orElseThrow();


//        graph.getNodes().values().stream()
//                .filter(n -> n.getPosition().getDistanceTo(unit.getPotentialField().getCenter()) < 15)
//                .forEach(node -> DebugData.getInstance().getDefaultLayer().addText(
//                        String.format("th: %.2f\nsv: %.2f\ndist: %.2f\nth sum: %.2f\ndiv: %.2f\n log%.2f\nres: %.2f",
//                                node.getScore().getThreatScore(),
//                                node.getScoreValue(),
//                                node.getDist(),
//                                node.getThreatSumOnPath(),
//                                Math.max(1, -node.getThreatSumOnPath() / Math.max(1, node.getDist())),
//                                Math.log(node.getDist()),
//                                (node.getScoreValue() - minScoreValue) /
//                                        Math.max(1, -node.getThreatSumOnPath() / Math.max(1, node.getDist())) /
//                                        (node.getDist() > 2 ? Math.log(node.getDist()) : 1)
//                        ),
//                        node.getPosition())
//                );

        return Path.from(bestNode,graph);
}
}
