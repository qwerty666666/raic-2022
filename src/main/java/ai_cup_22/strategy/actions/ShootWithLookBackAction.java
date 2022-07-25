package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.simulation.walk.WalkSimulation;

public class ShootWithLookBackAction implements Action {
    private final ShootAction shootAction;

    public ShootWithLookBackAction(Unit me, Unit targetEnemy) {
        shootAction = new ShootAction(me, targetEnemy);
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        shootAction.apply(unit, order);

        var positionToShoot = shootAction.getBestPositionToShoot();
        var diffAngle = unit.getDirection().getAngleTo(new Vector(unit.getPosition(), positionToShoot));
        var ticksToRotate = WalkSimulation.getTicksToRotate(diffAngle, unit.getAim(), unit.getAimChangePerTick(),
                true, unit.getAimRotationSpeed());

        if (ticksToRotate < unit.getRemainingCoolDownTicks() - 5 ||
                !shootAction.canShootToUnit(shootAction.getBestPositionToShoot(), shootAction.getTarget())) {
            new LookBackAction().apply(unit, order);
        }
    }
}
