package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.DodgeBulletsAction;
import ai_cup_22.strategy.actions.HoldDistanceAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.models.Unit;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class FightStrategy implements Strategy {
    private final Unit me;

    public FightStrategy(Unit me) {
        this.me = me;
    }

    @Override
    public double getOrder() {
        return MAX_ORDER;
    }

    @Override
    public Action getAction() {
        if (getEnemies().isEmpty()) {
            return new CompositeAction();
        }

        var enemiesUnderAttack = getEnemiesUnderAttack();

        if (!enemiesUnderAttack.isEmpty()) {
            // shoot to the nearest enemy
            var target = getNearestEnemy(enemiesUnderAttack);

            return new CompositeAction()
                    .add(new HoldDistanceAction(target.getPosition(), target.getThreatenDistanceFor(me)))
                    .add(new ShootAction(target))
                    .add(new DodgeBulletsAction());
        } else {
            // aim to the nearest enemy
            var target = getNearestEnemy(getEnemies());

            return new CompositeAction()
                    .add(new MoveToWithPathfindingAction(target.getPosition()))
                    .add(new LookToAction(target))
                    .add(new AimAction())
                    .add(new DodgeBulletsAction());
        }
    }

    private List<Unit> getEnemies() {
        return World.getInstance().getEnemyUnits().values().stream().toList();
    }

    private List<Unit> getEnemiesUnderAttack() {
        return World.getInstance().getEnemyUnits().values().stream()
                .filter(me::canShoot)
                .toList();
    }

    private Unit getNearestEnemy(Collection<Unit> enemies) {
        return enemies.stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(me)))
                .orElse(null);
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
