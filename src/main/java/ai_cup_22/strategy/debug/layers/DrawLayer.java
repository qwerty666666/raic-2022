package ai_cup_22.strategy.debug.layers;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Drawable;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
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

    public void addCircle(Position position, double radius, Color color) {
        add(new CircleDrawable(new Circle(position, radius), color));
    }

    public void addText(String text, Position pos) {
        add(new Text(text, pos, 0.1));
    }
}
