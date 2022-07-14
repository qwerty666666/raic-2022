package ai_cup_22.strategy.geometry;

public class Circle {
    private final Position center;
    private final double radius;

    public Circle(Position center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Position getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isIntersect(Line line) {
        return line.getStart().getDistanceTo(center) <= radius ||
                line.getEnd().getDistanceTo(center) <= radius ||
                (line.getDistanceTo(center) <= radius && line.contains(line.getProjection(center)));
    }

    public boolean contains(Position p) {
        return p.getSquareDistanceTo(center) <= radius * radius;
    }

    public Circle enlarge(double added) {
        return new Circle(center, radius + added);
    }

    public Circle moveToPosition(Position toPosition) {
        return new Circle(toPosition, radius);
    }

    public Circle move(Vector vector) {
        return new Circle(center.move(vector), radius);
    }

    public Circle enlargeToRadius(double radius) {
        return new Circle(center, radius);
    }

    @Override
    public String toString() {
        return "c: " + getCenter() + ", r: " + radius;
    }
}
