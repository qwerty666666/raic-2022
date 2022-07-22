package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.simulation.dodgebullets.DodgeSimulation;
import java.util.Collections;

public class DodgeBulletsAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var dodgeDirection = new DodgeSimulation().simulate(unit);

        if (dodgeDirection != null) {
            unit.setCurrentPath(Collections.emptyList());

            new MoveToAction(unit.getPosition().move(dodgeDirection.getDirection())).apply(unit, order);

            if (!dodgeDirection.isWithAim()) {
                order.setAction(null);
            }

            if (dodgeDirection.isWithRotateToDirection()) {
                new LookToAction(dodgeDirection.getDirection()).apply(unit, order);
            }
        }
    }
}
