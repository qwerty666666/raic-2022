package ai_cup_22.strategy.models;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.potentialfield.PotentialField;

public class Unit {
    private Circle circle;
    private ai_cup_22.model.Unit unit;
    private Vector direction;
    private PotentialField potentialField;

    public void updateTick(ai_cup_22.model.Unit unit) {
        this.unit = unit;
        this.circle = new Circle(new Position(unit.getPosition()), World.getInstance().getConstants().getUnitRadius());
        this.direction = new Vector(unit.getDirection());
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

    public boolean canShoot(Unit enemy) {
        var line = new Line(getPosition(), enemy.getPosition());

        return World.getInstance().getNonShootThroughObstacles().stream()
                .noneMatch(obstacle -> obstacle.getCircle().intersect(line))
                &&
                World.getInstance().getAllUnits().values().stream()
                        .filter(u -> u.getId() != this.getId() && u.getId() != enemy.getId())
                        .noneMatch(u -> u.getCircle().intersect(line));
    }

    public boolean hasWeapon() {
        return getWeapon() != null;
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
                hasWeapon() ? getWeapon().getSpread() / 180 * Math.PI : 0
        );
    }

    public double getDistanceTo(Unit u) {
        return u.getPosition().getDistanceTo(this.getPosition());
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
