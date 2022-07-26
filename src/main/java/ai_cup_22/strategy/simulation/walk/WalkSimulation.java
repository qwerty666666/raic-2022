package ai_cup_22.strategy.simulation.walk;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.utils.MathUtils;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WalkSimulation {
    public static final double MAX_VELOCITY_CHANGE_PER_TICK = 1. / 30;
    public static final double UNIT_RADIUS = 1;

    public static int getTicksToRotate(double angle, double aim, double aimChangePerTick, boolean shouldAim, double aimRotationSpeedPerSec) {
        int ticks = 0;
        var curDirection = new Vector(1, 0);
        var targetDirection = new Vector(1, 0).rotate(angle);

        while (Math.abs(curDirection.getAngle() - angle) > 0.01) {
            aim = getAimOnNextTick(aim, aimChangePerTick, shouldAim);
            curDirection = simulateRotateTickToDirection(curDirection, targetDirection, aim, aimRotationSpeedPerSec);
            ticks++;
        }

        return ticks;
    }

    public static int getTicksToRotateWithAim(Unit unit, Position target, boolean rotateStrictlyToTarget) {
        var targetVector = new Vector(unit.getPosition(), target);
        var curDirection = unit.getDirection();
        int ticks = 0;
        var aim = unit.getAim();
        var aimChangePerTick = unit.getAimChangePerTick();
        var aimRotationSpeedPerSec = unit.getAimRotationSpeed();
        var ticksToCd = unit.getRemainingCoolDownTicks();
        var ticksToAim = unit.getRemainedTicksToAim();

        while (true) {
            if (rotateStrictlyToTarget) {
                if (targetVector.getAngleTo(curDirection) < 0.01) {
                    break;
                }
            } else {
                var fieldOfView = Math.toRadians(World.getInstance().getConstants().getFieldOfView());
                var aimFieldOfView = unit.getWeaponOptional()
                        .map(weapon -> Math.toRadians(weapon.getAimFieldOfView()))
                        .orElse(fieldOfView);
                var viewAngle = fieldOfView - (fieldOfView - aimFieldOfView) * aim;

                if (targetVector.getAngleTo(curDirection) < viewAngle / 2) {
                    break;
                }
            }

            var shouldAim = ticksToCd <= ticksToAim;
            aim = getAimOnNextTick(aim, aimChangePerTick, shouldAim);
            curDirection = simulateRotateTickToDirection(curDirection, targetVector, aim, aimRotationSpeedPerSec);
            ticks++;
            ticksToCd--;

            if (shouldAim) {
                ticksToAim--;
            } else {
                ticksToAim++;
            }
        }

        return ticks;
    }

    public static Position getMaxPositionIfWalkDirect(Unit unit, Vector directionVelocity, int ticks) {
        var obstacles = WalkSimulation.getNonWalkThroughObstaclesInRange(unit , ticks * unit.getMaxForwardSpeedPerTick() + 5);

        var pos = unit.getPosition();
        var velocity = unit.getVelocityPerTick();
        var aim = unit.getAim();
        for (int i = 0; i < ticks; i++) {
            velocity = getVelocityOnNextTickAfterCollision(
                    pos, unit.getDirection(), velocity,
                    unit.getMaxForwardSpeedPerTick(), unit.getMaxBackwardSpeedPerTick(),
                    unit.getAim(), unit.getAimSpeedModifier(),
                    directionVelocity,
                    obstacles
            );
            pos = pos.move(velocity);
            aim = MathUtils.restrict(0, 1, aim + (unit.isAiming() ? unit.getAimChangePerTick() : -unit.getAimChangePerTick()));
        }

        return pos;
    }

    public static int getTicksToRunDistanceWithAim(Unit unit, Position destination) {
        return getTicksToRunDistance(unit, destination, true);
    }

    public static int getTicksToRunDistance(Unit unit, Position destination, boolean shouldAim) {
        var pos = unit.getPosition();
        var velocity = unit.getVelocityPerTick();
        var aim = unit.getAim();
        var dist = unit.getPosition().getDistanceTo(destination);
        int ticks = 0;
        Vector velocityOnPrevTick;

        while (dist > 0.01) {
            aim = getAimOnNextTick(aim, unit.getAimChangePerTick(), shouldAim);
            velocityOnPrevTick = velocity;
            velocity = getVelocityOnNextTick(
                    pos,
                    unit.getDirection(),
                    velocity,
                    unit.getMaxForwardSpeedPerTick(),
                    unit.getMaxBackwardSpeedPerTick(),
                    unit.getAim(), unit.getAimSpeedModifier(),
                    new Vector(pos, destination)
            );
            pos = pos.move(velocity);
            dist -= velocity.getLength();
            ticks++;

            if (aim == 0 && velocity.equals(velocityOnPrevTick)) {
                ticks += dist / velocity.getLength();
            }
        }

        return ticks;
    }

    public static Vector getVelocityOnNextTickAfterCollision(
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

    public static Vector simulateRotateTickToDirection(Vector curLookDirection, Vector targetLookDirection, double aim,
            double aimRotationSpeedPerSec) {
        var nonAimRotationSpeed = World.getInstance().getConstants().getRotationSpeed();
        var rotationSpeedPerTick = Math.toRadians((nonAimRotationSpeed - (nonAimRotationSpeed - aimRotationSpeedPerSec) * aim) *
                World.getInstance().getTimePerTick());
        var diff = curLookDirection.getDiffToVector(targetLookDirection);

        return curLookDirection.rotate(Math.signum(diff) * Math.min(rotationSpeedPerTick, Math.abs(diff)));
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
                .filter(u -> !u.isPhantom() && u.isSpawned())
                .filter(u -> u.getDistanceTo(unit) < maxDist)
                .map(Unit::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(units);

        return obstacles;
    }

    public static double getAimOnNextTick(double aim, double aimChangePerTick, boolean shouldAim) {
        return MathUtils.restrict(0, 1, aim + (shouldAim ? aimChangePerTick : -aimChangePerTick));
    }
}
