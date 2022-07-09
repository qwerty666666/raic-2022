package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.model.Vec2;

public class Position {
    private final double x;
    private final double y;

    public Position(Vec2 vec) {
        this(vec.getX(), vec.getY());
    }

    public Position(ai_cup_22.strategy.geometry.Position position) {
        this(position.getX(), position.getY());
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vec2 toVec2() {
        return new Vec2(x, y);
    }
}
