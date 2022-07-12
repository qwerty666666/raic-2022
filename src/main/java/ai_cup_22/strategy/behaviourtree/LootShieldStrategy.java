package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.distributions.ConstDistributor;
import ai_cup_22.strategy.distributions.FirstMatchDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LootShieldStrategy implements Strategy {
    public static final double MAX_LOOT_DIST = 100;

    private final Unit unit;
    private final ExploreStrategy exploreStrategy;
    private final Strategy delegate;

    public LootShieldStrategy(Unit unit, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.exploreStrategy = exploreStrategy;
        this.delegate = new FirstMatchCompositeStrategy()
                .add(() -> unit.getShieldPotions() == unit.getMaxShieldPotions(), new NullStrategy())
                .add(() -> true, new LootNearestShieldStrategy());
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
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < MAX_LOOT_DIST)
                .toList();
    }

    @Override
    public String toString() {
        return Strategy.toString(this, delegate);
    }



    public class LootNearestShieldStrategy implements Strategy {
        @Override
        public double getOrder() {
            return getNearestLoot()
                    .map(loot -> {
                        var dist = unit.getPosition().getDistanceTo(loot.getPosition());

                        var distMul = new LinearDistributor(0, MAX_LOOT_DIST, 1, 0)
                                .get(dist);
                        var countMul = new FirstMatchDistributor()
                                // 0.15 -- dist < MAX_DIST * 0.3
                                .add(val -> val < 5, new LinearDistributor(2, 5, 1, 0.15))
                                .add(val -> val < unit.getMaxShieldPotions(), new ConstDistributor(0.15))
                                .get(unit.getShieldPotions());

                        return distMul * countMul;
                    })
                    .orElse(0.);
        }

        @Override
        public Action getAction() {
            return getNearestLoot()
                    .map(loot -> (Action) new TakeLootAction(loot))
                    .orElse(exploreStrategy.getAction());
        }

        private Optional<Loot> getNearestLoot() {
            return getSuitableLoots().stream()
                    .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " (" + getOrder() + ") \n";
        }
    }
}
