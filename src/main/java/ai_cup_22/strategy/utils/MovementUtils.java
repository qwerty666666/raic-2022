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
        var lookDirection = unit.getDirection();

        for (int i = 0; i < ticks; i++) {
//            if (World.getInstance().getCurrentTick() > 1415)
//            System.out.println(World.getInstance().getCurrentTick() + " " + i + " | " +
//                    unitCircle + " | cur: " +
//                    lookDirection.normalizeToLength(1) + " (" + lookDirection.getAngle() + ") | target: " +
//                    new Vector(unitCircle.getCenter(), unit.getLookPosition()).normalizeToLength(1)  + " (" + new Vector(unitCircle.getCenter(), unit.getLookPosition()).getAngle() + ") | " /*+ " | " +
//                    simulateRotateTickToDirection(lookDirection, new Vector(unitCircle.getCenter(), unit.getLookPosition()),
//                            aim, unit.getAimRotationSpeed()) + " | " +
//                    simulateRotateTickToDirection(lookDirection, new Vector(unitCircle.getCenter(), unit.getLookPosition()),
//                            MathUtils.restrict(0, 1, aim + unit.getAimChangePerTick()), unit.getAimRotationSpeed()) + ""*/
//            );

            // move unit

            var shouldAim = shouldSimulateAim && (unit.isAiming() || remainingCoolDownTicks <= ticksToFullAim);
//DebugData.getInstance().getDefaultLayer().addCircle(bulletPos);
//DebugData.getInstance().getDefaultLayer().addText(Double.toString(aim), bulletPos);
            aim = MathUtils.restrict(0, 1, aim + (shouldAim ? unit.getAimChangePerTick() : -unit.getAimChangePerTick()));

            if (shouldRotateToDirection) {
                lookDirection = simulateRotateTickToDirection(lookDirection, directionVelocity, aim, unit.getAimRotationSpeed());
            } else if (unit.getLookPosition() != null) {
                lookDirection = simulateRotateTickToDirection(lookDirection, new Vector(unitCircle.getCenter(), unit.getLookPosition()),
                        aim, unit.getAimRotationSpeed());
            }

            velocity = getVelocityOnNextTickAfterCollision(
                    unitCircle.getCenter(), lookDirection, velocity,
                    unit.getMaxForwardSpeedPerTick(), unit.getMaxBackwardSpeedPerTick(),
                    aim, unit.getAimSpeedModifier(),
                    directionVelocity,
                    nonWalkThroughObstacles
            );
            var newUnitCircle = unitCircle.move(velocity);

            // move bullet

            var newBulletPos = bulletPos.move(bullet.getVelocity()).getDistanceTo(bulletTrajectory.getStart()) >
                    bulletTrajectory.getLength() ? bulletTrajectory.getEnd() : bulletPos.move(bullet.getVelocity());
            var tickTrajectory = new Line(bulletPos, newBulletPos);
            bulletPos = newBulletPos;

            bulletTrajectory = new Line(bulletPos, bulletTrajectory.getEnd());

            // check that bullet hits unit

            if (isBulletHitCircle(tickTrajectory, unitCircle) || isBulletHitCircle(tickTrajectory, newUnitCircle)) {
                if (isBulletHitInTick(tickTrajectory, unitCircle, newUnitCircle)) {
                    result.isHit = true;
                    break;
                }
            }

            // check that unit run away from hit trajectory

            if (!unitCircle.enlarge(0.35).isIntersect(bulletTrajectory)) {
                result.isHit = false;
                break;
            }

            // go to next iteration cycle

            unitCircle = newUnitCircle;
            remainingCoolDownTicks--;

            result.ticks += 1;
            result.dodgePosition = unitCircle.getCenter();
            result.steps.add(unitCircle.getCenter());
        }

        return result;
    }

    private static Vector simulateRotateTickToDirection(Vector curLookDirection, Vector targetLookDirection, double aim,
            double aimRotationSpeedPerSec) {
        var nonAimRotationSpeed = World.getInstance().getConstants().getRotationSpeed();

        var targetDirectionAngle = MathUtils.normalizeAngle(targetLookDirection.getAngle());
        var lookDirectionAngle = MathUtils.normalizeAngle(curLookDirection.getAngle());

//        var rotateSign = Math.signum(targetDirectionAngle - lookDirectionAngle) *
//                (Math.abs(targetDirectionAngle - lookDirectionAngle) < Math.PI ? 1 : -1);
        var rotationSpeedPerTick = Math.toRadians((nonAimRotationSpeed - (nonAimRotationSpeed - aimRotationSpeedPerSec) * aim) *
                World.getInstance().getTimePerTick());

        var diff = targetDirectionAngle - lookDirectionAngle;
        if (diff > Math.PI) {
            diff = diff - Math.PI * 2;
        } else if (diff < -Math.PI) {
            diff = Math.PI * 2 + diff;
        }

        return curLookDirection.rotate(-Math.signum(diff) * Math.min(rotationSpeedPerTick, Math.abs(diff)));
    }

    private static boolean isBulletHitInTick(Line bulletTickTrajectory, Circle oldPosition, Circle newPosition) {
        var x0 = oldPosition.getCenter().getX();
        var y0 = oldPosition.getCenter().getY();

        var x1 = bulletTickTrajectory.getStart().getX();
        var y1 = bulletTickTrajectory.getStart().getY();

        var v0x = (newPosition.getCenter().getX() - x0);
        var v0y = (newPosition.getCenter().getY() - y0);

        var v1x = (bulletTickTrajectory.getEnd().getX() - x1);
        var v1y = (bulletTickTrajectory.getEnd().getY() - y1);

        var a = Math.pow(v1x - v0x, 2) + Math.pow(v1y - v0y, 2);
        var b = 2 * (v1x - v0x) * (x1 - x0) + 2 * (v1y - v0y) * (y1 - y0);
        var c = (x1 * x1 - 2 * x1 * x0 + x0 * x0) + (y1 * y1 - 2 * y1 * y0 + y0 * y0) - 1;

        return b * b - 4 * a * c >= 0;
    }

    private static boolean isBulletHitCircle(Line bulletTickTrajectory, Circle unitCircle) {
        if (unitCircle.isIntersect(bulletTickTrajectory)) {
            var hitUnitPosition = bulletTickTrajectory.getIntersectionPoints(unitCircle).stream()
                    .min(Comparator.comparingDouble(point -> point.getDistanceTo(bulletTickTrajectory.getStart())))
                    .orElse(null);

            return hitUnitPosition == null ||
                    bulletTickTrajectory.getEnd().getDistanceTo(bulletTickTrajectory.getStart()) >
                            hitUnitPosition.getDistanceTo(bulletTickTrajectory.getStart());
        }

        return false;
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
