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

            new LookToAction(lookTo).apply(unit, order);
        }
    }

    private Unit getUnitToUpdate(Unit me) {
        var obstacles = World.getInstance().getNonLookThroughObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getSquareDistanceTo(me.getPosition()) < Constants.USER_VIEW_DIST * Constants.USER_VIEW_DIST)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        return World.getInstance().getAllEnemyUnits().stream()
                .filter(enemy -> enemy.getDistanceTo(me) < Constants.USER_VIEW_DIST)
                .filter(enemy -> obstacles.stream().noneMatch(o -> o.isIntersect(new Line(me.getPosition(), enemy.getPosition()))))
                .min(Comparator.comparingInt(Unit::getLastSeenTick)
                        .thenComparingDouble(enemy -> {
                            return new Vector(enemy.getPosition(), me.getPosition()).getAngleTo(me.getDirection());
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
