package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.simulation.walk.WalkSimulation;

public class ShootAction implements Action {
    private final Unit target;
    private final Unit me;
    private final Position bestPositionToShoot;
    private final boolean shouldShoot;

    public ShootAction(Unit me, Unit target) {
        this.me = me;
        this.target = target;
        this.bestPositionToShoot = getBestPositionToShoot(me, target);
        this.shouldShoot = shouldShoot(me, bestPositionToShoot, target);
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        new CompositeAction()
                .add(new AimAction(shouldShoot))
                .add(new LookToAction(bestPositionToShoot))
                .apply(unit, order);
    }

    public Position getBestPositionToShoot() {
        return bestPositionToShoot;
    }

    public boolean isShouldShoot() {
        return shouldShoot;
    }

    private boolean shouldShoot(Unit me, Position targetPosition, Unit enemy) {
       if (!canShootToUnit(targetPosition, enemy)) {
           return false;
       }

        if (!me.canDoNewAction() || me.isCoolDown()) {
            return false;
        }

        if (!me.getShootingSegment().contains(targetPosition)) {
            return false;
        }

        return true;
    }

    public boolean canShootToUnit(Position targetPosition, Unit enemy) {
        if (enemy.isPhantom() || !enemy.isSpawned()) {
            return false;
        }

        if (!me.canShoot(targetPosition, enemy)) {
            return false;
        }

        return true;
    }

    public Unit getTarget() {
        return target;
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

        var pos1 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection1, ticksToHit);
        var pos2 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection2, ticksToHit);

        return new Line(pos1, pos2).getMiddlePoint();
    }
}
