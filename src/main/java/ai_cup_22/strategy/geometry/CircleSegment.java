package ai_cup_22.strategy.geometry;

public class CircleSegment {
    private final Circle circle;
    private final double centerAngle;
    private final double angle;

    public CircleSegment(Circle circle, double centerAngle, double angle) {
        this.circle = circle;
        this.centerAngle = centerAngle;
        this.angle = angle;
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

        var angleToPosition = new Vector(getCenter(), position).getAngle();
        while (angleToPosition > getMinAngle()) {
            angleToPosition -= Math.PI * 2;
        }
        while (angleToPosition <= getMinAngle()) {
            angleToPosition += Math.PI * 2;
        }

        return angleToPosition < getMaxAngle();
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
}
