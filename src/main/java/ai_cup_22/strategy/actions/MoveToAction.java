package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import java.util.ArrayList;
import java.util.List;

public class MoveToAction implements Action {
    private final Position target;

    public MoveToAction(Position target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var velocity = new Vector(unit.getPosition(), target);

        unit.setCurrentPath(List.of(unit.getPosition(), target));

        order.setTargetVelocity(velocity.normalizeToLength(10).toVec2());
    }
}
