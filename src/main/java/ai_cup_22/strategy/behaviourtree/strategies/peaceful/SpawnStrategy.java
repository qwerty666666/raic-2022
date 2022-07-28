package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.RotateAction;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.RetreatStrategy;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnStrategy implements Strategy {
    private final Unit unit;
    private final FightStrategy fightStrategy;
    private final ExploreStrategy exploreStrategy;

    public SpawnStrategy(Unit unit, FightStrategy fightStrategy, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.fightStrategy = fightStrategy;
        this.exploreStrategy = exploreStrategy;
    }

    @Override
    public double getOrder() {
        return unit.isSpawned() ? MIN_ORDER : MAX_ORDER;
    }

    @Override
    public Action getAction() {
        if (World.getInstance().getCurrentTick() < 151) {
            return spawnStartGame();
        } else {
            return spawnMidGame();
        }
    }

    private Action spawnStartGame() {
        // avoid spawn on obstacles
        var obstacles = getObstacles();
        if (unit.getRemainingSpawnTicks() < 5 && !canBeAtPosition(unit.getPosition(), obstacles)) {
            return new MoveToAction(getSafePlaceToSpawn(obstacles));
        }

        return new LootBowWithExploreStrategy(unit, exploreStrategy).getAction();
    }

    private Action spawnMidGame() {
        // try to not spawn on another unit

        var obstacles = getObstacles();
        var action = new CompositeAction();

        if (unit.getRemainingSpawnTicks() < 10 && !canBeAtPosition(unit.getPosition(), obstacles)) {
            return new MoveToAction(getSafePlaceToSpawn(obstacles));
        }

        // take loot or smth...

        if (World.getInstance().getZone().getRadius() > 150) {
            action.add(new MaxOrderCompositeStrategy()
                    .add(new RetreatToSafeSpawn(unit))
                    .add(new LootBowWithExploreStrategy(unit, exploreStrategy))
                    .getAction()
            );
        } else {
            action.add(new MaxOrderCompositeStrategy()
                    .add(new RetreatToSafeSpawn(unit))
                    .add(new LootStrategy(unit, exploreStrategy, fightStrategy))
                    .getAction()
            );
        }

        // always rotate

        action.add(new RotateAction());

        return action;
    }

    private Position getSafePlaceToSpawn(List<Circle> obstacles) {
        List<Position> positionsToRun = new ArrayList<>();
        for (int i = -5; i < 5; i++) {
            for (int j = -5; j < 5; j++) {
                positionsToRun.add(unit.getPosition().move(new Vector(i, j)));
            }
        }

        return positionsToRun.stream()
                .sorted(Comparator.comparingDouble(pos -> unit.getPosition().getSquareDistanceTo(pos)))
                .filter(pos -> canBeAtPosition(pos, obstacles))
                .findFirst()
                .orElseGet(() -> positionsToRun.get(positionsToRun.size() - 1));
    }

    private boolean canBeAtPosition(Position unitPosition, List<Circle> obstacles) {
        return obstacles.stream().noneMatch(circle -> circle.getCenter().getDistanceTo(unitPosition) <= circle.getRadius() + 1);
    }

    private List<Circle> getObstacles() {
        var obstacles = World.getInstance().getObstacles().values().stream()
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(World.getInstance().getMyUnits().values().stream()
                .filter(u -> u.getId() != unit.getId())
                .map(Unit::getCircle)
                .collect(Collectors.toList())
        );
        obstacles.addAll(World.getInstance().getAllEnemyUnits().stream()
                .filter(enemy -> enemy.getRemainingSpawnTicks() <= unit.getRemainingSpawnTicks())
                .map(Unit::getCircle)
                .collect(Collectors.toList())
        );

        return obstacles.stream()
                .filter(obs -> obs.getCenter().getDistanceTo(unit.getPosition()) < 20)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }


    public static class RetreatToSafeSpawn implements Strategy {
        private final RetreatStrategy retreatStrategy;
        private final Unit unit;

        public RetreatToSafeSpawn(Unit unit) {
            retreatStrategy = new RetreatStrategy(unit);
            this.unit = unit;
        }

        @Override
        public double getOrder() {
            var nearestEnemy = getNearestEnemy();

            if (nearestEnemy == null) {
                return MIN_ORDER;
            }

            return nearestEnemy.getDistanceTo(unit) < Constants.SAFE_DIST ? MAX_ORDER : MIN_ORDER;
        }

        @Override
        public Action getAction() {
            return retreatStrategy.getAction();
        }

        private Unit getNearestEnemy() {
            return World.getInstance().getAllEnemyUnits().stream()
                    .min(Comparator.comparingDouble(e -> e.getDistanceTo(unit)))
                    .orElse(null);
        }
    }


    public static class LootBowWithExploreStrategy implements Strategy {
        private Unit unit;
        private ExploreStrategy exploreStrategy;

        public LootBowWithExploreStrategy(Unit unit, ExploreStrategy exploreStrategy) {
            this.unit = unit;
            this.exploreStrategy = exploreStrategy;
        }

        @Override
        public double getOrder() {
            return 0.5;
        }

        @Override
        public Action getAction() {
            // take bow loot always
            var bowLoot = World.getInstance().getWeaponLoots().values().stream()
                    .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                    .filter(loot -> loot.getWeaponId() == Weapon.BOW_ID)
                    .filter(loot -> World.getInstance().getAllEnemyUnits().stream().noneMatch(u -> u.isStayOnLoot(loot)) &&
                            World.getInstance().getMyUnits().values().stream().noneMatch(u -> u != unit && u.isStayOnLoot(loot))
                    )
                    .min(Comparator.comparingDouble(loot -> loot.getPosition().getDistanceTo(unit.getPosition())))
                    .orElse(null);

            if (bowLoot != null) {
                World.getInstance().getGlobalStrategy().markLootAsTaken(bowLoot, unit);

                var dist = bowLoot.getPosition().getDistanceTo(unit.getPosition());
                if (dist / Constants.UNIT_SPAWN_SPEED_TICK + 50 > 150 - World.getInstance().getCurrentTick()) {
                    return new CompositeAction()
                            .add(new TakeLootAction(bowLoot))
                            .add(new RotateAction());
                }
            }

            // explore if no bow loot were found
            return exploreStrategy.getAction();
        }
    }
}
