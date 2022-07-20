package ai_cup_22.strategy.debug.layers;

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
import ai_cup_22.strategy.models.Unit;

public class DefaultLayer extends DrawLayer {
    public void update(World world) {
        addShootLines(world);
        addShootAreas(world);

        addUnitPaths(world);
        addUnitStrategies(world);
        addUnitShields(world);
        addPhantomUnits(world);

        addBullets(world);

        addCursorPosition();
    }

    private void addPhantomUnits(World world) {
        world.getEnemyUnits().values().forEach(unit -> {
            addCircle(unit.getPosition(), unit.getCircle().getRadius(), Colors.RED_TRANSPARENT);
//            addText(unit.getId() + "", unit.getPosition());
        });
        world.getPhantomEnemies().values().forEach(unit -> {
            addCircle(unit.getPosition(), unit.getCircle().getRadius(), Colors.RED_TRANSPARENT);
            if (unit.getId() >= 0) {
                addText(unit.getId() + " (" + unit.getTicksSinceLastUpdate() + ")", unit.getPosition());
            } else {
                addText("(" + unit.getTicksSinceLastUpdate() + ")", unit.getPosition());
            }
            addRing(unit.getPossibleLocationCircle().getCenter(), unit.getPossibleLocationCircle().getRadius(), Colors.GRAY_TRANSPARENT);
        });
    }

    private void addUnitStrategies(World world) {
        world.getMyUnits().values().forEach(unit -> {
            add(new Text(unit.getBehaviourTree().getStrategy().toString(), unit.getPosition(), 0.5, new Vector(0, 2)));
        });
    }

    private void addUnitShields(World world) {
        world.getMyUnits().values().forEach(unit -> {
            add(new Text("Shields: " + unit.getShieldPotions(), unit.getPosition(), 0.3, new Vector(0, -2)));
        });
        world.getEnemyUnits().values().forEach(unit -> {
            add(new Text("Shields: " + unit.getShieldPotions(), unit.getPosition(), 0.3, new Vector(0, -1)));
        });
    }

    private void addBullets(World world) {
        world.getBullets().values().forEach(bullet -> {
            if (bullet.isSimulated()) {
                add(new CircleDrawable(new Circle(bullet.getPosition(), 0.3), Colors.ORANGE_TRANSPARENT));
            }

            if (bullet.isEnemy()) {
                add(new Line(bullet.getTrajectory(), Colors.RED_TRANSPARENT));
            }
        });
    }

    private void addUnitPaths(World world) {
        world.getMyUnits().values().forEach(unit -> add(new PathDrawable(unit.getCurrentPath())));
    }

    private void addCursorPosition() {
        DebugData.getInstance().getCursorPosition().ifPresent(position -> {
            DebugData.getInstance().getClickPosition().ifPresent(clickedPos -> {
                add(new Text(Double.toString(position.getDistanceTo(clickedPos)), position, 0.2, new Vector(1, 0.5)));
                add(new Line(position, clickedPos, Colors.GRAY_TRANSPARENT));
            });
        });
    }

    private void addShootLines(World world) {
        for (var unit: world.getMyUnits().values()) {
            world.getEnemyUnits().values().stream()
                    .filter(Unit::isSpawned)
                    .filter(enemy -> enemy.getDistanceTo(unit) < 40)
                    .forEach(enemy -> {
                        if (unit.canShoot(enemy)) {
                            add(new Line(unit.getPosition(), enemy.getPosition(), Colors.GREEN_TRANSPARENT));
                        } else {
                            add(new Line(unit.getPosition(), enemy.getPosition(), Colors.RED_TRANSPARENT));
                        }
                    });

            var priorityEnemy = unit.getBehaviourTree().getFightStrategy().getPriorityEnemy();
            if (priorityEnemy != null) {
                addLine(unit.getPosition(), priorityEnemy.getPosition(), Colors.YELLOW_TRANSPARENT);
            }
        }
    }

    private void addShootAreas(World world) {
        for (var unit: world.getMyUnits().values()) {
            if (unit.hasWeapon()) {
                add(new CircleSegment(unit.getShootingSegment(), Colors.LIGHT_BLUE_TRANSPARENT));
            }
        }
        for (var unit: world.getEnemyUnits().values()) {
            if (unit.hasWeapon()) {
                add(new CircleSegment(unit.getShootingSegment(), Colors.LIGHT_BLUE_TRANSPARENT));
            }
        }
    }
}
