package ai_cup_22.strategy.debug.layers;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.CircleSegmentDrawable;
import ai_cup_22.strategy.debug.primitives.Drawable;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.List;

public class DrawLayer {
    private List<Drawable> items = new ArrayList<>();

    public void clear() {
        items.clear();
    }

    public void add(Drawable drawable) {
        items.add(drawable);
    }

    public void show(DebugInterface debugInterface) {
        this.items.forEach(i -> i.draw(debugInterface));
    }

    public boolean isImmutable() {
        return true;
    }

    public void addCircle(Position position) {
        addCircle(position, 0.1);
    }

    public void addCircle(Position position, double radius) {
        addCircle(position, radius, Colors.BLUE_TRANSPARENT);
    }

    public void addSegment(CircleSegment segment) {
        add(new CircleSegmentDrawable(segment, Colors.BLUE_TRANSPARENT));
    }

    public void addCircle(Position position, double radius, Color color) {
        add(new CircleDrawable(new Circle(position, radius), color));
    }

    public void addRing(Position position, double radius, Color color) {
        add(new CircleDrawable(new Circle(position, radius), color, false));
    }

    public void addLine(Position p1, Position p2, Color color) {
        add(new Line(p1, p2, color));
    }

    public void addLine(Position p1, Position p2) {
        addLine(p1, p2, Colors.BLUE_TRANSPARENT);
    }

    public void addText(String text, Position pos) {
        addText(text, pos, 0.1);
    }

    public void addText(String text, Position pos, double size) {
        add(new Text(text, pos, size));
    }
}
