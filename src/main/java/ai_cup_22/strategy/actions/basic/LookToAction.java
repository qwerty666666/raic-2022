package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;

public class LookToAction implements Action {
    private Position target;
    private Vector vector;
    private boolean setUnitLookTo = true;

    public LookToAction(Unit target) {
        this(target.getPosition());
    }

    public LookToAction(Position target) {
        this.target = target;
    }

    public LookToAction(Position target, boolean setUnitLookTo) {
        this.target = target;
        this.setUnitLookTo = setUnitLookTo;
    }

    public LookToAction(Vector vector) {
        this.vector = vector;
    }

    public LookToAction(Vector vector, boolean setUnitLookTo) {
        this(vector);
        this.setUnitLookTo = setUnitLookTo;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        Vector targetVector;

        if (vector != null) {
            targetVector = vector;
            if (setUnitLookTo) {
                unit.setLookPosition(unit.getPosition().move(vector.normalizeToLength(10)));
            }
        } else {
            targetVector = new Vector(unit.getPosition(), target);
            if (setUnitLookTo) {
                unit.setLookPosition(target);
            }
        }

        order.setTargetDirection(targetVector.normalizeToLength(10).toVec2());
    }
}
