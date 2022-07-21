package ai_cup_22.strategy.actions;

import ai_cup_22.debugging.Color;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.LookToAction;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DodgeBulletsAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var dodgeDirection = getBestDodgeDirection(unit);

        if (dodgeDirection != null && dodgeDirection.canDodgeBullet()) {
            unit.setCurrentPath(Collections.emptyList());

            new MoveToAction(unit.getPosition().move(dodgeDirection.getDirection())).apply(unit, order);

            if (!dodgeDirection.isWithAim()) {
                order.setAction(null);
            }

            if (dodgeDirection.isWithRotateToDirection()) {
                new LookToAction(dodgeDirection.getDirection()).apply(unit, order);
            }
        }
    }

    private DodgeDirection getBestDodgeDirection(Unit unit) {
        // take only bullets that threats me
        var bullets = World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .filter(bullet -> isBulletTreatsUnit(unit, bullet))
                .sorted(Comparator.comparingDouble(b -> b.getPosition().getDistanceTo(unit.getPosition())))
                .collect(Collectors.toList());

        // dodge from first possible bullet
        List<DodgeDirection> directionsToDodge = Collections.emptyList();

        Bullet bulletToDodge = null;
        for (var bullet: bullets) {
            bulletToDodge = bullet;

            directionsToDodge = tryDodgeBullet(unit, bullet);

            if (directionsToDodge.stream().noneMatch(DodgeDirection::canDodgeBullet)) {
                if (DebugData.isEnabled) {
                    DebugData.getInstance().getDefaultLayer().addCircle(bullet.getPosition(), 0.2, Colors.RED_TRANSPARENT);
                }
            } else {
                break;
            }
        }

        // take best direction
        var bulletFinal = bulletToDodge;
        var bestDirection = directionsToDodge.stream()
                .min((d1, d2) -> {
                    if (!d1.canDodgeBullet() && !d2.canDodgeBullet()) {
                        return d2.getDodgeResult().getTicks() - d1.getDodgeResult().getTicks();
                    } else {
                        return Comparator.comparing(DodgeDirection::canDodgeBullet).reversed()
                                .thenComparing((dd1, dd2) -> {
                                    var dist1 = bulletFinal.getRealTrajectory().getDistanceTo(dd1.getDodgeResult().getDodgePosition());
                                    var dist2 = bulletFinal.getRealTrajectory().getDistanceTo(dd2.getDodgeResult().getDodgePosition());
                                    var dist = 0.2;
                                    if (dist1 < dist && dist2 >= dist) {
                                        return -1;
                                    }
                                    if (dist1 >= dist && dist2 < dist) {
                                        return 1;
                                    }
                                    return 0;
                                })
                                .thenComparing(Comparator.comparingDouble((DodgeDirection d) -> d.getScore(unit)).reversed())
                                .thenComparingDouble(d -> d.getDodgeResult().getTicks())
                                .compare(d1, d2);
                    }
                })
                .orElse(null);

        drawDebugDodgeResult(unit, directionsToDodge, bestDirection);

        return bestDirection;
    }

    private void drawDebugDodgeResult(Unit unit, List<DodgeDirection> directionsToDodge, DodgeDirection bestDirection) {
        if (DebugData.isEnabled) {
            for (var dodgeDirection: directionsToDodge) {
                DebugData.getInstance().getDefaultLayer().add(
                        new Line(unit.getPosition(), unit.getPosition().move(dodgeDirection.getDirection()), Colors.BLUE_TRANSPARENT)
                );
                DebugData.getInstance().getDefaultLayer().add(
                        new Text(Double.toString(dodgeDirection.getScore(unit)), unit.getPosition().move(dodgeDirection.getDirection()), 0.2)
                );
            }

            if (bestDirection != null) {
                for (var step: bestDirection.getDodgeResult().getSteps()) {
                    Color color;
                    if (!bestDirection.canDodgeBullet()) {
                        color = Colors.RED_TRANSPARENT;
                    } else if (bestDirection.isWithRotateToDirection()) {
                        color = Colors.ORANGE_TRANSPARENT;
                    } else if (!bestDirection.isWithAim()) {
                        color = Colors.YELLOW_TRANSPARENT;
                    } else {
                        color = Colors.GREEN_TRANSPARENT;
                    }

                    DebugData.getInstance().getDefaultLayer().add(
                            new CircleDrawable(new Circle(step, unit.getCircle().getRadius()), color, false)
                    );
                }
            }
        }
    }

    private List<DodgeDirection> tryDodgeBullet(Unit unit, Bullet bullet) {
        var directions = getPossibleDirectionsToDodge(unit, bullet);

        // try with aim
        simulateDodgeBulletsInDirections(directions, unit, bullet);
        if (directions.stream().anyMatch(DodgeDirection::canDodgeBullet)) {
            return directions;
        }

        if (bullet.isWand() || bullet.isBow()) {
            // try without aim
            directions.forEach(d -> d.setWithAim(false));
            simulateDodgeBulletsInDirections(directions, unit, bullet);
            if (directions.stream().anyMatch(DodgeDirection::canDodgeBullet)) {
                return directions;
            }

            // try with rotate
            directions.forEach(d -> d.setWithRotateToDirection(true));
            simulateDodgeBulletsInDirections(directions, unit, bullet);
        }

        return directions;
    }

    private void simulateDodgeBulletsInDirections(List<DodgeDirection> directions, Unit unit, Bullet bullet) {
        for (var d: directions) {
            d.calculateResult(unit, bullet);
        }
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
        return unit.getCircle().enlarge(2).isIntersect(bullet.getRealTrajectory());
    }

    private static class DodgeDirection {
        private Vector direction;
        private int priority;
        private DodgeResult result;
        private Double score;
        private boolean withAim = true;
        private boolean withRotateToDirection = false;

        public DodgeDirection(Vector direction, int priority) {
            this.direction = direction;
            this.priority = priority;
        }

        public boolean isWithAim() {
            return withAim;
        }

        public DodgeDirection setWithAim(boolean withAim) {
            this.withAim = withAim;
            return this;
        }

        public boolean isWithRotateToDirection() {
            return withRotateToDirection;
        }

        public DodgeDirection setWithRotateToDirection(boolean withRotateToDirection) {
            this.withRotateToDirection = withRotateToDirection;
            return this;
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
            result = MovementUtils.tryDodgeByWalkDirect(unit, direction, bullet, withAim, withRotateToDirection);
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
