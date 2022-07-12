package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.actions.basic.PickupAction;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;

public class TakeLootAction implements Action {
    private final Loot loot;

    public TakeLootAction(Loot loot) {
        this.loot = loot;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.canTakeLoot(loot)) {
            new CompositeAction()
                    .add(new MoveToAction(loot.getPosition()))
                    .add(new PickupAction(loot))
                    .apply(unit, order);
        } else {
            // by default move to given loot
            new MoveToWithPathfindingAction(loot.getPosition()).apply(unit, order);
        }
    }
}
