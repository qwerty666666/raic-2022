package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;

public class CircleSegment implements Drawable {
    private final ai_cup_22.strategy.geometry.CircleSegment segment;
    private final Color color;

    public CircleSegment(ai_cup_22.strategy.geometry.CircleSegment segment, Color color) {
        this.segment = segment;
        this.color = color;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addPie(segment.getCenter().toVec2(), segment.getRadius(),
                segment.getMinAngle(), segment.getMaxAngle(), color);
    }
}
