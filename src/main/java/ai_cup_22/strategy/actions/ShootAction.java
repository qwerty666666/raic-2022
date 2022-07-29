package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.simulation.walk.WalkSimulation;
import ai_cup_22.strategy.utils.MathUtils;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ShootAction implements Action {
    private final Unit target;
    private final Unit me;
    private final Position bestPositionToShoot;
    private final boolean shouldShoot;
    private final boolean shouldStartAimingToEnemy;
    private final boolean shouldRotateToEnemy;
    private final Vector bestLookDirection;

    public ShootAction(Unit me, Unit target) {
        this.me = me;
        this.target = target;
        this.bestPositionToShoot = getBestPositionToShoot(me, target);
        this.bestLookDirection = getBestLookDirection(bestPositionToShoot);
        this.shouldStartAimingToEnemy = shouldStartAimingToEnemy();
        this.shouldRotateToEnemy = shouldStartAimingToEnemy || getTicksToCanShootEnemy() - 2 >= me.getRemainingCoolDownTicks();
        this.shouldShoot = shouldShoot(me, bestPositionToShoot, target);
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var action = new CompositeAction();

        if (shouldStartAimingToEnemy) {
            action.add(new AimAction(shouldShoot));
        }
        // on dist 18 stay half-aimed
        if (target.isSpawned() && target.getDistanceTo(me) < 18 && me.getRemainedTicksToAim() >= 15) {
            action.add(new AimAction(shouldShoot));
        }
        if (shouldStartAimingToEnemy || shouldRotateToEnemy || unit.getLookPosition() == null) {
            var lookDirection = getBestLookDirection(bestPositionToShoot);
            action.add(new LookToAction(lookDirection));
        }

        action.apply(unit, order);
    }

    public boolean isShootWillDamageEnemy(Unit me, Unit enemy) {
        var shootingSegment = getShootingSegmentOnStartNextTick();
        if (!shootingSegment.containsOrIntersects(enemy.getCircle())) {
            return false;
        }

        var dist = me.getDistanceTo(enemy) - me.getCircle().getRadius();
        var bulletSpeed = me.getWeaponOptional().map(Weapon::getSpeedPerTick).orElse(0.);
        var ticksToHit = (int) Math.ceil(dist / bulletSpeed);
        var trajectory = new Vector(enemy.getPosition(), me.getPosition()).normalizeToLength(100);
        var dodgeDirection1 = trajectory.rotate(Math.PI / 2);
        var dodgeDirection2 = trajectory.rotate(-Math.PI / 2);

        var pos1 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection1, ticksToHit);
        var pos2 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection2, ticksToHit);

        return shootingSegment.containsOrIntersects(enemy.getCircle().moveToPosition(pos1)) &&
                shootingSegment.containsOrIntersects(enemy.getCircle().moveToPosition(pos2));
    }

    private Vector getBestLookDirection(Position lookPosition) {
        var shootingLine = new Line(me.getPosition(), lookPosition);
        var shootingVector = shootingLine.toVector();

        var obstacle = World.getInstance().getNonShootThroughObstacles().values().stream()
                .filter(ob -> ob.getCircle().isIntersect(shootingLine))
                .min(Comparator.comparingDouble(ob -> ob.getCenter().getDistanceTo(me.getPosition())))
                .orElse(null);
        if (obstacle == null) {
            return shootingVector;
        }

        var tangent = obstacle.getCircle().getTangentLines(me.getPosition()).stream()
                .min(Comparator.comparingDouble(line -> line.toVector().getAngleTo(shootingVector)))
                .map(Line::toVector)
                .orElse(null);
        if (tangent == null || tangent.getAngleTo(shootingVector) > me.getShootingSegment().getAngle()) {
            return shootingVector;
        }

        var vectorToTree = new Vector(me.getPosition(), obstacle.getCenter());

        return tangent.rotate(Math.signum(vectorToTree.getDiffToVector(tangent)) * me.getShootingSegment().getAngle() / 2);
    }

    public boolean isShouldStartAimingToEnemy() {
        return shouldStartAimingToEnemy;
    }

    private boolean shouldStartAimingToEnemy() {
        return getTicksToCanShootEnemy() - 2 <= me.getRemainedTicksToAim();
    }

    private int getTicksToCanShootEnemy() {
        var ticksToSpawn = target.getRemainingSpawnTicks();
        int ticksToShootablePosition;
        // TODO rotate, aim, cd
        if (target.getTicksSinceLastUpdate() > 50 || (target.isSeenBefore() && target.getBulletCount() == 0)) {
            ticksToShootablePosition = getTicksToNearestShootablePositionWithAim(me, target);
        } else {
            ticksToShootablePosition = Math.min(
                    getTicksToNearestShootablePositionWithAim(me, target),
                    getTicksToRunBySideToTheNearestShootablePosition(target, me)
            );
        }
        var ticksToRotate = WalkSimulation.getTicksToRotateWithBestEffortToAimTarget(me, bestPositionToShoot);

        if (DebugData.isEnabled) {
            DebugData.getInstance().getDefaultLayer().addText(String.format("run: %d (me: %d, en: %d), rot: %d", ticksToShootablePosition,
                            getTicksToNearestShootablePositionWithAim(me, target),
                            getTicksToRunBySideToTheNearestShootablePosition(target, me),
                            ticksToRotate),
                    me.getPosition().move(new Vector(1, 1)), 0.5
            );
        }

        return (int) MathUtils.max(
                ticksToRotate,
                ticksToSpawn,
                ticksToShootablePosition
        );
    }

    private int getTicksToNearestShootablePositionWithAim(Unit unit, Unit target) {
//DebugData.getInstance().getDefaultLayer().addLine(getNearestShootablePosition(unit, target).getStart(),
//        getNearestShootablePosition(unit, target).getEnd(), Colors.GRAY_TRANSPARENT);
        return WalkSimulation.getTicksToRunDistanceWithAim(unit, getNearestShootablePosition(unit, target).getProjection(unit.getPosition()));
    }

    private int getTicksToRunBySideToTheNearestShootablePosition(Unit unit, Unit target) {
//DebugData.getInstance().getDefaultLayer().addLine(getNearestShootablePosition(unit, target).getStart(),
//        getNearestShootablePosition(unit, target).getEnd(), Colors.GRAY_TRANSPARENT);
        var destination = getNearestShootablePosition(unit, target).getProjection(unit.getPosition());
        return (int) (unit.getPosition().getDistanceTo(destination) / Constants.UNIT_MAX_SIDE_SPEED_PER_TICK);
    }

    public static Line getNearestShootablePosition(Unit unit, Unit target) {
        var shootLine = new Line(target.getPosition(), unit.getPosition());

        var lines = World.getInstance().getNonShootThroughObstacles().values().stream()
                .filter(obstacle -> obstacle.getCircle().isIntersect(shootLine))
                .flatMap(obstacle -> obstacle.getCircle().getTangentLines(target.getPosition()).stream())
                .collect(Collectors.toList());

        if (lines.isEmpty()) {
            return shootLine;
        }

        double maxAngle = 0;
        Line maxLine = null;
        double minAngle = 1000;
        Line minLine = null;
        for (var line: lines) {
            var diff = shootLine.toVector().getDiffToVector(line.toVector());
            if (diff > maxAngle) {
                maxAngle = diff;
                maxLine = line;
            }
            if (diff < minAngle) {
                minAngle = diff;
                minLine = line;
            }
        }

        if (maxAngle < -minAngle) {
            return maxLine;
        } else {
            return minLine;
        }
    }

    public Position getBestPositionToShoot() {
        return bestPositionToShoot;
    }

    public boolean isShouldShoot() {
        return shouldShoot;
    }

    private boolean shouldShoot(Unit me, Position targetPosition, Unit enemy) {
        if (!canShootUnit(targetPosition, enemy)) {
            return false;
        }

        if (!me.canDoNewAction() || me.isCoolDown()) {
            return false;
        }

        if (isShootWillDamageEnemy(me, enemy)) {
            return true;
        }

        if (!getShootingSegmentOnStartNextTick().contains(targetPosition)) {
            return false;
        }

        return true;
    }

    private CircleSegment getShootingSegmentOnStartNextTick() {
        var aim = WalkSimulation.getAimOnNextTick(me.getAim(), me.getAimChangePerTick(), shouldStartAimingToEnemy);
        var direction = WalkSimulation.simulateRotateTickToDirection(me.getDirection(), bestLookDirection, aim, me.getAimRotationSpeed());
        return me.getShootingSegment().rotateToAngle(direction.getAngle());
    }

    public boolean canShootUnit(Position targetPosition, Unit enemy) {
        if (enemy.isPhantom() || !enemy.isSpawned()) {
            return false;
        }

        if (!me.canShoot(targetPosition, enemy)) {
            return false;
        }

        return true;
    }

    public Unit getTarget() {
        return target;
    }

    private Position getBestPositionToShoot(Unit me, Unit enemy) {
        if (enemy.isPhantom()) {
            return enemy.getPosition();
        }

        var dist = me.getDistanceTo(enemy) - me.getCircle().getRadius();
        var bulletSpeed = me.getWeaponOptional().map(Weapon::getSpeedPerTick).orElse(0.);

        var ticksToHit = (int) Math.ceil(dist / bulletSpeed);

        var trajectory = new Vector(enemy.getPosition(), me.getPosition()).normalizeToLength(100);
        var dodgeDirection1 = trajectory.rotate(Math.PI / 2);
        var dodgeDirection2 = trajectory.rotate(-Math.PI / 2);

        var pos1 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection1, ticksToHit);
        var pos2 = WalkSimulation.getMaxPositionIfWalkDirect(enemy, dodgeDirection2, ticksToHit);

        return new Line(pos1, pos2).getMiddlePoint();
    }
}
