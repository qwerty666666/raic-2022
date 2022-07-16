package ai_cup_22.strategy.utils;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.DebugData;
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

    private static double getMaxVelocityInDirection(
            Position position, Vector lookDirection,
            double maxForwardSpeed, double maxBackwardSpeed,
            double aim, double aimSpeedModifier,
            Vector targetDirection) {
        maxForwardSpeed = maxForwardSpeed * (1 - (1 - aimSpeedModifier) * aim);
        maxBackwardSpeed = maxBackwardSpeed * (1 - (1 - aimSpeedModifier) * aim);

        var speedCircleCenter = position.move(lookDirection.normalizeToLength((maxForwardSpeed - maxBackwardSpeed) / 2));
        var speedCircleRadius = maxForwardSpeed - (maxForwardSpeed - maxBackwardSpeed) / 2;

        var vectorToSpeedCircleCenter = new Vector(position, speedCircleCenter);
        var angle = vectorToSpeedCircleCenter.getAngleTo(targetDirection);
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

    private static Vector getVelocityOnNextTick(
            Position position, Vector lookDirection, Vector currentVelocity,
            double maxForwardSpeed, double maxBackwardSpeed,
            double aim, double aimSpeedModifier,
            Vector targetVelocity) {
        var diffVector = restrictVelocitySpeed(position, lookDirection, maxForwardSpeed, maxBackwardSpeed, aim, aimSpeedModifier, targetVelocity)
                .subtract(currentVelocity);

        var nextVelocity = currentVelocity.add(diffVector.restrictLength(MAX_VELOCITY_CHANGE_PER_TICK));

        return restrictVelocitySpeed(position, lookDirection, maxForwardSpeed, maxBackwardSpeed, aim, aimSpeedModifier, nextVelocity);
    }

    private static Vector restrictVelocitySpeed(
            Position position, Vector lookDirection,
            double maxForwardSpeed, double maxBackwardSpeed,
            double aim, double aimSpeedModifier,
            Vector targetVelocity) {
        var maxSpeed = getMaxVelocityInDirection(position, lookDirection, maxForwardSpeed, maxBackwardSpeed, aim,
                aimSpeedModifier, targetVelocity);

        return maxSpeed < targetVelocity.getLength() ? targetVelocity.normalizeToLength(maxSpeed) : targetVelocity;
    }

    private static Vector getVelocityAfterCollision(Position curPosition, Vector curVelocity, List<Circle> obstacles) {
        var nextPosition = curPosition.move(curVelocity);
        var trajectory = new Line(curPosition, nextPosition);

        // find the nearest obstacle which I collide with
        var collidingCircle = obstacles.stream()
                .map(circle -> circle.enlarge(UNIT_RADIUS))
                .filter(circle -> circle.isIntersect(trajectory))
                .findFirst();
        if (collidingCircle.isEmpty()) {
            return curVelocity;
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
        var aim = unit.getAim();
        for (int i = 0; i < ticks; i++) {
            velocity = getVelocityOnNextTickAfterCollision(
                    pos, unit.getDirection(), velocity,
                    unit.getMaxForwardSpeedPerTick(), unit.getMaxBackwardSpeedPreTick(),
                    unit.getAim(), unit.getAimSpeedModifier(),
                    directionVelocity,
                    obstacles
            );
            pos = pos.move(velocity);
            aim = MathUtils.restrict(0, 1, aim + (unit.isAiming() ? unit.getAimChangePerTick() : -unit.getAimChangePerTick()));
        }

        return pos;
    }

    public static DodgeResult tryDodgeByWalkDirect(Unit unit, Vector directionVelocity, Bullet bullet,
            boolean shouldSimulateAim, boolean shouldRotateToDirection) {
        var result = new DodgeResult();
        result.dodgePosition = unit.getPosition();

        var ticks = bullet.getRemainingLifetimeTicks();
        var nonWalkThroughObstacles = getNonWalkThroughObstaclesInRange(unit , ticks * unit.getMaxForwardSpeedPerTick() + 5);
        var bulletTrajectory = bullet.getTrajectory();
//DebugData.getInstance().getDefaultLayer().addText(Double.toString(unit.getAim()), unit.getPosition());
        var unitCircle = unit.getCircle();
        var aim = unit.getAim();
        var velocity = unit.getVelocityPerTick();
        var bulletPos = bullet.getPosition();
        var remainingCoolDownTicks = unit.getRemainingCoolDownTicks();
        var ticksToFullAim = unit.getTicksToFullAim();
        for (int i = 0; i < ticks; i++) {

            // move unit

            velocity = getVelocityOnNextTickAfterCollision(
                    unitCircle.getCenter(), unit.getDirection(), velocity,
                    unit.getMaxForwardSpeedPerTick(), unit.getMaxBackwardSpeedPreTick(),
                    aim, unit.getAimSpeedModifier(),
                    directionVelocity,
                    nonWalkThroughObstacles
            );
            unitCircle = unitCircle.move(velocity);
            var shouldAim = shouldSimulateAim && (unit.isAiming() || remainingCoolDownTicks <= ticksToFullAim);
//DebugData.getInstance().getDefaultLayer().addCircle(bulletPos);
//DebugData.getInstance().getDefaultLayer().addText(Double.toString(aim), bulletPos);
            aim = MathUtils.restrict(0, 1, aim + (shouldAim ? unit.getAimChangePerTick() : -unit.getAimChangePerTick()));
            remainingCoolDownTicks--;

            result.ticks += 1;
            result.dodgePosition = unitCircle.getCenter();
            result.steps.add(unitCircle.getCenter());

            // move bullet

            var newBulletPos = bulletPos.move(bullet.getVelocity());
            var tickTrajectory = new Line(bulletPos, newBulletPos);
            bulletPos = newBulletPos;

            // check that bullet hits unit

            if (unitCircle.isIntersect(tickTrajectory)) {
                var hitUnitPosition = tickTrajectory.getIntersectionPoints(unitCircle).stream()
                        .min(Comparator.comparingDouble(point -> point.getDistanceTo(tickTrajectory.getStart())))
                        .orElse(null);

                var isHitUnit = hitUnitPosition == null ||
                        bulletTrajectory.getEnd().getDistanceTo(bulletTrajectory.getStart()) > hitUnitPosition.getDistanceTo(bulletTrajectory.getStart());

                if (isHitUnit) {
                    result.isHit = true;
                    break;
                }
            }

            // check that unit run away from hit trajectory

            if (!unitCircle.enlarge(0.35).isIntersect(bulletTrajectory)) {
                result.isHit = false;
                break;
            }
        }

        return result;
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
                .filter(u -> u.getDistanceTo(unit) < maxDist)
                .map(Unit::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(units);

        return obstacles;
    }

    private static Vector getVelocityOnNextTickAfterCollision(
            Position position, Vector lookDirection, Vector currentVelocity,
            double maxForwardSpeed, double maxBackwardSpeed,
            double aim, double aimSpeedModifier,
            Vector targetVelocity,
            List<Circle> obstacles) {
        var velocity = getVelocityOnNextTick(
                position, lookDirection, currentVelocity,
                maxForwardSpeed, maxBackwardSpeed,
                aim, aimSpeedModifier,
                targetVelocity
        );

        return getVelocityAfterCollision(position, velocity, obstacles);
    }




    public static class DodgeResult {
        private Position dodgePosition;
        private boolean isHit;
        private int ticks;
        private List<Position> steps = new ArrayList<>();

        public boolean isSuccess() {
            return !isHit;
        }

        public Position getDodgePosition() {
            return dodgePosition;
        }

        public int getTicks() {
            return ticks;
        }

        public List<Position> getSteps() {
            return steps;
        }
    }

}
