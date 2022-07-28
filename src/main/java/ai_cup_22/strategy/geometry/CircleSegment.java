package ai_cup_22.strategy.geometry;

import java.util.List;

public class CircleSegment {
    private final Circle circle;
    private final double centerAngle;
    // whole angle
    private final double angle;

    public CircleSegment(Circle circle, double centerAngle, double angle) {
        this.circle = circle;
        this.centerAngle = centerAngle;
        this.angle = angle;
    }

    public CircleSegment restrictToAngle(double newAngle) {
        return new CircleSegment(circle, centerAngle, newAngle);
    }

    public List<Line> getBoundaries() {
        return List.of(
                new Line(circle.getCenter(), circle.getCenter().move(new Vector(circle.getRadius(), 0).rotate(getMinAngle()))),
                new Line(circle.getCenter(), circle.getCenter().move(new Vector(circle.getRadius(), 0).rotate(getMaxAngle())))
        );
    }

    public CircleSegment rotateToAngle(double newCenterAngle) {
        return new CircleSegment(circle, newCenterAngle, angle);
    }

    public Position getCenter() {
        return circle.getCenter();
    }

    public double getRadius() {
        return circle.getRadius();
    }

    public double getCenterAngle() {
        return centerAngle;
    }

    public double getMinAngle() {
        return centerAngle - angle / 2;
    }

    public double getMaxAngle() {
        return getMinAngle() + angle;
    }

    public double getAngle() {
        return angle;
    }

    public boolean contains(Position position) {
        if (position.getDistanceTo(getCenter()) > getRadius()) {
            return false;
        }

        return contains(new Vector(getCenter(), position));
    }

    public boolean contains(Vector v) {
        var angle = v.getAngle();

        while (angle > getMinAngle()) {
            angle -= Math.PI * 2;
        }
        while (angle <= getMinAngle()) {
            angle += Math.PI * 2;
        }

        return angle < getMaxAngle();
    }

    public boolean contains(Circle circle) {
        if (this.circle.getCenter().getDistanceTo(circle.getCenter()) + circle.getRadius() >= this.getRadius()) {
            return false;
        }

        if (!contains(circle.getCenter())) {
            return false;
        }

        return circle.getTangentPoints(this.circle.getCenter()).stream()
                .allMatch(this::contains);
    }

    public boolean containsOrIntersects(Circle circle) {
        return contains(circle) ||
                this.getBoundaries().stream().anyMatch(circle::isIntersect);
    }
}
