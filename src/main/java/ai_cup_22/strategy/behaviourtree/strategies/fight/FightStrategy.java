package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.NullAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
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
        var targetEnemy = getTargetEnemy();

        if (targetEnemy == null) {
            return new NullAction();
        }

        if (isOnSafeDistance()) {
            return new CompositeAction()
                    .add(new MoveToWithPathfindingAction(targetEnemy.getPosition()))
                    .add(new LookToAction(targetEnemy));
        } else {
            contributeToPotentialField();

            return new CompositeAction()
                    .add(new MoveByPotentialFieldAction())
                    .add(new ShootAction(targetEnemy));
        }
    }

    public boolean isOnSafeDistance() {
        return World.getInstance().getAllEnemyUnits().stream()
                .allMatch(enemy -> enemy.getDistanceTo(me) > Constants.SAFE_DIST);
    }

    private double getBestDistanceToEnemy(Unit enemy) {
        if (enemy.isPhantom()) {
            return Constants.SAFE_DIST;
        }

        if (getEnemiesInFightRange().size() <= 1) {
            if (canAttackByDps(enemy)) {
                return 0;
            }

            if (canAttackByEnemyHasNoEnoughBullets(enemy)) {
                return 0;
            }
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

    private List<Unit> getEnemiesUnderAttack(List<Unit> enemies) {
        var weaponMaxDistance = me.getWeaponOptional().map(Weapon::getMaxDistance).orElse(0.);

        return enemies.stream()
                .filter(enemy -> me.canShoot(enemy) && me.getDistanceTo(enemy) < weaponMaxDistance)
                .collect(Collectors.toList());
    }

    private List<Unit> getEnemiesInFightRange() {
        return World.getInstance().getAllEnemyUnits().stream()
                .filter(enemy -> enemy.getDistanceTo(me) < 50)
                .collect(Collectors.toList());
    }

    private Unit getNearestEnemy(Collection<Unit> enemies) {
        return enemies.stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(me)))
                .orElse(null);
    }

    public Unit getTargetEnemy() {
        var enemiesAround = getEnemiesInFightRange();
        var enemiesUnderAttack = getEnemiesUnderAttack(enemiesAround);

        if (!enemiesUnderAttack.isEmpty()) {
            return getNearestEnemy(enemiesUnderAttack);
        }

        return getNearestEnemy(enemiesAround);
    }

    private void contributeToPotentialField() {
        var contributor = new SumCompositeScoreContributor("Enemies")
                .add(new ZoneScoreContributor());

        // target enemy

        var targetEnemy = getTargetEnemy();
        var safeDistant = getBestDistanceToEnemy(targetEnemy);

        if (!targetEnemy.isPhantom()) {
            contributor.add(new FirstMatchCompositeScoreContributor("Target Enemy")
                    .add(new LinearScoreContributor(
                            "Target Enemy Hold Distance",
                            targetEnemy.getPosition(),
                            Constants.PF_ENEMY_HOLD_DISTANCE_MAX_SCORE,
                            Constants.PF_ENEMY_HOLD_DISTANCE_MIN_SCORE,
                            safeDistant,
                            safeDistant + Constants.PF_ENEMY_HOLD_DISTANCE_DIST
                    ))
                    .add(new LinearScoreContributor(
                            targetEnemy.getPosition(),
                            Constants.PF_ENEMY_THREATEN_DIST_MIN_SCORE,
                            Constants.PF_ENEMY_THREATEN_DIST_MAX_SCORE,
                            safeDistant
                    ))
            );
        }

        // other enemies
        World.getInstance().getEnemyUnits().values().stream()
                .filter(enemy -> enemy != targetEnemy)
                .forEach(enemy -> {
                    contributor.add(new LinearScoreContributor(
                            "Enemy " + enemy.getPosition(),
                            enemy.getPosition(),
                            Constants.PF_NON_TARGET_ENEMY_MIN_SCORE,
                            Constants.PF_NON_TARGET_ENEMY_MAX_SCORE,
                            getThreatenDistanceForNonTargetEnemy(targetEnemy, enemy, me)
                    ));
                });

        // phantom enemies
        World.getInstance().getPhantomEnemies().values().stream()
                .filter(enemy -> enemy.getDistanceTo(me) < 50)
                .forEach(enemy -> {
                    contributor.add(new LinearScoreContributor(
                            "Phantom Enemy " + enemy.getPosition(),
                            enemy.getPosition(),
                            Constants.PF_PHANTOM_ENEMY_MIN_SCORE,
                            Constants.PF_PHANTOM_ENEMY_MAX_SCORE,
                            getThreatenDistanceForNonTargetEnemy(targetEnemy, enemy, me)
                    ));
                });

        contributor.contribute(me.getPotentialField());
    }

    private double getThreatenDistanceForNonTargetEnemy(Unit targetEnemy, Unit enemy, Unit me) {
        var angle = new Vector(me.getPosition(), targetEnemy.getPosition())
                .getAngleTo(new Vector(me.getPosition(), enemy.getPosition()));

        if (angle > Math.PI / 2) {
            return Constants.SAFE_DIST;
        }

        return new LinearDistributor(0, Math.PI / 2, enemy.getThreatenDistanceFor(me), Constants.SAFE_DIST)
                .get(angle);
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
