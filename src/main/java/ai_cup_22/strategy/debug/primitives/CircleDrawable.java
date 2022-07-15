package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.geometry.Circle;

public class CircleDrawable implements Drawable {
    private final PositionDrawable center;
    private final double radius;
    private final Color color;
    private final boolean fill;

    public CircleDrawable(Circle circle, Color color) {
        this(circle, color, true);
    }

    public CircleDrawable(Circle circle, Color color, boolean fill) {
        this.center = new PositionDrawable(circle.getCenter());
        this.radius = circle.getRadius();
        this.color = color;
        this.fill = fill;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        if (fill) {
            debugInterface.addCircle(center.toVec2(), radius, color);
        } else {
            debugInterface.addRing(center.toVec2(), radius, 0.1, color);
        }
    }
}
