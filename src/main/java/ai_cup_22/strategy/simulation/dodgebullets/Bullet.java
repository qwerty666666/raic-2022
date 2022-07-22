package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Weapon;

public class Bullet {
    private ai_cup_22.strategy.models.Bullet bullet;
    private Position position;
    private int ttl;

    public Bullet(ai_cup_22.strategy.models.Bullet bullet) {
        this.bullet = bullet;
        this.position = bullet.getPosition();
        this.ttl = bullet.getRealRemainingLifetimeTicks();
    }

    public Position getPosition() {
        return position;
    }

    public Bullet setPosition(Position position) {
        this.position = position;
        return this;
    }

    public void simulateTick() {
        this.position = this.position.move(getVelocity());
        ttl--;
    }

    public boolean isDead() {
        return ttl <= 0;
    }

    public Vector getVelocity() {
        return bullet.getVelocity();
    }

    public double getDmg() {
        return bullet.getDmg();
    }

    public Line getTrajectory() {
        return bullet.getRealTrajectory();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Bullet && ((Bullet)o).bullet.equals(bullet);
    }

    @Override
    public int hashCode() {
        return bullet.hashCode();
    }
}
