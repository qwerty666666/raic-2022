package ai_cup_22.strategy.geometry;

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

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
