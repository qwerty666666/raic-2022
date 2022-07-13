package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.DodgeBulletsAction;
import ai_cup_22.strategy.actions.HoldDistanceAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
            var targetEnemy = getNearestEnemy(enemiesUnderAttack);

            return new CompositeAction()
                    .add(new HoldDistanceAction(targetEnemy.getPosition(), getBestDistanceToEnemy(targetEnemy)))
                    .add(new ShootAction(targetEnemy))
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

    private double getBestDistanceToEnemy(Unit enemy) {
        if (canAttackByDps(enemy)) {
            return 0;
        }

        if (canAttackByEnemyHasNoEnoughBullets(enemy)) {
            return 0;
        }

        return enemy.getThreatenDistanceFor(me);
    }

    private boolean canAttackByEnemyHasNoEnoughBullets(Unit enemy) {
        var dmg = enemy.getWeaponOptional().map(Weapon::getDamage).orElse(0.);

        return dmg * enemy.getBulletCount() <= me.getFullHealth() - me.getMaxHealth();
    }

    private boolean canAttackByDps(Unit enemy) {
        var enemyDps = enemy.getWeaponOptional().map(Weapon::getDps).orElse(0.);
        var myDps = me.getWeaponOptional().map(Weapon::getDps).orElse(0.);

        var timeToKillMe = Math.ceil(me.getFullHealth() / enemyDps);
        var timeToKillEnemy = Math.ceil(enemy.getFullHealth() / myDps);

        return timeToKillEnemy * 1.5 < timeToKillMe;
    }

    private List<Unit> getEnemies() {
        return World.getInstance().getEnemyUnits().values().stream().collect(Collectors.toList());
    }

    private List<Unit> getEnemiesUnderAttack() {
        return World.getInstance().getEnemyUnits().values().stream()
                .filter(me::canShoot)
                .collect(Collectors.toList());
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
