package ai_cup_22.strategy.geometry;

public class Line {
    private final Position start;
    private final Position end;

    public Line(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public double getLength() {
        var dx = end.getX() - start.getX();
        var dy = end.getY() - start.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }

    public double getDistanceTo(Position p) {
        var a = end.getY() - start.getY();
        var b = end.getX() - start.getX();
        var c = end.getX() * start.getY() - end.getY() * start.getX();

        return Math.abs(a * p.getX() - b * p.getY() + c) / getLength();
    }

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
