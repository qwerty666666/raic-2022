package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.strategies.composite.AndStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.MaxOrderCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.DodgeBulletsStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.GoToPhantomEnemyStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.RegenerateHealthStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.RetreatStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.ExploreStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootAmmoStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootShieldStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.SpawnStrategy;
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
    private GoToPhantomEnemyStrategy goToPhantomEnemyStrategy;
    private SpawnStrategy spawnStrategy;
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
        goToPhantomEnemyStrategy = new GoToPhantomEnemyStrategy(unit);
        spawnStrategy = new SpawnStrategy(unit, fightStrategy, exploreStrategy);
    }

    public Strategy getStrategy() {
        return new FirstMatchCompositeStrategy()
                // spawning
                .add(() -> isSpawning(unit), spawnStrategy)
                // alive
                .add(() -> true, new AndStrategy()
                        .add(takeShieldPotionStrategy)
                        .add(new FirstMatchCompositeStrategy()
                                // force loot ammo
                                .add(() -> unit.getBulletCount() == 0, lootAmmoStrategy)
                                // fight
                                .add(() -> isThereAreEnemiesAround(unit), new AndStrategy()
                                        .add(new FirstMatchCompositeStrategy()
                                                // I am on safe dist from enemies
                                                .add(() -> fightStrategy.isOnSafeDistance(), new MaxOrderCompositeStrategy()
                                                        .add(new LootAmmoStrategy(unit, exploreStrategy, fightStrategy, Constants.SAFE_DIST))
                                                        .add(new LootShieldStrategy(unit, exploreStrategy, fightStrategy, Constants.SAFE_DIST))
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
                                // go to phantom enemy
                                .add(() -> isThereArePhantomEnemies(unit), new MaxOrderCompositeStrategy()
                                        .add(goToPhantomEnemyStrategy)
                                        .add(regenerateHealthStrategy)
                                        .add(lootShieldStrategy)
                                        .add(lootAmmoStrategy)
                                )
                                // just explore
                                .add(() -> true, new MaxOrderCompositeStrategy()
                                        .add(lootAmmoStrategy)
                                        .add(lootShieldStrategy)
                                        .add(exploreStrategy)
                                )
                        )
                        .add(dodgeBulletsStrategy)
                );
    }

    private boolean isThereAreEnemiesAround(Unit unit) {
        return World.getInstance().getAllEnemyUnits().stream()
                .anyMatch(enemy -> enemy.getDistanceTo(unit) < 50);
    }

    private boolean isSpawning(Unit unit) {
        return !unit.isSpawned();
    }

    private boolean isThereArePhantomEnemies(Unit unit) {
        return !World.getInstance().getPhantomEnemies().isEmpty();
    }
}
