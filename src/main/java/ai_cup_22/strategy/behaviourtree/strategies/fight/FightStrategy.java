package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.HoldDistanceAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import java.util.ArrayList;
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
        if (isOnSafeDistance()) {
            return 0.2;   // allow loot strategy to win
        }

        return MAX_ORDER;
    }

    @Override
    public Action getAction() {
        if (getEnemies().isEmpty()) {
            return new CompositeAction();
        }

        var enemiesUnderAttack = getEnemiesUnderAttack();
        var targetEnemy = getTargetEnemy();

        if (!enemiesUnderAttack.isEmpty()) {
            // shoot target enemy
            return new CompositeAction()
                    .add(new HoldDistanceAction(targetEnemy.getPosition(), getBestDistanceToEnemy(targetEnemy)))
                    .add(new ShootAction(targetEnemy));
        } else {
            // aim to target enemy
            return new CompositeAction()
                    .add(new MoveToWithPathfindingAction(targetEnemy.getPosition()))
                    .add(new LookToAction(targetEnemy))
                    .add(new AimAction());
        }

    }

    public boolean isOnSafeDistance() {
        return World.getInstance().getEnemyUnits().values().stream()
                .allMatch(enemy -> enemy.getDistanceTo(me) > Constants.SAFE_DIST);
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

    private List<Unit> getEnemiesUnderAttack() {
        var weaponMaxDistance = me.getWeaponOptional().map(Weapon::getMaxDistance).orElse(0.);

        return World.getInstance().getEnemyUnits().values().stream()
                .filter(enemy -> me.canShoot(enemy) && me.getDistanceTo(enemy) < weaponMaxDistance)
                .collect(Collectors.toList());
    }

    private List<Unit> getEnemies() {
        return new ArrayList<>(World.getInstance().getEnemyUnits().values());
    }

    private Unit getNearestEnemy(Collection<Unit> enemies) {
        return enemies.stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(me)))
                .orElse(null);
    }

    public Unit getTargetEnemy() {
        var enemiesUnderAttack = getEnemiesUnderAttack();

        if (!enemiesUnderAttack.isEmpty()) {
            return getNearestEnemy(getEnemiesUnderAttack());
        }

        return getNearestEnemy(getEnemies());
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
