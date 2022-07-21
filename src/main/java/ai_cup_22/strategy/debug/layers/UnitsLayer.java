package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.PotentialFieldDrawable;
import java.util.stream.Collectors;

public class UnitsLayer extends DrawLayer {
    private Integer i;

    public void reinit(World world) {
        clear();

//        for (var unit: world.getMyUnits().values()) {
//            add(new CircleDrawable(unit.getCircle(), Colors.GREEN_TRANSPARENT));
//        }
//
//        for (var unit: world.getEnemyUnits().values()) {
//            add(new CircleDrawable(unit.getCircle(), Colors.RED_TRANSPARENT));
//        }

        if (i == null) {
            return;
        }

        var units = World.getInstance().getMyUnits().values().stream().collect(Collectors.toList());
        if (units.size() <= i) {
            return;
        }

        add(new PotentialFieldDrawable(units.get(i).getPotentialField()));
    }

    public void updateUnit(Integer i) {
        this.i = i;
        this.reinit(World.getInstance());
    }
}
