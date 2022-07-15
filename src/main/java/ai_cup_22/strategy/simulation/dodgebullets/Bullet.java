package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;

public class Bullet {
    private Position position;
    private Vector velocity;
    private int ttl;

    public Bullet(Position position, Vector velocity, int ttl) {
        this.position = position;
        this.velocity = velocity;
        this.ttl = ttl;
    }

    public Position getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public int getTtl() {
        return ttl;
    }
}
