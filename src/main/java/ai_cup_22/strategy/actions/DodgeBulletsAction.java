package ai_cup_22.strategy.actions;

import ai_cup_22.model.ActionOrder.Aim;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.utils.MovementUtils;
import ai_cup_22.strategy.utils.MovementUtils.DodgeResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DodgeBulletsAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var velocityToDodge = getVelocityToDodgeNearPossibleBullet(unit);

        if (velocityToDodge != null) {
            new MoveToAction(unit.getPosition().move(velocityToDodge)).apply(unit, order);
        }
    }

    private Vector getVelocityToDodgeNearPossibleBullet(Unit unit) {
        // take only bullets that threat me
        var bullets = World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .filter(bullet -> isBulletTreatsUnit(unit, bullet))
                .sorted(Comparator.comparingDouble(b -> b.getPosition().getDistanceTo(unit.getPosition())))
                .collect(Collectors.toList());

        // dodge from first possible bullet
        var directionsToDodge = bullets.stream()
                .map(bullet -> getDirectionsToDodgeBullet(unit, bullet))
                .filter(dodgeDirections -> dodgeDirections.stream().anyMatch(DodgeDirection::canDodgeBullet))
                .findFirst().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (DebugData.isEnabled) {
            for (var dodgeDirection: directionsToDodge) {
                DebugData.getInstance().getDefaultLayer().add(
                        new Line(unit.getPosition(), unit.getPosition().move(dodgeDirection.getDirection()), Colors.BLUE_TRANSPARENT)
                );
                DebugData.getInstance().getDefaultLayer().add(
                        new Text(Double.toString(dodgeDirection.getScore(unit)), unit.getPosition().move(dodgeDirection.getDirection()), 0.2)
                );

                for (var step: dodgeDirection.getDodgeResult().getSteps()) {
                    DebugData.getInstance().getDefaultLayer().add(
                            new CircleDrawable(
                                    new Circle(step, unit.getCircle().getRadius()),
                                    dodgeDirection.canDodgeBullet() ? Colors.GREEN_TRANSPARENT : Colors.RED_TRANSPARENT,
                                    false
                            )
                    );
                }
            }
        }

        // take best direction
        var bestDirection = directionsToDodge.stream()
                .min(Comparator.comparing(DodgeDirection::canDodgeBullet).reversed()
                            .thenComparing(Comparator.comparingDouble((DodgeDirection d) -> d.getScore(unit)).reversed())
                            .thenComparingDouble(d -> d.getDodgeResult().getTicks())
                )
                .orElse(null);

        if (DebugData.isEnabled && bestDirection != null) {
            for (var step: bestDirection.getDodgeResult().getSteps()) {
                DebugData.getInstance().getDefaultLayer().add(
                        new CircleDrawable(
                                new Circle(step, unit.getCircle().getRadius()),
                                Colors.ORANGE,
                                false
                        )
                );
            }
        }

        return bestDirection == null ? null : bestDirection.getDirection();
    }

    private List<DodgeDirection> getDirectionsToDodgeBullet(Unit unit, Bullet bullet) {
        var directions = getPossibleDirectionsToDodge(unit, bullet);

        for (var d: directions) {
            d.calculateResult(unit, bullet);
        }

        return directions;
    }

    private List<DodgeDirection> getPossibleDirectionsToDodge(Unit unit, Bullet bullet) {
        var bulletVelocity = bullet.getVelocity();

        var directionsToDodge = new ArrayList<Vector>();

        // predefined
        directionsToDodge.addAll(List.of(
                bulletVelocity.rotate(Math.PI / 2),
                bulletVelocity.rotate(-Math.PI / 2),
                bulletVelocity.rotate(Math.PI / 4),
                bulletVelocity.rotate(-Math.PI / 4),
                bulletVelocity
        ));

        // tangents to obstacles
        directionsToDodge.addAll(
                // take first direction which are further to the bullet
                // (to not run on next bullets)
                getTrajectoriesToAvoidNonMoveThroughObstacles(unit).stream()
                        .sorted(Comparator.comparingDouble((Vector direction) -> {
                                return direction.getEndPosition().getDistanceTo(unit.getPosition());
                            })
                            .reversed()
                        )
                        .collect(Collectors.toList())
        );

        return IntStream.range(0, directionsToDodge.size())
                .mapToObj(ind -> new DodgeDirection(directionsToDodge.get(ind).normalizeToLength(10), ind))
                .collect(Collectors.toList());
    }

    private List<Vector> getTrajectoriesToAvoidNonMoveThroughObstacles(Unit unit) {
        return MovementUtils.getNonWalkThroughObstaclesInRange(unit, 10).stream()
                .map(circle -> circle.enlarge(unit.getCircle().getRadius()))
                .flatMap(circle -> circle.getTangentPoints(unit.getPosition()).stream())
                .map(tangentPoint -> new Vector(unit.getPosition(), tangentPoint))
                .collect(Collectors.toList());
    }

    private boolean isBulletTreatsUnit(Unit unit, Bullet bullet) {
        return unit.getCircle().enlarge(0.35).isIntersect(bullet.getTrajectory());
    }

    private static class DodgeDirection {
        private Vector direction;
        private int priority;
        private DodgeResult result;
        private Double score;

        public DodgeDirection(Vector direction, int priority) {
            this.direction = direction;
            this.priority = priority;
        }

        public Vector getDirection() {
            return direction;
        }

        public int getPriority() {
            return priority;
        }

        public DodgeResult getDodgeResult() {
            return result;
        }

        public void calculateResult(Unit unit, Bullet bullet) {
            result = MovementUtils.tryDodgeByWalkDirect(unit, direction, bullet);
        }

        public boolean canDodgeBullet() {
            return result.isSuccess();
        }

        public double getScore(Unit unit) {
            if (score == null) {
                score = unit.getPotentialField().getScoreValue(getDodgeResult().getDodgePosition());
            }
            return score;
        }

        @Override
        public String toString() {
            return direction + " " + canDodgeBullet() + " " + score;
        }
    }
}
