package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.models.Unit;
import java.util.ArrayList;
import java.util.List;

public class CompositeAction implements Action {
    private final List<Action> actions = new ArrayList<>();

    public CompositeAction add(Action action) {
        this.actions.add(action);

        return this;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        actions.forEach(action -> action.apply(unit, order));
    }
}
