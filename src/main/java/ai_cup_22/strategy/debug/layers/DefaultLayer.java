package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.CircleSegmentDrawable;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.PathDrawable;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;

public class DefaultLayer extends DrawLayer {
    public void update(World world) {
//        addShootLines(world);
        addShootAreas(world);
        addLookLines(world);

        addUnitPaths(world);
        addUnitStrategies(world);
        addUnitInfo(world);
        addPhantomUnits(world);

        addBullets(world);

        addCursorPosition();
    }

    private void addLookLines(World world) {
        world.getMyUnits().values().forEach(unit -> {
            if (unit.getLookBackPosition() != null) {
                DebugData.getInstance().getDefaultLayer().addLine(unit.getPosition(), unit.getLookBackPosition(),
                        Colors.VIOLET_TRANSPARENT);
            }
            if (unit.getLookBackVector() != null) {
                DebugData.getInstance().getDefaultLayer().addLine(unit.getPosition(), unit.getPosition().move(unit.getLookBackVector()),
                        Colors.YELLOW_TRANSPARENT);
            }
            if (unit.getLookPosition() != null) {
                DebugData.getInstance().getDefaultLayer().addLine(unit.getPosition(), unit.getLookPosition(),
                        Colors.LIGHT_GREEN_TRANSPARENT);
            }
        });
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

    private void addUnitInfo(World world) {
        world.getMyUnits().values().forEach(unit -> {
            add(new Text(
                    String.format("Shields: %d\n CD: %d \n Aim: %d", unit.getShieldPotions(), unit.getRemainingCoolDownTicks(), unit.getRemainedTicksToAim()),
                    unit.getPosition(),
                    0.3,
                    new Vector(0, -2)
            ));
        });
        world.getAllEnemyUnits().forEach(unit -> {
            add(new Text(
                    String.format("Shields: %d\n CD: %d", unit.getShieldPotions(), unit.getRemainingCoolDownTicks()),
                    unit.getPosition(),
                    0.3,
                    new Vector(0, -2)
            ));
        });
    }

    private void addBullets(World world) {
        world.getBullets().values().forEach(bullet -> {
            if (bullet.isSimulated()) {
                add(new CircleDrawable(new Circle(bullet.getPosition(), 0.3), Colors.ORANGE_TRANSPARENT));
            }

            if (bullet.isEnemy()) {
                add(new Line(bullet.getRealTrajectory(), Colors.RED_TRANSPARENT));
            }

            addText(bullet.getStartTick() + "", bullet.getPosition(), 0.1);
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
                add(new CircleSegmentDrawable(unit.getShootingSegment(), Colors.LIGHT_BLUE_TRANSPARENT));
            }
        }
        for (var unit: world.getEnemyUnits().values()) {
            if (unit.hasWeapon()) {
                add(new CircleSegmentDrawable(unit.getShootingSegment(), Colors.LIGHT_BLUE_TRANSPARENT));
            }
        }
    }
}
