package ai_cup_22.strategy.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Line {
    private final Position start;
    private final Position end;
    private final double a;
    private final double b;
    private final double c;

    public Line(Position start, Position end) {
        this.start = start;
        this.end = end;

        a = end.getY() - start.getY();
        b = start.getX() - end.getX();
        c = end.getX() * start.getY() - end.getY() * start.getX();
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public double getLength() {
        return Math.sqrt(getSquareDistance());
    }

    public double getSquareDistance() {
        var dx = end.getX() - start.getX();
        var dy = end.getY() - start.getY();

        return dx * dx + dy * dy;
    }

    public double getDistanceTo(Position p) {
        return Math.abs(a * p.getX() + b * p.getY() + c) / getLength();
    }

    public Position getProjection(Position p) {
        var x = (b * (b * p.getX() - a * p.getY()) - a * c) / getSquareDistance();
        var y = (a * (-b * p.getX() + a * p.getY()) - b * c) / getSquareDistance();

        return new Position(x, y);
    }

    public boolean contains(Position p) {
        return Math.abs(a * p.getX() + b * p.getY() + c) < 0.001 &&
                p.getX() >= Math.min(start.getX(), end.getX()) && p.getX() <= Math.max(start.getX(), end.getX()) &&
                p.getY() >= Math.min(start.getY(), end.getY()) && p.getY() <= Math.max(start.getY(), end.getY());
    }

    public Vector toVector() {
        return new Vector(start, end);
    }

    public Line getPerpendicular() {
        return toVector().rotate(Math.PI / 2).toLine();
    }

    public Line getPerpendicularThroughtPoint(Position position) {
        return getPerpendicular().move(new Vector(position));
    }

    public Line move(Vector v) {
        return new Line(start.move(v), end.move(v));
    }

    public Position getMiddlePoint() {
        return new Position((start.getX() + end.getX()) / 2, (start.getY() + end.getY()) / 2);
    }

    /**
     * Line used as ray
     */
    public List<Position> getIntersectionPointsAsRay(Circle circle) {
        var intersectionPositions = new ArrayList<Position>();

        // rewrite c to align circle to (0, 0)
        var c = this.c + (a * circle.getCenter().getX() + b * circle.getCenter().getY());
        var r = circle.getRadius();
        var eps = 1e-5;
        var s = a * a + b * b;

        double x0 = -a * c / s;
        double y0 = -b * c / s;

        var originOffset = new Vector(circle.getCenter());

        if (c * c > r * r * s + eps) {
            // no points
        } else if (Math.abs(c * c - r * r * s) < eps) {
            intersectionPositions.add(new Position(x0, y0).move(originOffset));
        } else {
            double d = r * r - c * c / s;
            double mult = Math.sqrt(d / s);

            intersectionPositions.add(new Position(x0 + b * mult, y0 - a * mult).move(originOffset));
            intersectionPositions.add(new Position(x0 - b * mult, y0 + a * mult).move(originOffset));
        }

        return intersectionPositions;
    }

    public List<Position> getIntersectionPoints(Circle circle) {
        return getIntersectionPointsAsRay(circle).stream()
                .filter(this::contains)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
