package ai_cup_22.strategy.models;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;

public class Zone {
    private Circle circle;

    public void updateTick(ai_cup_22.model.Zone zone) {
        circle = new Circle(new Position(zone.getCurrentCenter()), zone.getCurrentRadius());
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
}
