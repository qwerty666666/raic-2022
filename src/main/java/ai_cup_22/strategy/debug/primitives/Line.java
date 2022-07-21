package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;

public class Line implements Drawable {
    private final ai_cup_22.strategy.geometry.Line line;
    private final Color color;
    private final double width;

    public Line(ai_cup_22.strategy.geometry.Position p1, ai_cup_22.strategy.geometry.Position p2, Color color, double width) {
        this(new ai_cup_22.strategy.geometry.Line(p1, p2), color, width);
    }

    public Line(ai_cup_22.strategy.geometry.Position p1, ai_cup_22.strategy.geometry.Position p2, Color color) {
        this(new ai_cup_22.strategy.geometry.Line(p1, p2), color, 0.1);
    }

    public Line(ai_cup_22.strategy.geometry.Line line, Color color) {
        this(line, color, 0.1);
    }

    public Line(ai_cup_22.strategy.geometry.Line line, Color color, double width) {
        this.line = line;
        this.color = color;
        this.width = width;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        debugInterface.addSegment(line.getStart().toVec2(), line.getEnd().toVec2(), width, color);
    }
}
