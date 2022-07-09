package ai_cup_22.strategy.debug.layers;

import ai_cup_22.DebugInterface;
import ai_cup_22.strategy.debug.primitives.Drawable;
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
}
