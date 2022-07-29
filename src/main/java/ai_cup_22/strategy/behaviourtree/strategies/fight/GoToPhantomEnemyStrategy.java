package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.basic.NullAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class GoToPhantomEnemyStrategy implements Strategy {
    private final Unit unit;

    public GoToPhantomEnemyStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        var enemyGroup = getTargetGroup();

        if (enemyGroup == null || getGroupPriority(enemyGroup) < 100) {
            return MIN_ORDER;
        }

        return 0.2;
    }

    @Override
    public Action getAction() {
        var enemyGroup = getTargetGroup();

        if (enemyGroup == null) {
            return new NullAction();
        }

        return new CompositeAction()
                .add(new MoveToWithPathfindingAction(enemyGroup.get(0).getPosition()));
    }

    private List<Unit> getTargetGroup() {
        var enemies = World.getInstance().getEnemyUnits().values().stream()
                .sorted(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)))
                .collect(Collectors.toList());

        var groups = new ArrayList<List<Unit>>();

        var set = new HashSet<>(enemies);
        for (var enemy: enemies) {
            if (!set.contains(enemy)) {
                continue;
            }

            set.remove(enemy);

            var group = new ArrayList<Unit>();
            group.add(enemy);
            groups.add(group);

            for (var other: new ArrayList<>(set)) {
                if (other.getDistanceTo(enemy) < 40) {
                    group.add(other);
                    set.remove(other);
                }
            }
        }

        return groups.stream()
                .filter(group -> {
                    return group.size() > 3 || World.getInstance().getMyUnits().values().stream()
                            .noneMatch(u -> u.getId() != unit.getId() && u.getDistanceTo(group.get(0)) < 40);
                })
                .max(Comparator.comparingDouble(this::getGroupPriority))
                .orElse(null);
    }

    private double getGroupPriority(List<Unit> group) {
        var dist = group.get(0).getDistanceTo(unit);
        var count = group.size();

        return count / dist;
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
