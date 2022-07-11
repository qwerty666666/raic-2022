package ai_cup_22.strategy.models;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.BehaviourTree;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.UnitPotentialField;
import java.util.Collections;
import java.util.List;

public class Unit {
    private Circle circle;
    private ai_cup_22.model.Unit unit;
    private Vector direction;
    private UnitPotentialField potentialField = new UnitPotentialField(this);
    private List<Position> currentPath;
    private BehaviourTree behaviourTree = new BehaviourTree(this);

    public void updateTick(ai_cup_22.model.Unit unit) {
        this.unit = unit;
        this.circle = new Circle(new Position(unit.getPosition()), World.getInstance().getConstants().getUnitRadius());
        this.direction = new Vector(unit.getDirection());
        this.potentialField.refresh();
        this.currentPath = Collections.emptyList();
    }

    public BehaviourTree getBehaviourTree() {
        return behaviourTree;
    }

    public List<Position> getCurrentPath() {
        return currentPath;
    }

    public Unit setCurrentPath(List<Position> currentPath) {
        this.currentPath = currentPath;
        return this;
    }

    public PotentialField getPotentialField() {
        return potentialField;
    }

    public int getId() {
        return unit.getId();
    }

    public boolean isMe() {
        return unit.getPlayerId() == World.getInstance().getMyId();
    }

    public Position getPosition() {
        return circle.getCenter();
    }

    public Vector getDirection() {
        return direction;
    }

    public Vector getSpeedVector() {
        return new Vector(unit.getVelocity()).increase(1. / World.getInstance().getConstants().getTicksPerSecond());
    }

    public double getSpeed() {
        return getSpeedVector().getLength();
    }

    public Circle getCircle() {
        return circle;
    }

    public int getTicksToFullAim() {
        return getWeapon().getAimTicks();
    }

    public double getMaxSpeedPerTick() {
        return World.getInstance().getConstants().getMaxUnitForwardSpeed() * World.getInstance().getTimePerTick();
    }

    public int getRemainingCoolDownTicks() {
        return Math.max(0, unit.getNextShotTick() - World.getInstance().getCurrentTick());
    }

    public boolean canSee(Position p) {
        return getViewSegment().contains(p);
    }

    public boolean canShoot(Unit enemy) {
        var line = new Line(getPosition(), enemy.getPosition());

        return World.getInstance().getNonShootThroughObstacles().stream()
                .noneMatch(obstacle -> obstacle.getCircle().isIntersect(line))
                &&
                World.getInstance().getAllUnits().values().stream()
                        .filter(u -> u.getId() != this.getId() && u.getId() != enemy.getId())
                        .noneMatch(u -> u.getCircle().isIntersect(line));
    }

    public boolean hasWeapon() {
        return getWeapon() != null;
    }

    public int getBulletCount() {
        return hasWeapon() ? unit.getAmmo()[unit.getWeapon()] : 0;
    }

    public int getMaxBulletCount() {
        return hasWeapon() ? getWeapon().getMaxBulletCount() : 0;
    }

    public Weapon getWeapon() {
        if (unit.getWeapon() == null) {
            return null;
        }
        return Weapon.get(unit.getWeapon());
    }

    public CircleSegment getShootingSegment() {
        return new CircleSegment(
                new Circle(getPosition(), World.getInstance().getConstants().getViewDistance()),
                direction.getAngle(),
                hasWeapon() ? Math.toRadians(getWeapon().getSpread()) : 0
        );
    }

    public CircleSegment getViewSegment() {
        var fieldOfView = Math.toRadians(World.getInstance().getConstants().getFieldOfView());
        var aimFieldOfView = hasWeapon() ? Math.toRadians(getWeapon().getAimFieldOfView()) : fieldOfView;

        return new CircleSegment(
                new Circle(getPosition(), World.getInstance().getConstants().getViewDistance()),
                direction.getAngle(),
                fieldOfView - (fieldOfView - aimFieldOfView) * getAim()
        );
    }

    public double getAim() {
        return unit.getAim();
    }

    public double getDistanceTo(Unit u) {
        return u.getPosition().getDistanceTo(this.getPosition());
    }

    public boolean canTakeLoot(Loot loot) {
        return circle.contains(loot.getPosition());
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Unit u && u.getId() == getId();
    }
}
