package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.geometry.Position;
import java.util.List;

public class PathDrawable implements Drawable {
    private final List<Position> path;

    public PathDrawable(List<Position> path) {
        this.path = path;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        if (path != null) {
            for (int i = 0; i < path.size() - 1; i++) {
                new Line(path.get(i), path.get(i + 1), Colors.BLUE).draw(debugInterface);
            }
        }
    }
}
