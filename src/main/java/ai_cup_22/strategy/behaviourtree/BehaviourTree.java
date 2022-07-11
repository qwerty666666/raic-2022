package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.models.Unit;

public class BehaviourTree {
    private ExploreStrategy exploreStrategy;
    private FightStrategy fightStrategy;

    public BehaviourTree(Unit unit) {
        exploreStrategy = new ExploreStrategy(unit);
        fightStrategy = new FightStrategy(unit);
    }

    public Strategy getStrategy() {
        if (!World.getInstance().getEnemyUnits().isEmpty()) {
            return fightStrategy;
        }

        return exploreStrategy;
    }
}
