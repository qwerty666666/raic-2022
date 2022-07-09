package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPotentialField implements PotentialField {
    private double[][] values;
    private double startX;
    private double startY;

    public StaticPotentialField(World world) {
        initValues();
        fillStaticData(world);
    }

    private void initValues() {
        var initRadius = World.getInstance().getConstants().getInitialZoneRadius();

        int size = (int) (initRadius * 2 * (1. / PotentialField.STEP_SIZE));
        this.values = new double[size][size];

        startX = -initRadius;
        startY = -initRadius;
    }

    private void fillStaticData(World world) {
        world.getObstacles().forEach((id, obstacle) -> {
            var circle = obstacle.getCircle();

            var radius = circle.getRadius() + 2.5;
            var obstaclesContributor = new CompositeForceContributor()
//                    .add(new ConstantOutCircleForceContributor(circle.enlargeToRadius(radius), 0))
                    .add(new ConstantInCircleForceContributor(circle, PotentialField.UNREACHABLE_VALUE))
                    .add(new LinearForceContributor(circle.getCenter(), -50, -10, circle.getRadius(), radius));

            var minX = Math.max(0, (int) ((circle.getCenter().getX() - radius - startX) / PotentialField.STEP_SIZE));
            var maxX = Math.min(values.length - 1, (int) ((circle.getCenter().getX() + radius - startX) / PotentialField.STEP_SIZE) + 1);
            var minY = Math.max(0, (int) ((circle.getCenter().getY() - radius - startY) / PotentialField.STEP_SIZE));
            var maxY = Math.min(values.length - 1, (int) ((circle.getCenter().getY() + radius - startY) / PotentialField.STEP_SIZE) + 1);

            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    var pos = getPositionByIndex(x, y);
                    values[x][y] += obstaclesContributor.getForce(pos);
                }
            }
        });
    }

    public List<Position> getPositionsInCircle(Circle circle) {
        var list = new ArrayList<Position>();

        var minX = Math.max(0, (int) ((circle.getCenter().getX() - circle.getRadius() - startX) / PotentialField.STEP_SIZE));
        var maxX = Math.min(values.length - 1, (int) ((circle.getCenter().getX() + circle.getRadius() - startX) / PotentialField.STEP_SIZE) + 1);
        var minY = Math.max(0, (int) ((circle.getCenter().getY() - circle.getRadius() - startY) / PotentialField.STEP_SIZE));
        var maxY = Math.min(values.length - 1, (int) ((circle.getCenter().getY() + circle.getRadius() - startY) / PotentialField.STEP_SIZE) + 1);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                var pos = getPositionByIndex(x, y);
                if (circle.contains(pos)) {
                    list.add(pos);
                }
            }
        }

        return list;
    }

    private Position getPositionByIndex(int x, int y) {
        return Position.getCached(x * PotentialField.STEP_SIZE + startX, y * PotentialField.STEP_SIZE + startY);
    }

    @Override
    public Map<Position, Double> getForces() {
        var map = new HashMap<Position, Double>();

        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < values[0].length; y++) {
                map.put(getPositionByIndex(x, y), values[x][y]);
            }
        }

        return map;
    }
}
