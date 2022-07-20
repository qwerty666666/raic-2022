package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.NullStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.distributions.FirstMatchDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
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
                .add(() -> true, new LootNearestAmmoStrategy(unit, exploreStrategy, fightStrategy));
    }

    @Override
    public double getOrder() {
        return delegate.getOrder();
    }

    @Override
    public Action getAction() {
        return delegate.getAction();
    }

    private List<Loot> getSuitableAmmoLoots() {
        return World.getInstance().getAmmoLoots(unit.getWeapon().getId()).stream()
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
                    .filter(this::canTakeLootOnlyAfterDisabledTime)
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
            return getSuitableAmmoLoots();
        }

        private boolean canTakeLootOnlyAfterDisabledTime(Loot loot) {
            return loot.getPosition().getDistanceTo(unit.getPosition()) >=
                    unit.getTicksToNewActionBeAvailable() * unit.getMaxForwardSpeedPerTick();
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
        public Action getAction() {
            // TODO it is better to hide instead
            if (getSuitableAmmoLoots().isEmpty()) {
                return exploreStrategy.getAction();
            }

            return super.getAction();
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            return getSuitableAmmoLoots();
        }

        @Override
        protected Position getLookToPosition(Loot loot) {
            return loot.getPosition();
        }

        @Override
        protected Optional<Loot> getBestLoot() {
            getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

            var pathFinder = new AStarPathFinder(unit.getPotentialField());

            var paths = getSuitableLoots().stream()
                    .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
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
                    .get()
                    .getKey();

            return Optional.ofNullable(loot);
        }

        private ScoreContributor getPotentialFieldScoreContributor() {
            var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                    .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 15))
                    .collect(Collectors.toList());

            return new SumCompositeScoreContributor("loot ammo force")
                    .add(new ZoneScoreContributor())
                    .add(enemyScoreContributors);
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }
}
