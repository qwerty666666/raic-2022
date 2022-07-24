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
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.BaseLootStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.ExploreStrategy;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.CircularWithAvoidObstaclesContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class FightStrategy implements Strategy {
    private final Unit me;
    private final Strategy lootAmmoStrategy;

    public FightStrategy(Unit me, ExploreStrategy exploreStrategy) {
        this.me = me;
        this.lootAmmoStrategy = new LootAmmoSafestWayStrategy(me, exploreStrategy, this, 50);
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
        var enemyToShoot = getEnemyToShoot();
        var priorityEnemy = getPriorityEnemy();

        if (enemyToShoot == null) {
            return new NullAction();
        }

        if (isOnSafeDistance()) {
            return new CompositeAction()
                    .add(new MoveToWithPathfindingAction(priorityEnemy.getPosition()))
                    .add(new LookToAction(enemyToShoot));
        } else {
            contributeToPotentialField();

            var action = new CompositeAction();

            // take ammo if needed
            if (shouldTakeAmmo()) {
                action.add(lootAmmoStrategy.getAction());
            } else {
                // otherwise go to best point
                action.add(new MoveByPotentialFieldAction());
            }

            // always try to shoot to enemy
            action.add(new ShootAction(enemyToShoot));

            return action;
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

        if (canPushEnemy(enemy)) {
            return 0;
        }

        return enemy.getThreatenDistanceFor(me);
    }

    private boolean canPushEnemy(Unit enemy) {
        if (getEnemiesInFightRange().size() > 1) {
            return false;
        }

        if (!hasEnoughAmmoToKill(enemy)) {
            return false;
        }

        if (canAttackByDps(enemy)) {
            return true;
        }

        if (canAttackByEnemyHasNoEnoughBullets(enemy)) {
            return true;
        }

        return false;
    }

    private boolean hasEnoughAmmoToKill(Unit enemy) {
        var dmg = me.getBulletCount() * me.getWeaponOptional().map(Weapon::getDamage).orElse(0.);

        return enemy.getFullHealth() <= dmg;
    }

    private boolean canAttackByEnemyHasNoEnoughBullets(Unit enemy) {
        if (enemy.isPhantom()) {
            return false;
        }

        var dmg = enemy.getWeaponOptional().map(Weapon::getDamage).orElse(0.);
        return dmg * enemy.getBulletCount() <= me.getFullHealth() - me.getMaxHealth();
    }

    private boolean canAttackByDps(Unit enemy) {
        var ticksToKillMe = getTickToKill(enemy, me);
        var ticksToKillEnemy = getTickToKill(me, enemy);

        return ticksToKillMe > 0 && ticksToKillEnemy * 1.5 < ticksToKillMe;
    }

    private int getTickToKill(Unit attacker, Unit attacked) {
        var dmg = attacker.getWeaponOptional().map(Weapon::getDamage).orElse(0.);
        var cd = attacker.getWeaponOptional().map(Weapon::getCoolDownTicks).orElse(0);

        return (int) (Math.ceil(attacked.getFullHealth() / dmg) - 1) * cd;
    }

    private List<Unit> getEnemiesUnderAttack(List<Unit> enemies) {
        return enemies.stream()
                .filter(me::canShoot)
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

    public Unit getPriorityEnemy() {
        var priorityEnemy = World.getInstance().getGlobalStrategy().getPriorityTargetEnemy();

        if (priorityEnemy != null) {
            var enemyMinDist = getEnemiesInFightRange().stream()
                    .filter(Unit::isSpawned)
                    .mapToDouble(u -> u.getDistanceTo(me))
                    .min()
                    .orElse(Double.MAX_VALUE);

            if (enemyMinDist > Constants.SAFE_DIST || priorityEnemy.getDistanceTo(me) < Constants.SAFE_DIST) {
                return priorityEnemy;
            }
        }

        return getTargetEnemy();
    }

    private Unit getTargetEnemy() {
        var enemiesAround = getEnemiesInFightRange();

        var spawnedEnemies = enemiesAround.stream()
                .filter(Unit::isSpawned)
                .collect(Collectors.toList());

        if (spawnedEnemies.isEmpty()) {
            return getNearestEnemy(enemiesAround);
        } else {
            var enemiesUnderAttack = getEnemiesUnderAttack(spawnedEnemies);
            if (!enemiesUnderAttack.isEmpty()) {
                return getNearestEnemy(enemiesUnderAttack);
            }

            return getNearestEnemy(spawnedEnemies);
        }
    }

    public Unit getEnemyToShoot() {
        var priorityEnemy = getPriorityEnemy();
        if (priorityEnemy != null && me.canShoot(priorityEnemy)) {
            return priorityEnemy;
        }

        return getTargetEnemy();
    }

    private void contributeToPotentialField() {
        var contributor = new SumCompositeScoreContributor("Enemies")
                .add(new ZoneScoreContributor(me.getPotentialField()));

        // target enemy

        var targetEnemy = getEnemyToShoot();
        var safeDistant = getBestDistanceToEnemy(targetEnemy);

        if (!targetEnemy.isPhantom()) {
            contributor.add(new FirstMatchCompositeScoreContributor("Target Enemy")
                    .add(new CircularWithAvoidObstaclesContributor(
                            "Target Enemy Hold Distance",
                            targetEnemy.getPosition(),
                            new LinearScoreContributor(
                                    targetEnemy.getPosition(),
                                    Constants.PF_ENEMY_HOLD_DISTANCE_MAX_SCORE,
                                    Constants.PF_ENEMY_HOLD_DISTANCE_MIN_SCORE,
                                    safeDistant,
                                    safeDistant + Constants.PF_ENEMY_HOLD_DISTANCE_DIST
                            ),
                            safeDistant + Constants.PF_ENEMY_HOLD_DISTANCE_DIST,
                            0
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
                .filter(Unit::isSpawned)
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
                .filter(Unit::isSpawned)
                .forEach(enemy -> {
                    contributor.add(new LinearScoreContributor(
                            "Phantom Enemy " + enemy.getPosition(),
                            enemy.getPosition(),
                            Constants.PF_PHANTOM_ENEMY_MIN_SCORE,
                            Constants.PF_PHANTOM_ENEMY_MAX_SCORE,
                            getThreatenDistanceForNonTargetEnemy(targetEnemy, enemy, me)
                    ));
                });

        // my units
        World.getInstance().getMyUnits().values().stream()
                .filter(unit -> unit.getId() != me.getId() && unit.isSpawned())
                .forEach(unit -> {
                    contributor.add(new LinearScoreContributor(
                            "My Units: " + unit.getPosition(),
                            unit.getPosition(),
                            Constants.PF_ALLY_MIN_SCORE,
                            Constants.PF_ALLY_MAX_SCORE,
                            Constants.PF_ALLY_DIST
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

    private boolean shouldTakeAmmo() {
        var enemy = getEnemyToShoot();

        if (canPushEnemy(enemy)) {
            return false;
        }

        return !hasEnoughAmmoToKill(enemy) || me.getBulletCount() < me.getMaxBulletCount() * 0.3;
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }



    public static class LootAmmoSafestWayStrategy extends BaseLootStrategy {
        private final double maxLootDist;

        protected LootAmmoSafestWayStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy, double maxLootDist) {
            super(unit, exploreStrategy, fightStrategy);
            this.maxLootDist = maxLootDist;
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            return World.getInstance().getAmmoLoots(unit.getWeapon().getId()).stream()
                    .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                    .collect(Collectors.toList());
        }

        @Override
        protected Optional<Loot> getBestLoot() {
            var loots = getSuitableLoots().stream()
                    .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                    .collect(Collectors.toList());

            if (loots.isEmpty()) {
                return Optional.empty();
            }

            var pathFinder = new AStarPathFinder(unit.getPotentialField());
            var paths = loots.stream()
                    .collect(Collectors.toMap(
                            loot -> loot,
                            loot -> pathFinder.findPath(unit.getPosition(), loot.getPosition())
                    ));

            // search by min sum of treats on the path
            // and min by distance if there is no treat on the path
            var loot = paths.entrySet().stream()
                    .min(
                            Comparator.comparingDouble(
                                            (Entry<Loot, Path> e) -> e.getValue().getScores().stream()
                                                    .filter(score -> score.getNonStaticScore() < 0)
                                                    .mapToDouble(Score::getNonStaticScore)
                                                    .sum()
                                    )
                                    .reversed()
                                    .thenComparingDouble((Entry<Loot, Path> e) -> e.getValue().getDistance())
                    )
                    .orElseThrow()
                    .getKey();

            return Optional.ofNullable(loot);
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }
}
