package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.LookBackAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.actions.ShootWithLookBackAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.NullAction;
import ai_cup_22.strategy.actions.basic.TakeShieldPotionAction;
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
import ai_cup_22.strategy.simulation.walk.WalkSimulation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class FightStrategy implements Strategy {
    private static final double TAKE_SHIELD_POTION_ADDITIONAL_DIST = 2;

    private final Unit me;
    private final LootAmmoSafestWayStrategy lootAmmoStrategy;
    private Unit targetEnemy;

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
                    .add(new LookToAction(enemyToShoot))
                    .add(new LookBackAction());
        } else {
            contributeToPotentialField();

            var action = new CompositeAction();

            if (shouldTakeShieldPotion()) {

                // take shield potion if possible

                if (getEnemiesThatCanShootInSafeDist().stream()
                        .noneMatch(e -> e.getDistanceTo(me) < e.getThreatenDistanceFor(me) + TAKE_SHIELD_POTION_ADDITIONAL_DIST)) {
                    action
                            .add(new TakeShieldPotionAction())
                            .add(new LookBackAction());
                } else {
                    action.add(new RetreatStrategy(me).getAction());
                }

            } else if (shouldTakeAmmo()) {

                // take ammo if needed

                action.add(lootAmmoStrategy.getAction());

                // if I can take loot, then allow to do it (should not aim)
                var loot = lootAmmoStrategy.getBestLoot();
                if (loot.isPresent() && (
                        // I stay on the loot
                        (me.canDoNewAction() && me.isStayOnLoot(loot.get())) ||
                        // I can safely run to the loot
                        (getEnemiesThatCanShootInSafeDist().stream()
                                .noneMatch(e -> e.getDistanceTo(me) < e.getThreatenDistanceFor(me) + TAKE_SHIELD_POTION_ADDITIONAL_DIST) &&
                            WalkSimulation.getTicksToRunDistance(me, loot.get().getPosition(), false) < me.getTicksToUnaim()
                        )
                )) {
                    action.add(new LookBackAction());
                } else {
                    action.add(new ShootWithLookBackAction(me, enemyToShoot));
                }

            } else {

                // otherwise go to best point

                action
                        .add(new MoveByPotentialFieldAction(false))
                        .add(new ShootWithLookBackAction(me, enemyToShoot));
            }

            return action;
        }
    }

    private boolean shouldTakeShieldPotion() {
        if (me.isTakenShieldPotion()) {
            return false;
        }

        if (me.getShieldPotions() == 0) {
            return false;
        }

        var enemyToShoot = getEnemyToShoot();
        if (canPushEnemy(enemyToShoot)) {
            return false;
        }

        return me.getShield() == 0;
    }

    public boolean isOnSafeDistance() {
        return World.getInstance().getAllEnemyUnits().stream()
                .filter(Unit::isSpawned)
                .allMatch(enemy -> enemy.getDistanceTo(me) > Constants.SAFE_DIST);
    }

    private double getBestDistanceToTargetEnemy(Unit enemy) {
        if (canPushEnemy(enemy)) {
            return 0;
        }

        return enemy.getThreatenDistanceFor(me) + (me.canDoNewAction() ? 0 : TAKE_SHIELD_POTION_ADDITIONAL_DIST);
    }

    private boolean canPushEnemy(Unit enemy) {
        if (!enemy.isSeenBefore()) {
            return false;
        }

        if (!hasEnoughAmmoToKill(enemy)) {
            return false;
        }

        if (!canAttackByDps(enemy)) {
            return false;
        }

        if (!canAttackByEnemyHasNoEnoughBullets(enemy)) {
            return false;
        }

        var enemiesThatCanShoot = getEnemiesThatCanShootInSafeDist();
        if (enemiesThatCanShoot.size() > 1 ||
                (enemiesThatCanShoot.size() == 1 && !enemiesThatCanShoot.get(0).equals(enemy))) {
            return false;
        }

        return true;
    }

    private List<Unit> getEnemiesThatCanShootInSafeDist() {
        return getEnemiesInViewRange().stream()
                .filter(enemy -> enemy.isSpawned() && enemy.hasWeapon() && enemy.getBulletCount() > 0)
                .filter(enemy -> enemy.getDistanceTo(me) < Constants.SAFE_DIST)
                .collect(Collectors.toList());
    }

    private boolean hasEnoughAmmoToKill(Unit enemy) {
        var dmg = me.getBulletCount() * me.getWeaponOptional().map(Weapon::getDamage).orElse(0.);

        return enemy.getFullHealth() <= dmg;
    }

    private boolean canAttackByEnemyHasNoEnoughBullets(Unit enemy) {
        if (enemy.isSeenBefore()) {
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

    private List<Unit> getEnemiesInViewRange() {
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
            var enemyMinDist = getEnemiesInViewRange().stream()
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
        if (targetEnemy == null) {
            var enemiesInViewRange = getEnemiesInViewRange();

            var spawnedEnemies = enemiesInViewRange.stream()
                    .filter(Unit::isSpawned)
                    .collect(Collectors.toList());

            if (spawnedEnemies.isEmpty()) {
                targetEnemy = getNearestEnemy(enemiesInViewRange);
            } else {
                targetEnemy = spawnedEnemies.stream()
                        .max(Comparator.comparingDouble((Unit enemy) -> getShootingPriorityForEnemy(me, enemy))
                                .thenComparingDouble((Unit enemy) -> enemy.getDistanceTo(me))
                        )
                        .orElseThrow();
            }
        }

        return targetEnemy;
    }

    public double getShootingPriorityForEnemy(Unit me, Unit enemy) {
        var dist = me.getDistanceTo(enemy);

        var shootLine = ShootAction.getNearestShootablePosition(me, enemy);
        var ticksToShoot = WalkSimulation.getTicksToRunDistanceWithAim(me, shootLine.getProjection(me.getPosition()));
        var ticksToRotate = WalkSimulation.getTicksToRotateWithAim(me, enemy.getPosition(), true);

        var distMul = new LinearDistributor(10, 30, 1, 0)
                .get(dist);
        var timeToShootMul = new LinearDistributor(Math.min(me.getRemainedTicksToAim(),
                me.getRemainingCoolDownTicks()), 30, 1, 0
        )
                .get(Math.max(ticksToShoot, ticksToRotate));
        var healthMul = enemy.getFullHealth() <= me.getDamage() ? 1 : 0.6;

        return distMul * timeToShootMul * healthMul;
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
        var safeDistant = getBestDistanceToTargetEnemy(targetEnemy);

        if (targetEnemy.isSpawned()) {
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
        World.getInstance().getAllEnemyUnits().stream()
                .filter(enemy -> enemy != targetEnemy)
                .filter(Unit::isSpawned)
                .forEach(enemy -> {
                    if (!enemy.isSpawned()) {
                        return;
                    }

                    if (enemy.isSeenBefore() && (!enemy.hasWeapon() || enemy.getBulletCount() == 0)) {
                        return;
                    }

                    contributor.add(new LinearScoreContributor(
                            "Enemy " + enemy.getPosition(),
                            enemy.getPosition(),
                            Constants.PF_NON_TARGET_ENEMY_MIN_SCORE,
                            Constants.PF_NON_TARGET_ENEMY_MAX_SCORE,
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

        return new LinearDistributor(
                Math.PI / 4,
                Math.PI / 2,
                enemy.getThreatenDistanceFor(me) + (me.canDoNewAction() ? 0 : TAKE_SHIELD_POTION_ADDITIONAL_DIST),
                Constants.SAFE_DIST
        )
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
        private Optional<Loot> bestLoot;

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
        public Optional<Loot> getBestLoot() {
            if (bestLoot == null) {
                var loots = getSuitableLoots().stream()
                        .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                        .collect(Collectors.toList());

                if (loots.isEmpty()) {
                    bestLoot = Optional.empty();
                } else {
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

                    bestLoot = Optional.ofNullable(loot);
                }
            }
            return bestLoot;
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }
}
