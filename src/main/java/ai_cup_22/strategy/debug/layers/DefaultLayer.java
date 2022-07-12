package ai_cup_22.strategy.debug.layers;

import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.CircleSegment;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.PathDrawable;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Vector;

public class DefaultLayer extends DrawLayer {
    public void update(World world) {
        clear();

        addShootLines(world);
        addShootAreas(world);

        addUnitPaths(world);
        addUnitStrategies(world);

        addBullets(world);

//        addCursorPosition();
    }

    private void addUnitStrategies(World world) {
        world.getMyUnits().values().forEach(unit -> {
            add(new Text(unit.getBehaviourTree().getStrategy().toString(), unit.getPosition(), 0.5, new Vector(0, 2)));
        });
    }

    private void addBullets(World world) {
        world.getBullets().values().forEach(bullet -> {
            Color color;
            if (bullet.isSimulated()) {
                color = Colors.ORANGE_TRANSPARENT;
            } else if (bullet.isEnemy()) {
                color = Colors.RED_TRANSPARENT;
            } else {
                color = Colors.GREEN_TRANSPARENT;
            }

            add(new CircleDrawable(new Circle(bullet.getPosition(), 0.3), color));
        });
    }

    private void addUnitPaths(World world) {
        world.getMyUnits().values().forEach(unit -> add(new PathDrawable(unit.getCurrentPath())));
    }

    private void addCursorPosition() {
        DebugData.getInstance().getCursorPosition().ifPresent(position -> {
            add(new Text(position.toString(), position));
        });
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
//                if (unit.isMe()) add(new CircleSegment(unit.getViewSegment(), Colors.YELLOW_TRANSPARENT));
            }
        }
    }
}
