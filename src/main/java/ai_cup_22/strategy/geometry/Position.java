package ai_cup_22.strategy.geometry;

import ai_cup_22.model.Vec2;
import java.util.HashMap;
import java.util.Map;

public class Position {
    public static final Position ZERO = new Position(0, 0);

    private final static Map<Double, Map<Double, Position>> cachedPositions = new HashMap<>();

    private final double x;
    private final double y;

    public Position(Vec2 vec) {
        this(vec.getX(), vec.getY());
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Position getCached(double x, double y) {
        var row = cachedPositions.computeIfAbsent(y, yy -> new HashMap<>());
        return row.computeIfAbsent(x, xx -> new Position(x, y));
    }

    public double getSquareDistanceTo(Position p) {
        var dx = x - p.x;
        var dy = y - p.y;

        return dx * dx + dy * dy;
    }

    public double getDistanceTo(Position p) {
        return Math.sqrt(getSquareDistanceTo(p));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Position move(Vector vector) {
        return new Position(x + vector.getX(), y + vector.getY());
    }

    public Vec2 toVec2() {
        return new Vec2(x, y);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Position p && (Double.compare(p.x, x) == 0 && Double.compare(p.y, y) == 0);
    }

    @Override
    public int hashCode() {
        return (int)(x * 1000 + y);
    }

    @Override
    public String toString() {
        return "{x: %.3f, y: %.3f}".formatted(x, y);
    }
}
