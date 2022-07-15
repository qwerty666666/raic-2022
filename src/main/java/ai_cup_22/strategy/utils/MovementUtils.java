package ai_cup_22.strategy.utils;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovementUtils {
    public static final double MAX_VELOCITY_CHANGE_PER_TICK = 1. / 30;
    public static final double UNIT_RADIUS = 1;

    public static double getMaxVelocityInDirection(Position position, Vector direction, double maxForwardSpeed,
            double maxBackwardSpeed, double aim, double aimSpeedModifier, Vector movementVector) {
        maxForwardSpeed = maxForwardSpeed * (1 - (1 - aimSpeedModifier) * aim);
        maxBackwardSpeed = maxBackwardSpeed * (1 - (1 - aimSpeedModifier) * aim);

        var speedCircleCenter = position.move(direction.normalizeToLength((maxForwardSpeed - maxBackwardSpeed) / 2));
        var speedCircleRadius = maxForwardSpeed - (maxForwardSpeed - maxBackwardSpeed) / 2;

        var vectorToSpeedCircleCenter = new Vector(position, speedCircleCenter);
        var angle = vectorToSpeedCircleCenter.getAngleTo(movementVector);
        var a = speedCircleRadius;
        var b = vectorToSpeedCircleCenter.getLength();

        // a^2 = b^2 + x^2 - 2bx cos(angle)
        // x^2 - x * (2b * cos(angle)) + (b^2 - a^2)
        var B = -2 * b * Math.cos(angle);
        var C = b * b - a * a;
        var D = B * B - 4 * C;

        return (-B + Math.sqrt(D)) / 2;
//        DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(speedCircle, Colors.YELLOW_TRANSPARENT));
//        DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(speedCircle.enlargeToRadius(0.2), Colors.RED_TRANSPARENT));
//        DebugData.getInstance().getDefaultLayer().add(new Text(Double.toString(x), position.move(new Vector(2, 2))));
//        DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(new Circle(position.move(movementVector.normalizeToLength(x)), 0.2), Colors.BLUE_TRANSPARENT));
    }



    public static Vector getVelocityOnNextTick(Position position, Vector direction, double maxForwardSpeed,
            double maxBackwardSpeed, double aim, double aimSpeedModifier, Vector targetVelocity, Vector currentVelocity) {
        var diffVector = restrictVelocitySpeed(position, direction, maxForwardSpeed, maxBackwardSpeed, aim, aimSpeedModifier, targetVelocity)
                .subtract(currentVelocity);

        var nextVelocity = currentVelocity.add(diffVector.restrictLength(MAX_VELOCITY_CHANGE_PER_TICK));

        return restrictVelocitySpeed(position, direction, maxForwardSpeed, maxBackwardSpeed, aim, aimSpeedModifier, nextVelocity);
    }

    private static Vector restrictVelocitySpeed(Position position, Vector direction, double maxForwardSpeed,
            double maxBackwardSpeed, double aim, double aimSpeedModifier, Vector movementVector) {
        var maxSpeed = getMaxVelocityInDirection(position, direction, maxForwardSpeed, maxBackwardSpeed, aim,
                aimSpeedModifier, movementVector);

        return maxSpeed < movementVector.getLength() ? movementVector.normalizeToLength(maxSpeed) : movementVector;
    }

    private static Vector getVelocityAfterCollision(Position curPosition, Vector velocity, List<Circle> obstacles) {
        var nextPosition = curPosition.move(velocity);
        var trajectory = new Line(curPosition, nextPosition);

        // find the nearest obstacle which I collide with
        var collidingCircle = obstacles.stream()
                .map(circle -> circle.enlarge(UNIT_RADIUS))
                .filter(circle -> circle.isIntersect(trajectory))
                .findFirst();
        if (collidingCircle.isEmpty()) {
            return velocity;
        }

        // find the point on the circle I hit in
        var intersectionPosition = trajectory.getIntersectionPointsAsRay(collidingCircle.get()).stream()
                .min(Comparator.comparingDouble(p -> p.getDistanceTo(curPosition)))
                .orElse(nextPosition);

        // find projection on perpendicular
        nextPosition = new Line(collidingCircle.get().getCenter(), intersectionPosition)
                .getPerpendicularThroughtPoint(intersectionPosition)
                .getProjection(nextPosition);

        return new Vector(curPosition, nextPosition);
    }

    public static Position getMaxPositionIfWalkDirect(Unit unit, Vector directionVelocity, int ticks) {
        var obstacles = getNonWalkThroughObstaclesInRange(unit , ticks * unit.getMaxForwardSpeedPerTick() + 5);

        var pos = unit.getPosition();
        var velocity = unit.getVelocityPerTick();
        for (int i = 0; i < ticks; i++) {
            // TODO add check aim
            velocity = getVelocityOnNextTickAfterCollision(pos, unit.getDirection(), unit.getMaxForwardSpeedPerTick(),
                    unit.getMaxBackwardSpeedPreTick(), 0, 0, directionVelocity, velocity, obstacles);
            pos = pos.move(velocity);
        }

        return pos;
    }

    public static boolean isHitBulletIfWalkDirect(Unit unit, Vector directionVelocity, Bullet bullet) {
        var ticks = bullet.getRemainingLifetimeTicks();
        var nonWalkThroughObstacles = getNonWalkThroughObstaclesInRange(unit , ticks * unit.getMaxForwardSpeedPerTick() + 5);
        var bulletTrajectory = bullet.getTrajectory();

        var unitCircle = unit.getCircle();
        var velocity = unit.getVelocityPerTick();
        var bulletPos = bullet.getPosition();
        for (int i = 0; i < ticks; i++) {
            // TODO add check aim
            velocity = getVelocityOnNextTickAfterCollision(unitCircle.getCenter(), unit.getDirection(), unit.getMaxForwardSpeedPerTick(),
                    unit.getMaxBackwardSpeedPreTick(), 0, 0, directionVelocity, velocity, nonWalkThroughObstacles);
            unitCircle = unitCircle.move(velocity);

            var newBulletPos = bulletPos.move(bullet.getVelocity());
            var tickTrajectory = new Line(bulletPos, newBulletPos);

            if (unitCircle.isIntersect(tickTrajectory)) {
                var hitUnitPosition = tickTrajectory.getIntersectionPoints(unitCircle).stream()
                        .min(Comparator.comparingDouble(point -> point.getDistanceTo(tickTrajectory.getStart())))
                        .orElse(null);

                return hitUnitPosition == null ||
                        bulletTrajectory.getEnd().getDistanceTo(bulletPos) > hitUnitPosition.getDistanceTo(bulletPos);
            }

            bulletPos = newBulletPos;
        }

        return false;
    }

    public static DodgeResult tryDodgeByWalkDirect(Unit unit, Vector directionVelocity, Bullet bullet) {
        var result = new DodgeResult();

        var ticks = bullet.getRemainingLifetimeTicks();
        var nonWalkThroughObstacles = getNonWalkThroughObstaclesInRange(unit , ticks * unit.getMaxForwardSpeedPerTick() + 5);
        var bulletTrajectory = bullet.getTrajectory();

        var unitCircle = unit.getCircle();
        var velocity = unit.getVelocityPerTick();
        var bulletPos = bullet.getPosition();

        for (int i = 0; i < ticks; i++) {
            // TODO add check aim
            velocity = getVelocityOnNextTickAfterCollision(unitCircle.getCenter(), unit.getDirection(), unit.getMaxForwardSpeedPerTick(),
                    unit.getMaxBackwardSpeedPreTick(), 0, 0, directionVelocity, velocity, nonWalkThroughObstacles);
            unitCircle = unitCircle.move(velocity);

            result.ticks += 1;
            result.dodgePosition = unitCircle.getCenter();
            result.steps.add(unitCircle.getCenter());

            var newBulletPos = bulletPos.move(bullet.getVelocity());
            var tickTrajectory = new Line(bulletPos, newBulletPos);

            if (unitCircle.isIntersect(tickTrajectory)) {
                var hitUnitPosition = tickTrajectory.getIntersectionPoints(unitCircle).stream()
                        .min(Comparator.comparingDouble(point -> point.getDistanceTo(tickTrajectory.getStart())))
                        .orElse(null);

                var isHitUnit = hitUnitPosition == null ||
                        bulletTrajectory.getEnd().getDistanceTo(bulletPos) > hitUnitPosition.getDistanceTo(bulletPos);

                if (isHitUnit) {
                    result.isHit = true;
                    break;
                }
            }

            bulletPos = newBulletPos;

            if (!unitCircle.enlarge(0.35).isIntersect(bulletTrajectory)) {
                result.isHit = false;
                break;
            }
        }

        return result;
    }

    public static class DodgeResult {
        public Position dodgePosition;
        public boolean isHit;
        public int ticks;
        public List<Position> steps = new ArrayList<>();
    }

    public static List<Circle> getNonWalkThroughObstaclesInRange(Unit unit, double maxDist) {
        var obstacles = World.getInstance().getObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getDistanceTo(unit.getPosition()) < maxDist)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        var units = Stream.concat(
                        World.getInstance().getMyUnits().values().stream(),
                        World.getInstance().getEnemyUnits().values().stream()
                )
                .filter(u -> u.getId() != unit.getId())
                .map(Unit::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(units);

        return obstacles;
    }

    public static List<Circle> getNonShootThroughObstaclesInRange(Unit unit, double maxDist) {
        return World.getInstance().getNonShootThroughObstacles().stream()
                .filter(obstacle -> obstacle.getCenter().getDistanceTo(unit.getPosition()) < maxDist)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());
    }

    private static Vector getVelocityOnNextTickAfterCollision(Position position, Vector direction, double maxForwardSpeed,
            double maxBackwardSpeed, double aim, double aimSpeedModifier, Vector targetVelocity, Vector currentVelocity,
            List<Circle> obstacles) {
        var velocity = getVelocityOnNextTick(position, direction, maxForwardSpeed, maxBackwardSpeed,
                aim, aimSpeedModifier, targetVelocity, currentVelocity);

        return getVelocityAfterCollision(position, velocity, obstacles);
    }
}
