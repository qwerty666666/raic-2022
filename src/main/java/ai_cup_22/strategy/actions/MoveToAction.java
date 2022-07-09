package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;

public class MoveToAction implements Action {
    private final Position target;

    public MoveToAction(Position target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var velocity = new Vector(unit.getPosition(), target);

        order.setTargetVelocity(velocity.normalizeToLength(10).toVec2());
    }
}
