package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.geometry.Vector;

public class CircleSegmentDrawable implements Drawable {
    private final ai_cup_22.strategy.geometry.CircleSegment segment;
    private final Color color;

    public CircleSegmentDrawable(ai_cup_22.strategy.geometry.CircleSegment segment, Color color) {
        this.segment = segment;
        this.color = color;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addPie(segment.getCenter().toVec2(), segment.getRadius(),
                segment.getMinAngle(), segment.getMaxAngle(), color);
//        new Line(segment.getCenter(), segment.getCenter().move(new Vector(segment.getRadius(), 0).rotate(-segment.getMinAngle())), Colors.BLUE, 0.05)
//                .draw(debugInterface);
//        new Line(segment.getCenter(), segment.getCenter().move(new Vector(segment.getRadius(), 0).rotate(-segment.getMaxAngle())), Colors.BLUE, 0.05)
//                .draw(debugInterface);
    }
}
