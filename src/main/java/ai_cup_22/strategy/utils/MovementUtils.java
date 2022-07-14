package ai_cup_22.strategy.utils;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import java.util.Comparator;
import java.util.List;

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

    public static Vector restrictVelocitySpeed(Position position, Vector direction, double maxForwardSpeed,
            double maxBackwardSpeed, double aim, double aimSpeedModifier, Vector movementVector) {
        var maxSpeed = getMaxVelocityInDirection(position, direction, maxForwardSpeed, maxBackwardSpeed, aim,
                aimSpeedModifier, movementVector);

        return maxSpeed < movementVector.getLength() ? movementVector.normalizeToLength(maxSpeed) : movementVector;
    }

    public static Position getPositionAfterCollision(Position curPosition, Vector velocityPerTick, List<Circle> obstacles) {
        var nextPosition = curPosition.move(velocityPerTick);
        var trajectory = new Line(curPosition, nextPosition);

        // find the nearest obstacle which I collide with
        var collidingCircle = obstacles.stream()
                .map(circle -> circle.enlarge(UNIT_RADIUS))
                .filter(circle -> circle.isIntersect(trajectory))
                .findFirst();
        if (collidingCircle.isEmpty()) {
            return nextPosition;
        }

        // find the point on the circle I hit in
        var intersectionPosition = trajectory.getIntersectionPointsAsRay(collidingCircle.get()).stream()
                .min(Comparator.comparingDouble(p -> p.getDistanceTo(curPosition)))
                .orElse(nextPosition);

        // find projection on perpendicular
        return new Line(collidingCircle.get().getCenter(), intersectionPosition)
                .getPerpendicularThroughtPoint(intersectionPosition)
                .getProjection(nextPosition);
    }
}
