package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.RotateAction;
import ai_cup_22.strategy.actions.basic.NullAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;

public class GoToPhantomEnemyStrategy implements Strategy {
    private final Unit unit;

    public GoToPhantomEnemyStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        var target = getTargetPhantomEnemy();
        if (target == null) {
            return MIN_ORDER;
        }

        return 0.2;
    }

    @Override
    public Action getAction() {
        var targetEnemy = getTargetPhantomEnemy();

        if (targetEnemy == null) {
            return new NullAction();
        }

        return new CompositeAction()
                .add(new MoveToWithPathfindingAction(targetEnemy.getPosition()))
                .add(new RotateAction());
    }

    private Unit getTargetPhantomEnemy() {
        return World.getInstance().getPhantomEnemies().values().stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)))
                .orElse(null);
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
