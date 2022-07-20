package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GlobalStrategy {
    private final Map<Loot, Unit> takenLoots = new HashMap<>();
    private Unit priorityTargetEnemy;

    public void updateTick() {
        takenLoots.clear();
        updatePriorityTargetEnemy();
    }

    private void updatePriorityTargetEnemy() {
        var spawnedEnemies = World.getInstance().getEnemyUnits().values().stream()
                .filter(Unit::isSpawned)
                .collect(Collectors.toList());
        var notSpawnedEnemies = World.getInstance().getEnemyUnits().values().stream()
                .filter(Unit::isSpawned)
                .collect(Collectors.toList());

        if (!spawnedEnemies.isEmpty()) {
            priorityTargetEnemy = getNearestToMyTeamUnit(spawnedEnemies);
        } else {
            priorityTargetEnemy = getNearestToMyTeamUnit(notSpawnedEnemies);
        }
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
}
