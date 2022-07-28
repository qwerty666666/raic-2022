package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.NullStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.distributions.FirstMatchDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootAmmoStrategy implements Strategy {
    private final Unit unit;
    private final Strategy delegate;
    private final double maxLootDist;

    public LootAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
        this(unit, exploreStrategy, fightStrategy, Constants.MAX_LOOT_STRATEGY_DIST);
    }

    public LootAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy, double maxLootDist) {
        this.unit = unit;
        this.maxLootDist = maxLootDist;

        this.delegate = new FirstMatchCompositeStrategy()
                .add(() -> !unit.hasWeapon(), new NullStrategy())
                .add(() -> unit.getBulletCount() == 0, new LootAmmoForceStrategy(unit, exploreStrategy, fightStrategy))
                .add(() -> true, new MaxOrderCompositeStrategy()
                        .add(new LootNearestAmmoStrategy(unit, exploreStrategy, fightStrategy))
                        .add(new LootNonOwningWeaponAmmoStrategy(unit, exploreStrategy, fightStrategy))
                );
    }

    @Override
    public double getOrder() {
        return delegate.getOrder();
    }

    @Override
    public Action getAction() {
        return delegate.getAction();
    }

    private List<Loot> getSuitableAmmoLoots(int weaponId) {
        return World.getInstance().getAmmoLoots(weaponId).stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return Strategy.toString(this, delegate);
    }




    public class LootNearestAmmoStrategy extends BaseLootStrategy {
        protected LootNearestAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
            super(unit, exploreStrategy, fightStrategy);
        }

        @Override
        public double getOrder() {
            return getBestLoot()
                    .map(ammo -> {
                        var dist = unit.getPosition().getDistanceTo(ammo.getPosition());
                        var maxBullets = unit.getMaxBulletCount();

                        var distMul = new LinearDistributor(0, maxLootDist, 1, 0)
                                .get(dist);
                        var countMul = new FirstMatchDistributor()
                                // 0.125 -- dist < MAX_DIST * 0.2
                                .add(val -> val < maxBullets * 0.8, new LinearDistributor(maxBullets * 0.2, maxBullets * 0.8, 1, 0.125))
                                .add(val -> true, new LinearDistributor(maxBullets * 0.8, maxBullets, 0.125, 0))
                                .get(unit.getBulletCount());

                        return distMul * countMul;
                    })
                    .orElse(0.);
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            return getSuitableAmmoLoots(unit.getWeapon().getId());
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }



    public class LootAmmoForceStrategy extends BaseLootStrategy {
        protected LootAmmoForceStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
            super(unit, exploreStrategy, fightStrategy);
        }

        @Override
        public double getOrder() {
            return MAX_ORDER;
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            return getSuitableAmmoLoots(unit.getWeapon().getId());
        }

        @Override
        protected Optional<Loot> getBestLoot() {
            var loots = getSuitableLoots().stream()
                    .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                    .collect(Collectors.toList());

            if (loots.isEmpty()) {
                return Optional.empty();
            }

            getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

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

        private ScoreContributor getPotentialFieldScoreContributor() {
            var enemyScoreContributors = World.getInstance().getAllEnemyUnits().stream()
//                    .filter(enemy -> enemy.isSpawned() && enemy.hasWeapon() && enemy.getBulletCount() > 0)
                    .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 15))
                    .collect(Collectors.toList());

            return new SumCompositeScoreContributor("loot ammo force")
                    .add(new ZoneScoreContributor(unit.getPotentialField()))
                    .add(enemyScoreContributors);
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }



    public class LootNonOwningWeaponAmmoStrategy extends BaseLootStrategy {
        protected LootNonOwningWeaponAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
            super(unit, exploreStrategy, fightStrategy);
        }

        @Override
        public double getOrder() {
//            if (World.getInstance().getZone().getRadius() > 160) {
//                return MIN_ORDER;
//            }

            return getBestLoot()
                    .map(ammo -> {
                        var weaponId = ((AmmoLoot)ammo).getWeaponId();

                        if (weaponId != 2) {
                            return 0.;
                        }

                        var dist = unit.getPosition().getDistanceTo(ammo.getPosition());
                        var maxBullets = Weapon.get(weaponId).getMaxBulletCount();

                        var distMul = new LinearDistributor(0, maxLootDist, 1, 0)
                                .get(dist);
                        var countMul = new FirstMatchDistributor()
                                // 0.125 -- dist < MAX_DIST * 0.2
//                                .add(val -> val < maxBullets * 0.5, new LinearDistributor(
//                                        maxBullets * 0.2, maxBullets * 0.5, 1, 0)
//                                )
//                                .add(val -> true, new LinearDistributor(maxBullets * 0.5, maxBullets, 0.125, 0))
                                .add(val -> true, new LinearDistributor(0, maxBullets * 0.5, 1, 0))
                                .get(unit.getBulletCount(weaponId));
                        var priority = Weapon.getPriority(weaponId);

                        return distMul * countMul / 2 * priority;
//                        return MIN_ORDER;
                    })
                    .orElse(0.);
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            var unitWeaponId = unit.getWeaponOptional().map(Weapon::getId).orElse(-1);

            return World.getInstance().getAmmoLoots().values().stream()
                    .filter(loot -> loot.getWeaponId() != unitWeaponId)
                    .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }
}
