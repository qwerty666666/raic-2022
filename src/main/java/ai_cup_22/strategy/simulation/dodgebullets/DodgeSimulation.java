package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.DodgeBulletsAction.DodgeDirection;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.utils.MathUtils;
import ai_cup_22.strategy.utils.MovementUtils;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DodgeSimulation {
    public DodgeDirection simulate(Unit unit) {
        var bullets = getBullets(unit);

        if (bullets.isEmpty()) {
            return null;
        }

        var dodgeResults = simulateDodge(unit, bullets);

        // take best direction
        var bestDirection = dodgeResults.stream()
                .min(Comparator.comparingDouble(DodgeResult::getTakenDmg)
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
                        .thenComparing(Comparator.comparingDouble((DodgeResult r) -> r.getDodgeDirection().getScore(unit)).reversed())
                        .thenComparingDouble(DodgeResult::getTicks)
                )
                .map(DodgeResult::getDodgeDirection)
                .orElse(null);

        drawDebugDodgeResult(unit, dodgeResults, bestDirection);

        return bestDirection;
    }

    private void drawDebugDodgeResult(Unit unit, List<DodgeResult> dodgeResults, DodgeDirection bestDirection) {
        if (DebugData.isEnabled) {
            dodgeResults.stream()
                    .forEach(res -> {
                        var dodgeDirection = res.getDodgeDirection();
                        DebugData.getInstance().getDefaultLayer().addLine(
                                unit.getPosition(), unit.getPosition().move(dodgeDirection.getDirection()), Colors.BLUE_TRANSPARENT
                        );
                        DebugData.getInstance().getDefaultLayer().addText(
                                Double.toString(res.takenDmg), unit.getPosition().move(dodgeDirection.getDirection()), 0.3
                        );
                    });

            if (bestDirection != null) {
                for (var step: bestDirection.getResult().getSteps()) {
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


    private List<DodgeResult> simulateDodge(Unit unit, Set<Bullet> bullets) {
        var nonWalkThroughObstacles = getNonWalkThroughObstaclesInRange(unit , 15);
        var directions = getPossibleDirectionsToDodge(unit, bullets);

        List<DodgeResult> results = new ArrayList<>();

        // try with aim
        results.addAll(simulateDodgeBulletsInDirections(directions, unit, bullets, nonWalkThroughObstacles));
        if (results.stream().anyMatch(res -> res.getTakenDmg() == 0)) {
            return results;
        }

        if (bullets.stream().anyMatch(b -> b.isWand() || b.isBow())) {
            // try without aim
            directions.forEach(d -> d.setWithAim(false));
            results.addAll(simulateDodgeBulletsInDirections(directions, unit, bullets, nonWalkThroughObstacles));
            if (results.stream().anyMatch(res -> res.getTakenDmg() == 0)) {
                return results;
            }

            // try with rotate
            directions.forEach(d -> d.setWithRotateToDirection(true));
            results.addAll(simulateDodgeBulletsInDirections(directions, unit, bullets, nonWalkThroughObstacles));
        }

        return results;
    }

    private List<DodgeResult> simulateDodgeBulletsInDirections(List<DodgeDirection> directions, Unit unit, Set<Bullet> bullets,
            List<Circle> nonWalkThroughObstacles) {
        return directions.stream()
                .map(direction -> simulate(unit, bullets, direction, nonWalkThroughObstacles))
                .collect(Collectors.toList());
    }

    private DodgeResult simulate(Unit unit, Set<Bullet> bullets, DodgeDirection dodgeDirection, List<Circle> nonWalkThroughObstacles) {
        var shouldSimulateAim = dodgeDirection.isWithAim();
        var shouldRotateToDirection = dodgeDirection.isWithRotateToDirection();

        var result = new DodgeResult(dodgeDirection);
        result.setDodgePosition(unit.getPosition());
        result.getDodgeDirection().setDodgeResult(result);

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
                lookDirection = MovementUtils.simulateRotateTickToDirection(lookDirection, directionVelocity, aim, unit.getAimRotationSpeed());
            } else if (unit.getLookPosition() != null) {
                lookDirection = MovementUtils.simulateRotateTickToDirection(lookDirection, new Vector(unitCircle.getCenter(), unit.getLookPosition()),
                        aim, unit.getAimRotationSpeed());
            }

            velocity = MovementUtils.getVelocityOnNextTickAfterCollision(
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

                if (MovementUtils.isBulletHitCircle(tickTrajectory, unitCircle) || MovementUtils.isBulletHitCircle(tickTrajectory, newUnitCircle)) {
                    if (MovementUtils.isBulletHitInTick(tickTrajectory, unitCircle, newUnitCircle)) {
                        result.setHit(true);
                        result.increaseTakenDmg(bullet.getDmg());
                        bulletSet.remove(bullet);
                        unitHealth -= bullet.getDmg();
                    }
                }
            }

            // go to next iteration cycle

            unitCircle = newUnitCircle;
            remainingCoolDownTicks--;

            result.setTicks(result.getTicks() + 1);
            result.setDodgePosition(unitCircle.getCenter());
            result.getSteps().add(unitCircle.getCenter());

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

        return result;
    }

    private Set<ai_cup_22.strategy.models.Bullet> getBullets(Unit unit) {
        return World.getInstance().getBullets().values().stream()
                .filter(ai_cup_22.strategy.models.Bullet::isEnemy)  // TODO
                .filter(bullet -> {
                    // can run into trajectory
                    return unit.getCircle().enlarge(unit.getMaxForwardSpeedPerTick() * (bullet.getRealRemainingLifetimeTicks() + 1))
                            .isIntersect(bullet.getRealTrajectory());
                })
                .collect(Collectors.toSet());
    }

    private List<DodgeDirection> getPossibleDirectionsToDodge(Unit unit, Collection<ai_cup_22.strategy.models.Bullet> bullets) {
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

        return IntStream.range(0, directionsToDodge.size())
                .mapToObj(ind -> new DodgeDirection(directionsToDodge.get(ind).normalizeToLength(10), ind))
                .collect(Collectors.toList());
    }

    private List<Vector> getTrajectoriesToAvoidNonMoveThroughObstacles(Unit unit) {
        return getNonWalkThroughObstaclesInRange(unit, 15).stream()
                .map(circle -> circle.enlarge(unit.getCircle().getRadius()))
                .flatMap(circle -> circle.getTangentPoints(unit.getPosition()).stream())
                .map(tangentPoint -> new Vector(unit.getPosition(), tangentPoint))
                .collect(Collectors.toList());
    }

    private List<Circle> getNonWalkThroughObstaclesInRange(Unit unit, double maxDist) {
        var obstacles = World.getInstance().getObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getDistanceTo(unit.getPosition()) < maxDist)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        var units = Stream.concat(
                        World.getInstance().getMyUnits().values().stream(),
                        World.getInstance().getEnemyUnits().values().stream()
                )
                .filter(u -> u.getId() != unit.getId())
                .filter(u -> !u.isPhantom() && u.isSpawned())
                .filter(u -> u.getDistanceTo(unit) < maxDist)
                .map(Unit::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(units);

        return obstacles;
    }




    public static class DodgeResult {
        private DodgeDirection dodgeDirection;
        private Position dodgePosition;
        private boolean isHit;
        private int ticks;
        private double takenDmg;
        private List<Position> steps = new ArrayList<>();

        public DodgeResult(DodgeDirection dodgeDirection) {
            this.dodgeDirection = dodgeDirection;
        }

        public DodgeDirection getDodgeDirection() {
            return dodgeDirection;
        }

        public boolean isHit() {
            return isHit;
        }

        public double getTakenDmg() {
            return takenDmg;
        }

        public DodgeResult increaseTakenDmg(double takenDmg) {
            this.takenDmg += takenDmg;
            return this;
        }

        public boolean isSuccess() {
            return !isHit;
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

        public DodgeResult setDodgePosition(Position dodgePosition) {
            this.dodgePosition = dodgePosition;
            return this;
        }

        public DodgeResult setHit(boolean hit) {
            isHit = hit;
            return this;
        }

        public DodgeResult setTicks(int ticks) {
            this.ticks = ticks;
            return this;
        }

        public DodgeResult setSteps(List<Position> steps) {
            this.steps = steps;
            return this;
        }
    }
}
