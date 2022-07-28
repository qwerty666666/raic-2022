package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.simulation.walk.WalkSimulation;
import ai_cup_22.strategy.utils.MathUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DodgeSimulation {
    public DodgeDirection simulate(Unit unit) {
        var bullets = getThreatenBullets(unit);
        if (bullets.isEmpty()) {
            return null;
        }

        // find all directions
        var dodgeDirections = simulateDodge(unit, bullets);

        // take best one
        var bestDirection = chooseBestDirection(unit, dodgeDirections, bullets);

        drawDebugDodgeResult(unit, dodgeDirections, bestDirection);

        return bestDirection;
    }

    private DodgeDirection chooseBestDirection(Unit unit, List<DodgeDirection> directions, List<Bullet> bullets) {
        return directions.stream()
                .min(Comparator.comparingDouble(DodgeDirection::getTakenDmg)
                        // take firstly:
                        // 1. with aim
                        // 2. without aim + without rotating
                        // 2. without aim + with rotating
                        .thenComparing(Comparator.comparing(DodgeDirection::isWithAim).reversed()
                                .thenComparing(DodgeDirection::isWithRotateToDirection)
                        )
                        // take position that is on safe dist from bullet trajectory
                        .thenComparing((d1, d2) -> {
                            var dist1 = bullets.stream()
                                    .mapToDouble(b -> b.getRealTrajectory().getDistanceTo(d1.getDodgePosition()))
                                    .min()
                                    .orElseThrow();
                            var dist2 = bullets.stream()
                                    .mapToDouble(b -> b.getRealTrajectory().getDistanceTo(d2.getDodgePosition()))
                                    .min()
                                    .orElseThrow();

                            if (dist1 < 1.35 || dist2 < 1.35) {
                                return Double.compare(dist2, dist1);
                            }

                            return 0;
                        })
                        .thenComparing(Comparator.comparingDouble((DodgeDirection r) -> r.getScore(unit)).reversed())
                        .thenComparingDouble(DodgeDirection::getTicks)
                )
                .orElse(null);
    }

    private void drawDebugDodgeResult(Unit unit, List<DodgeDirection> dodgeDirections, DodgeDirection bestDirection) {
        if (DebugData.isEnabled) {
            dodgeDirections.stream()
                    .forEach(direction -> {
                        DebugData.getInstance().getDefaultLayer().addLine(
                                unit.getPosition(), unit.getPosition().move(direction.getDirection()), getColor(direction)
                        );
                        DebugData.getInstance().getDefaultLayer().addText(
                                Double.toString(direction.takenDmg), unit.getPosition().move(direction.getDirection()), 0.3
                        );
                    });

            if (bestDirection != null) {
                for (var step: bestDirection.getSteps()) {
                    DebugData.getInstance().getDefaultLayer().add(
                            new CircleDrawable(new Circle(step, unit.getCircle().getRadius()), getColor(bestDirection), false)
                    );
                }
            }
        }
    }

    private Color getColor(DodgeDirection direction) {
        if (direction.isWithAim()) {
            return Colors.GREEN_TRANSPARENT;
        }
        if (direction.isWithRotateToDirection()) {
            return Colors.ORANGE_TRANSPARENT;
        }
        return Colors.YELLOW_TRANSPARENT;
    }

    private List<DodgeDirection> simulateDodge(Unit unit, List<Bullet> bullets) {
        var nonWalkThroughObstacles = WalkSimulation.getNonWalkThroughObstaclesInRange(unit , 15);
        var possibleDirections = getPossibleDirectionsToDodge(unit, bullets);

        List<DodgeDirection> results = new ArrayList<>();

        var shouldTryWithoutAim = bullets.stream().anyMatch(b -> b.isWand() || b.isBow());

        for (var direction: possibleDirections) {
            // try with aim
            var directionWithAim = new DodgeDirection(direction, true, false);
            simulateForDirection(unit, bullets, directionWithAim, nonWalkThroughObstacles);
            if (directionWithAim.takenDmg == 0) {
                results.add(directionWithAim);
                continue;
            }

            if (shouldTryWithoutAim) {
                // try without aim
                var directionWithoutAim = new DodgeDirection(direction, false, false);
                simulateForDirection(unit, bullets, directionWithoutAim, nonWalkThroughObstacles);
                if (directionWithoutAim.takenDmg == 0) {
                    results.add(directionWithoutAim);
                    continue;
                }

                // try with rotate
                var directionWithRotate = new DodgeDirection(direction, false, true);
                simulateForDirection(unit, bullets, directionWithRotate, nonWalkThroughObstacles);
                if (directionWithRotate.takenDmg == 0) {
                    results.add(directionWithRotate);
                    continue;
                }
            }

            results.add(directionWithAim);
        }

        return results;
    }

    private void simulateForDirection(Unit unit, List<Bullet> bullets, DodgeDirection dodgeDirection, List<Circle> nonWalkThroughObstacles) {
        var shouldSimulateAim = dodgeDirection.isWithAim();
        var shouldRotateToDirection = dodgeDirection.isWithRotateToDirection();

        dodgeDirection.setDodgePosition(unit.getPosition());

        var bulletSet = bullets.stream()
                .map(ai_cup_22.strategy.simulation.dodgebullets.Bullet::new)
                .collect(Collectors.toSet());

        var directionVelocity = dodgeDirection.getDirection();
        var unitCircle = unit.getCircle();
        var aim = unit.getAim();
        var velocity = unit.getVelocityPerTick();
        var unitHealth = unit.getFullHealth();
        var remainingCoolDownTicks = unit.getRemainingCoolDownTicks();
        var ticksToFullAim = unit.getTicksToFullAim();
        var lookDirection = unit.getDirection();

        while (!bulletSet.isEmpty()) {

            var bulletsSetClone = new ArrayList<>(bulletSet);

            // move unit

            var shouldAim = shouldSimulateAim && (unit.isAiming() || remainingCoolDownTicks <= ticksToFullAim);
            aim = MathUtils.restrict(0, 1, aim + (shouldAim ? unit.getAimChangePerTick() : -unit.getAimChangePerTick()));

            if (shouldRotateToDirection) {
                lookDirection = WalkSimulation.simulateRotateTickToDirection(lookDirection, directionVelocity, aim, unit.getAimRotationSpeed());
            } else if (unit.getLookPosition() != null) {
                lookDirection = WalkSimulation.simulateRotateTickToDirection(lookDirection, new Vector(unitCircle.getCenter(), unit.getLookPosition()),
                        aim, unit.getAimRotationSpeed());
            }

            velocity = WalkSimulation.getVelocityOnNextTickAfterCollision(
                    unitCircle.getCenter(), lookDirection, velocity,
                    unit.getMaxForwardSpeedPerTick(), unit.getMaxBackwardSpeedPerTick(),
                    aim, unit.getAimSpeedModifier(),
                    directionVelocity,
                    nonWalkThroughObstacles
            );
            var newUnitCircle = unitCircle.move(velocity);

            // move bullets

            for (var bullet: bulletsSetClone) {
                var bulletTrajectory = bullet.getTrajectory();
                var bulletPos = bullet.getPosition();

                var newBulletPos = bulletPos.move(bullet.getVelocity()).getDistanceTo(bulletTrajectory.getStart()) >
                        bulletTrajectory.getLength() ? bulletTrajectory.getEnd() : bulletPos.move(bullet.getVelocity());
                var tickTrajectory = new Line(bulletPos, newBulletPos);
                bullet.simulateTick();

                // check that bullet hits unit

                if (isBulletHitCircle(tickTrajectory, unitCircle) || isBulletHitCircle(tickTrajectory, newUnitCircle)) {
                    if (isBulletHitInTick(tickTrajectory, unitCircle, newUnitCircle)) {
                        dodgeDirection.increaseTakenDmg(bullet.getDmg());
                        bulletSet.remove(bullet);
                        unitHealth -= bullet.getDmg();
                    }
                }
            }

            // go to next iteration cycle

            unitCircle = newUnitCircle;
            remainingCoolDownTicks--;

            dodgeDirection.increaseTick();
            dodgeDirection.setDodgePosition(unitCircle.getCenter());
            if (DebugData.isEnabled) {
                dodgeDirection.getSteps().add(unitCircle.getCenter());
            }

            // check that unit run away from hit trajectory

            for (var bullet: bulletsSetClone) {
                if (bullet.isDead()) {
                    bulletSet.remove(bullet);
                } else {
                    var projection = bullet.getTrajectory().getProjection(unitCircle.getCenter());
                    if (projection.getDistanceTo(bullet.getTrajectory().getStart()) < bullet.getPosition().getDistanceTo(bullet.getTrajectory().getStart())) {
                        bulletSet.remove(bullet);
                    }
                }
            }

            if (unitHealth <= 0) {
                break;
            }
        }
    }

    private List<ai_cup_22.strategy.models.Bullet> getThreatenBullets(Unit unit) {
        return World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .filter(bullet -> {
                    // can run into trajectory
                    return unit.getCircle().enlarge(unit.getMaxForwardSpeedPerTick() * (bullet.getRealRemainingLifetimeTicks() + 1))
                            .isIntersect(bullet.getRealTrajectory());
                })
                .collect(Collectors.toList());
    }

    private List<Vector> getPossibleDirectionsToDodge(Unit unit, Collection<ai_cup_22.strategy.models.Bullet> bullets) {
        var directionsToDodge = new ArrayList<Vector>();

        // predefined
        var unitDirection = unit.getDirection();

        for (var i = 1; i <= 12; i++) {
            directionsToDodge.add(unitDirection.rotate(Math.PI * 2 / 12 * i));
        }

        bullets.stream()
                .filter(bullet -> bullet.isBow() || bullet.isWand())
                .forEach(bullet -> {
                    var bulletVelocity = bullet.getVelocity();

                    directionsToDodge.addAll(List.of(
                            bulletVelocity.rotate(Math.PI / 2),
                            bulletVelocity.rotate(-Math.PI / 2),
                            bulletVelocity.rotate(Math.PI / 4),
                            bulletVelocity.rotate(-Math.PI / 4),
                            bulletVelocity
                    ));
                });

        // tangents to obstacles
        directionsToDodge.addAll(getTrajectoriesToAvoidNonMoveThroughObstacles(unit));

        return directionsToDodge.stream()
                .map(d -> d.normalizeToLength(10))
                .collect(Collectors.toList());
    }

    private List<Vector> getTrajectoriesToAvoidNonMoveThroughObstacles(Unit unit) {
        return WalkSimulation.getNonWalkThroughObstaclesInRange(unit, 15).stream()
                .map(circle -> circle.enlarge(unit.getCircle().getRadius()))
                .flatMap(circle -> circle.getTangentPoints(unit.getPosition()).stream())
                .map(tangentPoint -> new Vector(unit.getPosition(), tangentPoint))
                .collect(Collectors.toList());
    }

    private boolean isBulletHitInTick(Line bulletTickTrajectory, Circle oldPosition, Circle newPosition) {
        var x0 = oldPosition.getCenter().getX();
        var y0 = oldPosition.getCenter().getY();

        var x1 = bulletTickTrajectory.getStart().getX();
        var y1 = bulletTickTrajectory.getStart().getY();

        var v0x = (newPosition.getCenter().getX() - x0);
        var v0y = (newPosition.getCenter().getY() - y0);

        var v1x = (bulletTickTrajectory.getEnd().getX() - x1);
        var v1y = (bulletTickTrajectory.getEnd().getY() - y1);

        var a = Math.pow(v1x - v0x, 2) + Math.pow(v1y - v0y, 2);
        var b = 2 * (v1x - v0x) * (x1 - x0) + 2 * (v1y - v0y) * (y1 - y0);
        var c = (x1 * x1 - 2 * x1 * x0 + x0 * x0) + (y1 * y1 - 2 * y1 * y0 + y0 * y0) - 1;

        return b * b - 4 * a * c >= 0;
    }

    private boolean isBulletHitCircle(Line bulletTickTrajectory, Circle unitCircle) {
        if (unitCircle.isIntersect(bulletTickTrajectory)) {
            var hitUnitPosition = bulletTickTrajectory.getIntersectionPoints(unitCircle).stream()
                    .min(Comparator.comparingDouble(point -> point.getDistanceTo(bulletTickTrajectory.getStart())))
                    .orElse(null);

            return hitUnitPosition == null ||
                    bulletTickTrajectory.getEnd().getDistanceTo(bulletTickTrajectory.getStart()) >
                            hitUnitPosition.getDistanceTo(bulletTickTrajectory.getStart());
        }

        return false;
    }


    public static class DodgeDirection {
        private boolean withAim = true;
        private boolean withRotateToDirection = false;

        private final Vector direction;

        private Position dodgePosition;
        private int ticks;
        private double takenDmg;
        private List<Position> steps = new ArrayList<>();

        private Double score;

        public DodgeDirection(Vector direction) {
            this.direction = direction;
        }

        public DodgeDirection(Vector direction, boolean isWithAim, boolean isWithRotateToDirection) {
            this(direction);
            setWithAim(isWithAim);
            setWithRotateToDirection(isWithRotateToDirection);
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

        public double getScore(Unit unit) {
            if (score == null) {
                score = unit.getPotentialField().getScoreValue(dodgePosition);
            }
            return score;
        }

        public double getTakenDmg() {
            return takenDmg;
        }

        public void increaseTakenDmg(double takenDmg) {
            this.takenDmg += takenDmg;
        }

        public Position getDodgePosition() {
            return dodgePosition;
        }

        public int getTicks() {
            return ticks;
        }

        public List<Position> getSteps() {
            return steps;
        }

        public void setDodgePosition(Position dodgePosition) {
            this.dodgePosition = dodgePosition;
        }

        public void increaseTick() {
            this.ticks += 1;
        }

        public void setSteps(List<Position> steps) {
            this.steps = steps;
        }

        @Override
        public String toString() {
            return direction + " dmg: " + takenDmg + " score: " + score;
        }
    }
}
