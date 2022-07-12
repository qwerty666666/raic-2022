package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.ActionOrder.UseShieldPotion;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.models.Unit;

public class TakeShieldPotionAction extends OrderAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.canDoNewAction() && unit.getShieldPotions() > 0 && unit.getShield() < unit.getMaxShield()) {
            unit.setLastAction(this);
            order.setAction(new UseShieldPotion());
        }
    }
}
