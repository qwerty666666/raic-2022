package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;

public class Rectangle implements Drawable {
    private final ai_cup_22.strategy.geometry.Rectangle rect;
    private final Color color;

    public Rectangle(ai_cup_22.strategy.geometry.Rectangle rect, Color color) {
        this.rect = rect;
        this.color = color;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addRect(rect.getLeftBottom().toVec2(), rect.getSize(), color);
    }
}
