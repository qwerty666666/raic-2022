package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;

public class Unit {
    private Circle circle;
    private Vector velocity;
    private Vector direction;

    public Unit(Circle circle, Vector velocity, Vector direction) {
        this.circle = circle;
        this.velocity = velocity;
        this.direction = direction;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public Vector getDirection() {
        return direction;
    }

    public Position getPosition() {
        return circle.getCenter();
    }
}
