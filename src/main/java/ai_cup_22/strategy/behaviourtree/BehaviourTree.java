package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.strategies.composite.AndStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.RegenerateHealthStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.RetreatStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.ExploreStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootAmmoStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootShieldStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.TakeShieldPotionStrategy;
import ai_cup_22.strategy.models.Unit;

public class BehaviourTree {
    private ExploreStrategy exploreStrategy;
    private FightStrategy fightStrategy;
    private LootAmmoStrategy lootAmmoStrategy;
    private LootShieldStrategy lootShieldStrategy;
    private TakeShieldPotionStrategy takeShieldPotionStrategy;
    private RetreatStrategy retreatStrategy;
    private RegenerateHealthStrategy regenerateHealthStrategy;
    private Unit unit;

    public BehaviourTree(Unit unit) {
        this.unit = unit;
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit);
        lootAmmoStrategy = new LootAmmoStrategy(unit, exploreStrategy);
        lootShieldStrategy = new LootShieldStrategy(unit, exploreStrategy);
        takeShieldPotionStrategy = new TakeShieldPotionStrategy(unit);
        retreatStrategy = new RetreatStrategy(unit);
        regenerateHealthStrategy = new RegenerateHealthStrategy(unit, retreatStrategy);
    }

    public Strategy getStrategy() {
        return new AndStrategy()
                .add(takeShieldPotionStrategy)
                .add(new FirstMatchCompositeStrategy()
                        .add(() -> unit.getBulletCount() == 0, lootAmmoStrategy)
                        .add(() -> !World.getInstance().getEnemyUnits().isEmpty(), new FirstMatchCompositeStrategy()
                                .add(() -> regenerateHealthStrategy.getOrder() == Strategy.MAX_ORDER, regenerateHealthStrategy)
                                .add(() -> unit.canDoNewAction(), fightStrategy)
                                .add(() -> true, retreatStrategy)
                        )
                        .add(() -> true, new MaxOrderCompositeStrategy()
                                .add(lootAmmoStrategy)
                                .add(lootShieldStrategy)
                                .add(exploreStrategy)
                        )
                );
    }
}
