package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.debug.Colors;

public class Text implements Drawable {
    private String text;
    private ai_cup_22.strategy.geometry.Position position;
    private double size;

    public Text(String text, ai_cup_22.strategy.geometry.Position position) {
        this(text, position, 1);
    }

    public Text(String text, ai_cup_22.strategy.geometry.Position position, double size) {
        this.text = text;
        this.position = position;
        this.size = size;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addPlacedText(position.toVec2(), text, new Vec2(0.5, 0.5), size, Colors.BLACK);
    }
}
