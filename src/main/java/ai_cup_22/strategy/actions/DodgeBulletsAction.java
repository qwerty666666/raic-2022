package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.utils.MovementUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DodgeBulletsAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var velocityToDodge = getVelocityToDodgeNearPossibleBullet(unit);

        if (velocityToDodge != null) {
            new MoveToAction(unit.getPosition().move(velocityToDodge)).apply(unit, order);
        }
    }

    private Vector getVelocityToDodgeNearPossibleBullet(Unit unit) {
        return World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .filter(bullet -> isBulletTreatsUnit(unit, bullet))
                .sorted(Comparator.comparingDouble(b -> b.getPosition().getDistanceTo(unit.getPosition())))
                .map(bullet -> getVelocityToDodgeBullet(unit, bullet))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Vector getVelocityToDodgeBullet(Unit unit, Bullet bullet) {
        var bulletVelocity = bullet.getVelocity();

        var directionsToDodge = new ArrayList<Vector>();
        directionsToDodge.addAll(List.of(
                bulletVelocity.rotate(Math.PI / 2),
                bulletVelocity.rotate(-Math.PI / 2),
                bulletVelocity.rotate(Math.PI / 4),
                bulletVelocity.rotate(-Math.PI / 4),
                bulletVelocity
        ));
        directionsToDodge.addAll(
                // take first direction which are further to the bullet
                // (to not run on next bullets)
                getTrajectoriesToAvoidNonMoveThroughObstacles(unit).stream()
                        .sorted(Comparator.comparingDouble((Vector direction) -> {
                                return direction.getEndPosition().getDistanceTo(unit.getPosition());
                            })
                            .reversed()
                        )
                        .collect(Collectors.toList())
        );

        if (DebugData.isEnabled) {
            directionsToDodge.stream()
                    .map(direction -> direction.normalizeToLength(10))
                    .forEach(d -> DebugData.getInstance().getDefaultLayer().add(new Line(unit.getPosition(), unit.getPosition().move(d), Colors.BLUE_TRANSPARENT)));
            directionsToDodge.stream()
                    .map(direction -> direction.normalizeToLength(10))
                    .filter(direction -> !MovementUtils.isHitBulletIfWalkDirect(unit, direction, bullet))
                    .findFirst()
                    .ifPresent(d -> DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(new Circle(unit.getPosition().move(d), 0.3), Colors.GREEN_TRANSPARENT)));
        }

        return directionsToDodge.stream()
                .map(direction -> direction.normalizeToLength(10))
                .filter(direction -> !MovementUtils.isHitBulletIfWalkDirect(unit, direction, bullet))
                .findFirst()
                .orElse(null);
    }

    private List<Vector> getTrajectoriesToAvoidNonMoveThroughObstacles(Unit unit) {
        return MovementUtils.getNonWalkThroughObstaclesInRange(unit, 10).stream()
                .map(circle -> circle.enlarge(unit.getCircle().getRadius()))
                .flatMap(circle -> circle.getTangentPoints(unit.getPosition()).stream())
                .map(tangentPoint -> new Vector(unit.getPosition(), tangentPoint))
                .collect(Collectors.toList());
    }

    private boolean isBulletTreatsUnit(Unit unit, Bullet bullet) {
        return unit.getCircle().enlarge(0.4).isIntersect(bullet.getTrajectory());
    }
}
