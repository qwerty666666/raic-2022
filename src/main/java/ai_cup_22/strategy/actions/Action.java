package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;

public interface Action {
    void apply(Unit unit, UnitOrder order);
}
