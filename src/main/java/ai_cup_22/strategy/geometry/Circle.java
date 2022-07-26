package ai_cup_22.strategy.geometry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Position> getTangentPoints(Position position) {
        if (position.getDistanceTo(center) <= radius) {
            return Collections.emptyList();
        }

        // cas a = r / dist
        var angle = Math.acos(radius / center.getDistanceTo(position));
        var vec = new Vector(center, position).normalizeToLength(radius);

        return List.of(
                vec.rotate(angle).getEndPosition().move(new Vector(center)),
                vec.rotate(-angle).getEndPosition().move(new Vector(center))
        );
    }

    public List<Line> getTangentLines(Position position) {
        return getTangentPoints(position).stream()
                .map(p -> new Line(position, p))
                .collect(Collectors.toList());
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
