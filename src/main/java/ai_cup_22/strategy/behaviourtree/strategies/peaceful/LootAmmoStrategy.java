package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.NullStrategy;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.distributions.FirstMatchDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootAmmoStrategy implements Strategy {
    public static final double MAX_AMMO_DIST = 100;

    private final Unit unit;
    private final ExploreStrategy exploreStrategy;
    private final Strategy delegate;

    public LootAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.exploreStrategy = exploreStrategy;
        this.delegate = new FirstMatchCompositeStrategy()
                .add(() -> !unit.hasWeapon(), new NullStrategy())
                .add(() -> unit.getBulletCount() == 0, new LootAmmoForceStrategy())
                .add(() -> true, new LootNearestAmmoStrategy());
    }

    @Override
    public double getOrder() {
        return delegate.getOrder();
    }

    @Override
    public Action getAction() {
        return delegate.getAction();
    }

    private List<AmmoLoot> getSuitableAmmoLoots() {
        return World.getInstance().getAmmoLoots(unit.getWeapon().getId()).stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < MAX_AMMO_DIST)
                .toList();
    }



    public class LootNearestAmmoStrategy implements Strategy {
        @Override
        public double getOrder() {
            return getNearestAmmoLoot()
                    .filter(this::canTakeLootOnlyAfterDisabledTime)
                    .map(ammo -> {
                        var dist = unit.getPosition().getDistanceTo(ammo.getPosition());
                        var maxBullets = unit.getMaxBulletCount();

                        var distMul = new LinearDistributor(0, MAX_AMMO_DIST, 1, 0)
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
        public Action getAction() {
            return getNearestAmmoLoot()
                    .map(ammo -> (Action) new TakeLootAction(ammo))
                    .orElse(exploreStrategy.getAction());
        }

        private Optional<AmmoLoot> getNearestAmmoLoot() {
            return getSuitableAmmoLoots().stream()
                    .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())));
        }

        private boolean canTakeLootOnlyAfterDisabledTime(Loot loot) {
            return loot.getPosition().getDistanceTo(unit.getPosition()) >=
                    unit.getTicksToNewActionBeAvailable() * unit.getMaxForwardSpeedPerTick();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " (" + getOrder() + ") \n";
        }
    }




    public class LootAmmoForceStrategy implements Strategy {
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

            getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

            return new TakeLootAction(getBestAmmo());
        }

        private Loot getBestAmmo() {
            var pathFinder = new AStarPathFinder(unit.getPotentialField());

            var paths = getSuitableAmmoLoots().stream()
                    .collect(Collectors.toMap(
                            loot -> loot,
                            loot -> pathFinder.findPath(unit.getPosition(), loot.getPosition())
                    ));

            // search by min sum of treats on the path
            // and min by distance if there is no treat on the path
            return paths.entrySet().stream()
                    .min(
                            Comparator.comparingDouble(
                                            (Entry<AmmoLoot, Path> e) -> e.getValue().getScores().stream()
                                                    .filter(score -> score.getNonStaticScore() < 0)
                                                    .mapToDouble(Score::getNonStaticScore)
                                                    .sum()
                                    )
                                    .reversed()
                                    .thenComparingDouble((Entry<AmmoLoot, Path> e) -> e.getValue().getDistance())
                    )
                    .orElseThrow()
                    .getKey();
        }

        private ScoreContributor getPotentialFieldScoreContributor() {
            var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                    .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 15))
                    .toList();

            return new SumCompositeScoreContributor()
                    .add(new ZoneScoreContributor())
                    .add(enemyScoreContributors);
        }

        @Override
        public String toString() {
            return Strategy.toString(this);
        }
    }

    @Override
    public String toString() {
        return Strategy.toString(this, delegate);
    }
}
