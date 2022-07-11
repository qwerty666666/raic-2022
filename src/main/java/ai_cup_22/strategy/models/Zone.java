package ai_cup_22.strategy.models;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;

public class Zone {
    private Circle circle;
    private Circle newCircle;

    public void updateTick(ai_cup_22.model.Zone zone) {
        circle = new Circle(new Position(zone.getCurrentCenter()), zone.getCurrentRadius());
        newCircle = new Circle(new Position(zone.getNextCenter()), zone.getNextRadius());
    }

    public Circle getCircle() {
        return circle;
    }

    public boolean contains(Position p) {
        return circle.contains(p);
    }

    public double getRadius() {
        return circle.getRadius();
    }

    public Position getCenter() {
        return circle.getCenter();
    }

    public Position getNewCenter() {
        return newCircle.getCenter();
    }

    public double getNewRadius() {
        return newCircle.getRadius();
    }

    public int getTicksToNewZone() {
        return (int) Math.ceil(getCenter().getDistanceTo(getNewCenter()) / getSpeedPerTick());
    }

    public double getSpeedPerTick() {
        return World.getInstance().getConstants().getZoneSpeed() * World.getInstance().getTimePerTick();
    }
}
