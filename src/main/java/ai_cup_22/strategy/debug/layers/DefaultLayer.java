package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleSegment;
import ai_cup_22.strategy.debug.primitives.Line;

public class DefaultLayer extends DrawLayer {
    public void update(World world) {
        clear();

        addShootLines(world);
        addShootAreas(world);
    }

    private void addShootLines(World world) {
        for (var unit: world.getMyUnits().values()) {
            for (var enemy : world.getEnemyUnits().values()) {
                if (unit.canShoot(enemy)) {
                    add(new Line(unit.getPosition(), enemy.getPosition(), Colors.GREEN_TRANSPARENT));
                } else {
                    add(new Line(unit.getPosition(), enemy.getPosition(), Colors.RED_TRANSPARENT));
                }
            }
        }
    }

    private void addShootAreas(World world) {
        for (var unit: world.getAllUnits().values()) {
            if (unit.hasWeapon()) {
                add(new CircleSegment(unit.getShootingSegment(), Colors.LIGHT_BLUE_TRANSPARENT));
            }
        }
    }
}
