package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.geometry.Vector;

public class Text implements Drawable {
    private String text;
    private ai_cup_22.strategy.geometry.Position position;
    private double size;
    private Vector align;

    public Text(String text, ai_cup_22.strategy.geometry.Position position) {
        this(text, position, 1);
    }

    public Text(String text, ai_cup_22.strategy.geometry.Position position, double size) {
        this(text, position, size, new Vector(0.5, 0.5));
    }

    public Text(String text, ai_cup_22.strategy.geometry.Position position, double size, Vector align) {
        this.text = text;
        this.position = position;
        this.size = size;
        this.align = align;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addPlacedText(position.toVec2(), text, align.toVec2(), size, Colors.BLACK);
    }
}
