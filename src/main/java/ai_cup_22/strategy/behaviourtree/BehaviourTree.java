package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.strategies.composite.AndStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.DodgeBulletsStrategy;
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
    private DodgeBulletsStrategy dodgeBulletsStrategy;
    private Unit unit;

    public BehaviourTree(Unit unit) {
        this.unit = unit;
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit);
        lootAmmoStrategy = new LootAmmoStrategy(unit, exploreStrategy,fightStrategy);
        lootShieldStrategy = new LootShieldStrategy(unit, exploreStrategy, fightStrategy);
        takeShieldPotionStrategy = new TakeShieldPotionStrategy(unit);
        retreatStrategy = new RetreatStrategy(unit);
        regenerateHealthStrategy = new RegenerateHealthStrategy(unit, retreatStrategy);
        dodgeBulletsStrategy = new DodgeBulletsStrategy(unit);
    }

    public Strategy getStrategy() {
        return new AndStrategy()
                .add(takeShieldPotionStrategy)
                .add(new FirstMatchCompositeStrategy()
                        // force loot ammo
                        .add(() -> unit.getBulletCount() == 0, lootAmmoStrategy)
                        // fight
                        .add(() -> isThereAreEnemiesInViewRange(unit), new AndStrategy()
                                .add(new FirstMatchCompositeStrategy()
                                        // I am on safe dist from enemies
                                        .add(() -> fightStrategy.isOnSafeDistance(), new MaxOrderCompositeStrategy()
                                                .add(new LootAmmoStrategy(unit, exploreStrategy, fightStrategy, 30))
                                                .add(new LootShieldStrategy(unit, exploreStrategy, fightStrategy, 30))
                                                .add(fightStrategy)
                                        )
                                        // I can fight with enemies
                                        .add(() -> true, new FirstMatchCompositeStrategy()
                                                .add(() -> unit.canDoNewAction(), fightStrategy)
                                                .add(() -> true, retreatStrategy)
                                        )
                                )
                                .add(regenerateHealthStrategy)
                        )
                        // just explore
                        .add(() -> true, new MaxOrderCompositeStrategy()
                                .add(lootAmmoStrategy)
                                .add(lootShieldStrategy)
                                .add(exploreStrategy)
                        )
                )
                .add(dodgeBulletsStrategy);
    }

    private boolean isThereAreEnemiesInViewRange(Unit unit) {
        return !World.getInstance().getEnemyUnits().isEmpty();
    }
}
