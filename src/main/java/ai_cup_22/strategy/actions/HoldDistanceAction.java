package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import java.util.List;

public class HoldDistanceAction implements Action {
    private final Position target;
    private final double distance;

    public HoldDistanceAction(Position target, double distance) {
        this.target = target;
        this.distance = distance;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        Vector velocity;

        if (unit.getCircle().getCenter().getDistanceTo(target) < distance) {
            velocity = new Vector(unit.getPosition(), target).reverse();
        } else {
            velocity = new Vector(unit.getPosition(), target);
        }

//        unit.setCurrentPath(List.of(unit.getPosition(), target));

        order.setTargetVelocity(velocity.normalizeToLength(10).toVec2());
    }
}
