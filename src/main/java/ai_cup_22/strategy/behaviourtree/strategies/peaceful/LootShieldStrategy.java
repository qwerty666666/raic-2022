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
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import java.util.List;
import java.util.stream.Collectors;

public class LootShieldStrategy implements Strategy {
    private final Unit unit;
    private final Strategy delegate;
    private final double maxLootDist;

    public LootShieldStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
        this(unit, exploreStrategy, fightStrategy, Constants.MAX_LOOT_STRATEGY_DIST);
    }

    public LootShieldStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy, double maxLootDist) {
        this.unit = unit;
        this.maxLootDist = maxLootDist;

        this.delegate = new FirstMatchCompositeStrategy()
                .add(() -> unit.getShieldPotions() == unit.getMaxShieldPotions(), new NullStrategy())
                .add(() -> true, new LootNearestShieldStrategy(unit, exploreStrategy, fightStrategy));
    }

    @Override
    public double getOrder() {
        return delegate.getOrder();
    }

    @Override
    public Action getAction() {
        return delegate.getAction();
    }

    private List<Loot> getSuitableLoots() {
        return World.getInstance().getShieldLoots().values().stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return Strategy.toString(this, delegate);
    }



    public class LootNearestShieldStrategy extends BaseLootStrategy {
        protected LootNearestShieldStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
            super(unit, exploreStrategy, fightStrategy);
        }

        @Override
        public double getOrder() {
            return getBestLoot()
                    .filter(this::canTakeLootOnlyAfterDisabledTime)
                    .map(loot -> {
                        var dist = unit.getPosition().getDistanceTo(loot.getPosition());

                        var distMul = new LinearDistributor(0, maxLootDist, 0.99, 0)
                                .get(dist);
                        var countMul = new FirstMatchDistributor()
                                // 0.15 -- dist < MAX_DIST * 0.3
//                                .add(val -> val < 5, new LinearDistributor(2, 5, 1, 0.15))
//                                .add(val -> val < unit.getMaxShieldPotions(), new ConstDistributor(0.15))
                                .add(val -> true, new LinearDistributor(2, unit.getMaxShieldPotions(), 0.99, 0))
                                .get(unit.getShieldPotions());

                        return distMul * countMul;
                    })
                    .orElse(0.);
        }

        @Override
        protected List<Loot> getSuitableLoots() {
            return LootShieldStrategy.this.getSuitableLoots();
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
}
