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
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.LootStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.MoveToPriorityEnemyStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.SpawnStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.peaceful.TakeShieldPotionStrategy;
import ai_cup_22.strategy.models.Unit;

public class BehaviourTree {
    private ExploreStrategy exploreStrategy;
    private FightStrategy fightStrategy;
    private LootStrategy lootStrategy;
    private TakeShieldPotionStrategy takeShieldPotionStrategy;
    private RetreatStrategy retreatStrategy;
    private RegenerateHealthStrategy regenerateHealthStrategy;
    private DodgeBulletsStrategy dodgeBulletsStrategy;
    private SpawnStrategy spawnStrategy;
    private MoveToPriorityEnemyStrategy moveToPriorityEnemyStrategy;
    private Unit unit;

    public BehaviourTree(Unit unit) {
        this.unit = unit;
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit, exploreStrategy);
        lootStrategy = new LootStrategy(unit, exploreStrategy,fightStrategy);
        takeShieldPotionStrategy = new TakeShieldPotionStrategy(unit);
        retreatStrategy = new RetreatStrategy(unit);
        regenerateHealthStrategy = new RegenerateHealthStrategy(unit, retreatStrategy);
        dodgeBulletsStrategy = new DodgeBulletsStrategy(unit);
        spawnStrategy = new SpawnStrategy(unit, fightStrategy, exploreStrategy);
        moveToPriorityEnemyStrategy = new MoveToPriorityEnemyStrategy(unit, fightStrategy);
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
                                .add(() -> unit.getBulletCount() == 0, lootStrategy)
                                // fight
                                .add(() -> isThereAreEnemiesAround(unit), new AndStrategy()
                                        .add(new FirstMatchCompositeStrategy()
                                                // I am on safe dist from enemies
                                                .add(() -> fightStrategy.isOnSafeDistance(), new MaxOrderCompositeStrategy()
                                                        .add(lootStrategy)
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
                                // I am safe
                                .add(() -> true, new MaxOrderCompositeStrategy()
                                        .add(lootStrategy)
                                        .add(moveToPriorityEnemyStrategy)
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

    public FightStrategy getFightStrategy() {
        return fightStrategy;
    }
}
