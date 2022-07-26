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

    public Vector(Position p) {
        this(p.getX(), p.getY());
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

    public Vector restrictLength(double len) {
        if (getLength() > len) {
            return normalizeToLength(len);
        }
        return this;
    }

    public Vector normalizeToLength(double len) {
        double mul = len / getLength();

       return increase(mul);
    }

    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }

    public Vector subtract(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    public Vec2 toVec2() {
        return new Vec2(x, y);
    }

    public Line toLine() {
        return new Line(Position.ZERO, getEndPosition());
    }

    public Vector rotate(double angle) {
        return new Vector(
                x * Math.cos(angle) - y * Math.sin(angle),
                y * Math.cos(angle) + x * Math.sin(angle)
        );
    }

    /**
     * [0, 2 * Pi)
     */
    public double getAngle() {
        var angle = Math.atan(y / x) + (x < 0 ? Math.PI : 0);
        if (angle < 0) {
            angle = Math.PI * 2 + angle;
        }
        return angle;
    }

    /**
     * [0, Pi]
     */
    public double getAngleTo(Vector v) {
        return Math.acos(Math.max(-1., Math.min(1., (x * v.x + y * v.y) / (getLength() * v.getLength()))));
    }

    /**
     * How to rotate "this" vector to align with "v" vector
     *
     * @return [-PI, PI]
     */
    public double getDiffToVector(Vector v) {
        var diff = v.getAngle() - getAngle();
        if (diff > Math.PI) {
            diff = diff - Math.PI * 2;
        } else if (diff < -Math.PI) {
            diff = Math.PI * 2 + diff;
        }
        return diff;
    }

    @Override
    public String toString() {
        return String.format("{x: %f, y: %f} (%f)", x, y, getLength());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector && Double.compare(((Vector)obj).getX(), x) == 0 &&
                Double.compare(((Vector)obj).getY(), y) == 0;
    }
}
