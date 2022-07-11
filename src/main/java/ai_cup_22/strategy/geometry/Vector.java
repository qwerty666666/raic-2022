package ai_cup_22.strategy.geometry;

import ai_cup_22.model.Vec2;

public class Vector {
    private final double x;
    private final double y;

    public Vector(Vec2 v) {
        this(v.getX(), v.getY());
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Position p1, Position p2) {
        this(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Position getEndPosition() {
        return new Position(x, y);
    }

    public Vector reverse() {
        return increase(-1);
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector increase(double mul) {
        return new Vector(x * mul, y * mul);
    }

    public Vector normalizeToLength(double len) {
        double mul = len / getLength();

       return increase(mul);
    }

    public Vec2 toVec2() {
        return new Vec2(x, y);
    }

    public Vector rotate(double angle) {
        return new Vector(
                x * Math.cos(angle) + y * Math.sin(angle),
                y * Math.cos(angle) - x * Math.sin(angle)
        );
    }

    public double getAngle() {
        return Math.atan(y / x) + (x < 0 ? Math.PI : 0);
    }

    @Override
    public String toString() {
        return "{x: %f, y: %f}".formatted(x, y);
    }
}
