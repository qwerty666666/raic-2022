package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
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
            contributeToPotentialField();

            // shoot target enemy
            return new CompositeAction()
                    .add(new MoveByPotentialFieldAction())
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

    private void contributeToPotentialField() {
        var contributor = new SumCompositeScoreContributor("Enemies")
                .add(new ZoneScoreContributor());

        // target enemy

        var targetEnemy = getTargetEnemy();
        var safeDistant = getBestDistanceToEnemy(targetEnemy);

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

        // other enemies
        getEnemies().stream()
                .filter(enemy -> enemy != targetEnemy)
                .forEach(enemy -> {
                    var angle = me.getDirection().getAngleTo(new Vector(me.getPosition(), enemy.getPosition()));

                    contributor.add(new LinearScoreContributor(
                            "Enemy " + enemy.getPosition(),
                            enemy.getPosition(),
                            Constants.PF_NON_TARGET_ENEMY_MIN_SCORE,
                            Constants.PF_NON_TARGET_ENEMY_MAX_SCORE,
                            enemy.getThreatenDistanceFor(me)
                    ));
                });

        contributor.contribute(me.getPotentialField());
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
