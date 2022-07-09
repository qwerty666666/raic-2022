package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.geometry.Circle;

public class CircleDrawable implements Drawable {
    private final Position center;
    private final double radius;
    private final Color color;

    public CircleDrawable(Circle circle, Color color) {
        this.center = new Position(circle.getCenter());
        this.radius = circle.getRadius();
        this.color = color;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addCircle(center.toVec2(), radius, color);
    }
}
