package ai_cup_22.strategy.geometry;

import ai_cup_22.model.Vec2;

public class Rectangle {
    private final Position leftBottom;
    private final Position rightTop;

    public Rectangle(Position p1, Position p2) {
        var minX = Math.min(p1.getX(), p2.getX());
        var minY = Math.min(p1.getY(), p2.getY());
        var maxX = Math.max(p1.getX(), p2.getX());
        var maxY = Math.max(p1.getY(), p2.getY());

        leftBottom = new Position(minX, minY);
        rightTop = new Position(maxX, maxY);
    }

    public Rectangle increase(double size) {
        return new Rectangle(leftBottom.move(new Vector(-size, -size)), rightTop.move(new Vector(size, size)));
    }

    public boolean contains(Position p) {
        return p.getX() >= leftBottom.getX() && p.getX() <= rightTop.getX() &&
                p.getY() >= leftBottom.getY() && p.getY() <= rightTop.getY();
    }

    public Position getLeftBottom() {
        return leftBottom;
    }

    public double getWidth() {
        return rightTop.getX() - leftBottom.getX();
    }

    public double getHeight() {
        return rightTop.getY() - leftBottom.getY();
    }

    public Vec2 getSize() {
        return new Vec2(getWidth(), getHeight());
    }

    public Position getRightTop() {
        return rightTop;
    }
}
