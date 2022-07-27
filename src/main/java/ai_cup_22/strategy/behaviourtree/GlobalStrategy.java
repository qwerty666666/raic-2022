package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.ViewMap.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GlobalStrategy {
    private final Map<Loot, Unit> takenLoots = new HashMap<>();
    private Unit priorityTargetEnemy;
    private Map<Unit, Node> explorePoints = new HashMap<>();
    private int explorePointsLastUpdateTick;

    public void updateTick() {
        takenLoots.clear();
        updatePriorityTargetEnemy();
    }

    private void updatePriorityTargetEnemy() {
//        var spawnedEnemies = World.getInstance().getEnemyUnits().values().stream()
//                .filter(Unit::isSpawned)
//                .collect(Collectors.toList());
//        var notSpawnedEnemies = World.getInstance().getEnemyUnits().values().stream()
//                .filter(Unit::isSpawned)
//                .collect(Collectors.toList());

//        if (!spawnedEnemies.isEmpty()) {
//            priorityTargetEnemy = getNearestToMyTeamUnit(spawnedEnemies);
//        } else {
//            priorityTargetEnemy = getNearestToMyTeamUnit(notSpawnedEnemies);
//        }
        priorityTargetEnemy = null;
    }

    private Unit getNearestToMyTeamUnit(List<Unit> units) {
        var myUnits = World.getInstance().getMyUnits().values().stream()
                .filter(Unit::isSpawned)
                .collect(Collectors.toList());

        return units.stream()
                .min(Comparator.comparingDouble(u -> myUnits.stream().mapToDouble(me -> me.getDistanceTo(u)).sum()))
                .orElse(null);
    }

    public void markLootAsTaken(Loot loot, Unit unit) {
        takenLoots.put(loot, unit);
    }

    public boolean isLootTakenByOtherUnit(Loot loot, Unit unit) {
        return takenLoots.containsKey(loot) && !takenLoots.get(loot).equals(unit);
    }

    public Unit getPriorityTargetEnemy() {
        return priorityTargetEnemy;
    }


    public Optional<Position> getPointToExplore(Unit unit) {
        if (explorePointsLastUpdateTick != World.getInstance().getCurrentTick() || explorePoints == null) {
            var viewNodes = World.getInstance().getViewMap().getNodes().values();

            if (viewNodes.size() < 3) {
                explorePoints = Collections.emptyMap();
            } else {
                explorePoints = findBestDistribution(
                        new ArrayList<>(World.getInstance().getMyUnits().values()),
                        new HashMap<>(),
                        World.getInstance().getMyUnits().values().stream()
                                .collect(Collectors.toMap(u -> u, u -> {
                                    return viewNodes.stream()
                                            .sorted(Comparator.comparingDouble(Node::getLastSeenTick)
                                                    .thenComparingDouble((Node node) -> node.getPosition().getDistanceTo(u.getPosition()))
                                            )
                                            .limit(3)
                                            .collect(Collectors.toList());
                                }))
                );
            }

            explorePointsLastUpdateTick = World.getInstance().getCurrentTick();
        }

        return Optional.ofNullable(explorePoints.get(unit))
                .map(Node::getPosition);
    }

    private Map<Unit, Node> findBestDistribution(List<Unit> units, Map<Unit, Node> takenNodes, Map<Unit, List<Node>> candidates) {
        if (units.isEmpty()) {
            return new HashMap<>(takenNodes);
        }

        Map<Unit, Node> bestDistribution = null;
        double bestDistributionScore = Double.MAX_VALUE;

        for (var unit: units) {
            var takeNode = candidates.get(unit).stream()
                    .filter(node -> !takenNodes.containsValue(node))
                    .findFirst()
                    .orElseThrow();
            takenNodes.put(unit, takeNode);

            var distribution = findBestDistribution(
                    units.stream().filter(u -> u != unit).collect(Collectors.toList()),
                    takenNodes,
                    candidates
            );

            var distToUnits = distribution.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getPosition().getDistanceTo(e.getValue().getPosition()))
                    .sum();
            var score = distToUnits;

            if (score < bestDistributionScore) {
                bestDistribution = distribution;
                bestDistributionScore = score;
            }

            takenNodes.remove(unit);
        }

        return bestDistribution;
    }
}
