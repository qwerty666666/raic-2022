package ai_cup_22.strategy.actions;

import ai_cup_22.model.ActionOrder.Pickup;
import ai_cup_22.model.UnitOrder;
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
            new MoveToAction(loot.getPosition()).apply(unit, order);
            order.setAction(new Pickup(loot.getId()));
        } else {
            // by default move to given loot
            new MoveToWithPathfindingAction(loot.getPosition()).apply(unit, order);
        }
    }
}
