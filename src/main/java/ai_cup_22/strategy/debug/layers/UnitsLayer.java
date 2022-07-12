package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.PotentialFieldDrawable;

public class UnitsLayer extends DrawLayer {
    public void reinit(World world) {
        clear();

//        for (var unit: world.getMyUnits().values()) {
//            add(new CircleDrawable(unit.getCircle(), Colors.GREEN_TRANSPARENT));
//        }
//
//        for (var unit: world.getEnemyUnits().values()) {
//            add(new CircleDrawable(unit.getCircle(), Colors.RED_TRANSPARENT));
//        }

        world.getMyUnits().values().forEach(u -> {
            add(new PotentialFieldDrawable(u.getPotentialField()));
        });
    }
}
