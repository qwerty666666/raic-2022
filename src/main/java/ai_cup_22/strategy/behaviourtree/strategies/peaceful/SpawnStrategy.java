package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.RotateAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnStrategy implements Strategy {
    private final Unit unit;
    private final FightStrategy fightStrategy;
    private final Strategy delegate;

    public SpawnStrategy(Unit unit, FightStrategy fightStrategy, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.fightStrategy = fightStrategy;
        this.delegate = new MaxOrderCompositeStrategy()
                .add(new LootAmmoStrategy(unit, exploreStrategy, fightStrategy, 100))
                .add(new LootShieldStrategy(unit, exploreStrategy, fightStrategy, 100))
                .add(fightStrategy)
                .add(exploreStrategy);
    }

    @Override
    public double getOrder() {
        return unit.isSpawned() ? MIN_ORDER : MAX_ORDER;
    }

    @Override
    public Action getAction() {

        // try to not spawn on another unit

        var obstacles = getObstacles();
        var action = new CompositeAction();

        if (unit.getRemainingSpawnTicks() < 30 && !canBeAtPosition(unit.getPosition(), obstacles)) {
            action.add(new MoveToAction(getSafePlaceToSpawn(obstacles)));
        } else {

            // take loot

            action.add(this.delegate.getAction());
        }

        // always rotate

        action.add(new RotateAction());

        // stop rotating

        if (unit.getRemainingSpawnTicks() < 15) {
            var targetEnemy = fightStrategy.getEnemyToShoot();
            if (targetEnemy != null) {
                action.add(new LookToAction(targetEnemy));
            } else {
                action.add(new RotateAction());
            }
        }

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
        obstacles.addAll(World.getInstance().getEnemyUnits().values().stream()
                .filter(enemy -> enemy.getRemainingSpawnTicks() <= unit.getRemainingSpawnTicks())
                .map(Unit::getCircle)
                .collect(Collectors.toList())
        );
        obstacles.addAll(World.getInstance().getPhantomEnemies().values().stream()
                .filter(phantom -> phantom.getRemainingSpawnTicks() <= unit.getRemainingSpawnTicks())
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
}
