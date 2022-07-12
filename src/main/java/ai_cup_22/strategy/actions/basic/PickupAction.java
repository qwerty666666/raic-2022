package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.ActionOrder.Pickup;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;

public class PickupAction extends OrderAction implements Action {
    private final Loot loot;

    public PickupAction(Loot loot) {
        this.loot = loot;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.canDoNewAction() && unit.canTakeLoot(loot)) {
            unit.setLastAction(this);
            targetId = loot.getId();

            order.setAction(new Pickup(loot.getId()));
        }
    }

    @Override
    public void updateTick(Unit unit, ai_cup_22.model.Action action) {
        super.updateTick(unit, action);
        unit.setLastLootingTick(startTick);
    }
}
