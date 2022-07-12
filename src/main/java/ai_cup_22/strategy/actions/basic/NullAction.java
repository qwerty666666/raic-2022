package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.models.Unit;

public class NullAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
    }
}
