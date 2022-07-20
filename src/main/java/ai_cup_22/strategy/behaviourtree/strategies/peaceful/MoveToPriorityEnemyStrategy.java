package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.NullAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.models.Unit;

public class MoveToPriorityEnemyStrategy implements Strategy {
    private final Unit unit;
    private final FightStrategy fightStrategy;

    public MoveToPriorityEnemyStrategy(Unit unit, FightStrategy fightStrategy) {
        this.unit = unit;
        this.fightStrategy = fightStrategy;
    }

    @Override
    public double getOrder() {
        if (fightStrategy.getPriorityEnemy() == null) {
            return MIN_ORDER;
        }

        return Constants.STRATEGY_MOVE_TO_PRIORITY_ENEMY_ORDER;
    }

    @Override
    public Action getAction() {
        var targetEnemy = fightStrategy.getPriorityEnemy();

        if (targetEnemy != null) {
            return new CompositeAction()
                    .add(new MoveToWithPathfindingAction(targetEnemy.getPosition()))
                    .add(new LookToAction(targetEnemy));

        }

        return new NullAction();
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
