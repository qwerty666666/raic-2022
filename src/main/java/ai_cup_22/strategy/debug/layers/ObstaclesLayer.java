package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;

public class ObstaclesLayer extends DrawLayer {
    public void reinit(World world) {
        clear();

        for (var obstacle: world.getObstacles().values()) {
            if (obstacle.isCanShootThrough()) {
                add(new CircleDrawable(obstacle.getCircle(), Colors.YELLOW_TRANSPARENT));
            } else {
                add(new CircleDrawable(obstacle.getCircle(), Colors.GRAY_TRANSPARENT));
            }
        }
    }

    @Override
    public boolean isImmutable() {
        return false;
    }
}
