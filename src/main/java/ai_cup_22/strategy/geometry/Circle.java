package ai_cup_22.strategy.geometry;

import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;

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

    public boolean intersect(Line line) {
        // check that circle is in rect formed by line
        if (!(new Rectangle(line.getEnd(), line.getStart()).increase(radius).contains(center))) {
            return false;
        }
//
//        DebugData.getInstance().getDefaultLayer().add(new ai_cup_22.strategy.debug.primitives.Rectangle(
//                new Rectangle(line.getEnd(), line.getStart()).increase(radius),
//                Colors.LIGHT_BLUE_TRANSPARENT
//        ));
//
//        var dist = line.getDistanceTo(center);

        return line.getDistanceTo(center) <= radius;
    }

    public boolean contains(Position p) {
        return p.getSquareDistanceTo(center) <= radius * radius;
    }

    public Circle enlarge(double added) {
        return new Circle(center, radius + added);
    }

    public Circle enlargeToRadius(double radius) {
        return new Circle(center, radius);
    }

    @Override
    public String toString() {
        return "c: " + getCenter() + ", r: " + radius;
    }
}
