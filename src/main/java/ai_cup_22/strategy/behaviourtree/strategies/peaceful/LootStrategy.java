package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootAmmoStrategy.LootNonOwningWeaponAmmoStrategy;
import ai_cup_22.strategy.models.Unit;

public class LootStrategy implements Strategy {
    private final Strategy delegate;
    private final LootWeaponStrategy lootWeaponStrategy;

    public LootStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
        this(unit, exploreStrategy, fightStrategy, Constants.MAX_LOOT_STRATEGY_DIST);
    }

    public LootStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy, double maxLootDist) {
        lootWeaponStrategy = new LootWeaponStrategy(unit, exploreStrategy, fightStrategy, maxLootDist);

        delegate = new MaxOrderCompositeStrategy()
                        .add(lootWeaponStrategy)
                        .add(new LootShieldStrategy(unit, exploreStrategy, fightStrategy, maxLootDist))
                        .add(new LootAmmoStrategy(unit, exploreStrategy, fightStrategy, maxLootDist));
    }

    @Override
    public double getOrder() {
        return delegate.getOrder();
    }

    @Override
    public Action getAction() {
        return delegate.getAction();
    }

    @Override
    public String toString() {
        return Strategy.toString(this, delegate);
    }
}
