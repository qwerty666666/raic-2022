package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.primitives.Text;

public class PositionsLayer extends DrawLayer {
    public void reinit(World world) {
        clear();

        for (var unit: world.getMyUnits().values()) {
            add(new Text(unit.getId() + ": " + unit.getCircle().getCenter().toString(), unit.getCircle().getCenter()));
        }
        for (var unit: world.getEnemyUnits().values()) {
            add(new Text(unit.getId() + ": " + unit.getCircle().getCenter().toString(), unit.getCircle().getCenter()));
        }

        for (var obstacle: world.getObstacles().values()) {
            add(new Text(obstacle.getId() + ": " + obstacle.getCircle().getCenter().toString(), obstacle.getCircle().getCenter()));
        }
    }
}
