package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.utils.MovementUtils;

public class ShootAction implements Action {
    private final Unit target;

    public ShootAction(Unit target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var bestPositionToShoot = getBestPositionToShoot(unit, target);
        var shouldShoot = shouldShoot(unit, bestPositionToShoot, target);

        new CompositeAction()
                .add(new AimAction(shouldShoot))
                .add(new LookToAction(bestPositionToShoot))
                .apply(unit, order);
    }

    private boolean shouldShoot(Unit me, Position targetPosition, Unit enemy) {
        if (enemy.isPhantom()) {
            return false;
        }

        if (!me.canDoNewAction() && me.isCoolDown()) {
            return false;
        }

        if (!me.getShootingSegment().contains(targetPosition)) {
            return false;
        }

        if (!me.canShoot(targetPosition, enemy)) {
            return false;
        }

        var bulletMaxDistance = me.getWeaponOptional().map(Weapon::getMaxDistance).orElse(0.);
        return me.getPosition().getDistanceTo(targetPosition) < bulletMaxDistance + 2;
    }

    private Position getBestPositionToShoot(Unit me, Unit enemy) {
        if (enemy.isPhantom()) {
            return enemy.getPosition();
        }

        var dist = me.getDistanceTo(enemy) - me.getCircle().getRadius();
        var bulletSpeed = me.getWeaponOptional().map(Weapon::getSpeedPerTick).orElse(0.);

        var ticksToHit = (int) Math.ceil(dist / bulletSpeed);

        var trajectory = new Vector(enemy.getPosition(), me.getPosition()).normalizeToLength(100);
        var dodgeDirection1 = trajectory.rotate(Math.PI / 2);
        var dodgeDirection2 = trajectory.rotate(-Math.PI / 2);

        var pos1 = MovementUtils.getMaxPositionIfWalkDirect(enemy, dodgeDirection1, ticksToHit);
        var pos2 = MovementUtils.getMaxPositionIfWalkDirect(enemy, dodgeDirection2, ticksToHit);

        return new Line(pos1, pos2).getMiddlePoint();
    }
}
