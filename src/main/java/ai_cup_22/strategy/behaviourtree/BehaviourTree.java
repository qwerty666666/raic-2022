package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.models.Unit;

public class BehaviourTree {
    private ExploreStrategy exploreStrategy;
    private FightStrategy fightStrategy;
    private TakeAmmoStrategy takeAmmoStrategy;
    private Unit unit;

    public BehaviourTree(Unit unit) {
        this.unit = unit;
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit);
        takeAmmoStrategy = new TakeAmmoStrategy(unit, exploreStrategy);
    }

    public Strategy getStrategy() {
        if (unit.getBulletCount() == 0) {
            return takeAmmoStrategy;
        }

        if (!World.getInstance().getEnemyUnits().isEmpty()) {
            return fightStrategy;
        }

        return exploreStrategy;
    }
}
