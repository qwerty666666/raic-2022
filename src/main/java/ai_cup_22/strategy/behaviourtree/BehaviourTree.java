package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.models.Unit;

public class BehaviourTree {
    private ExploreStrategy exploreStrategy;
    private FightStrategy fightStrategy;
    private LootAmmoStrategy lootAmmoStrategy;
    private Unit unit;

    public BehaviourTree(Unit unit) {
        this.unit = unit;
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit);
        lootAmmoStrategy = new LootAmmoStrategy(unit, exploreStrategy);
    }

    public Strategy getStrategy() {
        return new FirstMatchCompositeStrategy()
                .add(() -> unit.getBulletCount() == 0, lootAmmoStrategy)
                .add(() -> !World.getInstance().getEnemyUnits().isEmpty(), fightStrategy)
                .add(() -> true, new MaxOrderCompositeStrategy()
                        .add(lootAmmoStrategy)
                        .add(exploreStrategy)
                );
    }
}
