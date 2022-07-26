package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.simulation.walk.WalkSimulation;
import java.util.Comparator;
import java.util.stream.Collectors;

public class LookBackAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var unitToUpdate = getUnitToUpdate(unit);

        if (unitToUpdate != null) {
            var curLookTarget = unit.getLookPosition();
            var lookTo = getLookToVector(unit, unitToUpdate, curLookTarget != null ? curLookTarget : unitToUpdate.getPosition());

            unit.setLookBackPosition(unitToUpdate.getPosition());
            unit.setLookBackVector(lookTo.normalizeToLength(10));

            new LookToAction(lookTo, false).apply(unit, order);
        }
    }

    private Unit getUnitToUpdate(Unit me) {
        var obstacles = World.getInstance().getNonLookThroughObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getSquareDistanceTo(me.getPosition()) < Constants.USER_VIEW_DIST * Constants.USER_VIEW_DIST)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        return World.getInstance().getAllEnemyUnits().stream()
                .filter(Unit::isSpawned)
                .filter(enemy -> enemy.getDistanceTo(me) < Constants.USER_VIEW_DIST)
                .filter(enemy -> enemy.hasWeapon() && enemy.getBulletCount() > 0)
                .filter(enemy -> obstacles.stream().noneMatch(o -> o.isIntersect(new Line(me.getPosition(), enemy.getPosition()))))
                .min(
                        Comparator.comparing((Unit enemy) -> {
                            var dist = enemy.getDistanceTo(me) - 2;
                            var bulletTicks = Math.ceil(dist / enemy.getWeapon().getSpeedPerTick());
                            var cdTicks = enemy.getRemainingCoolDownTicks();
                            var rotateTicks = WalkSimulation.getTicksToRotateWithAim(me, enemy.getPosition(), false);

                            if (enemy.isPhantom() && cdTicks > 0 &&
                                    enemy.getWeapon().getCoolDownTicks() - cdTicks < bulletTicks) {
                                return true;
                            }

                            return cdTicks < rotateTicks + 4;
                        }).reversed()
//                                .thenComparingInt(Unit::getRemainingCoolDownTicks)
                                .thenComparingDouble(enemy -> {
                                    return new Vector(me.getPosition(), enemy.getPosition()).getAngleTo(me.getDirection());
                                })
                )
                .orElse(null);
    }

    private Vector getLookToVector(Unit me, Unit enemyToUpdate, Position targetLookPosition) {
        var viewSegmentOnNextTick = getViewSegmentOnNextTick(me);

        var targetVector = new Vector(me.getPosition(), targetLookPosition);
        var lookBackVector = new Vector(me.getPosition(), enemyToUpdate.getPosition());

        if (targetVector.getAngleTo(lookBackVector) < viewSegmentOnNextTick.getAngle() / 2) {
            // look to target
            return targetVector;
        } else {
            // look back
            return lookBackVector.rotate((viewSegmentOnNextTick.getAngle() / 2 - Math.PI / 180 * 5) *
                    Math.signum(lookBackVector.getDiffToVector(targetVector)));
        }
    }

    private CircleSegment getViewSegmentOnNextTick(Unit unit) {
        var fieldOfView = Math.toRadians(World.getInstance().getConstants().getFieldOfView());
        var aimFieldOfView = unit.getWeaponOptional()
                .map(weapon -> Math.toRadians(weapon.getAimFieldOfView()))
                .orElse(fieldOfView);
        var aim = WalkSimulation.getAimOnNextTick(unit.getAim(), unit.getAimChangePerTick(), unit.isAiming());

        return new CircleSegment(
                new Circle(unit.getPosition(), Constants.USER_VIEW_DIST),
                unit.getDirection().getAngle(),
                fieldOfView - (fieldOfView - aimFieldOfView) * aim
        );
    }
}
