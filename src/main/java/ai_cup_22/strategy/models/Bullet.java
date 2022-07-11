package ai_cup_22.strategy.models;

import ai_cup_22.model.Projectile;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;

public class Bullet {
    private Position position;
    private Vector velocity;
    private int id;
    private boolean isEnemy;
    private double lifetime;
    private boolean isSimulated;

    public Bullet(Projectile projectile) {
        this.velocity = new Vector(projectile.getVelocity()).increase(World.getInstance().getTimePerTick());
        this.id = projectile.getId();
        this.isEnemy = projectile.getShooterPlayerId() != World.getInstance().getMyId();

        updateTick(projectile);
    }

    public Bullet updateTick(Projectile projectile) {
        position = new Position(projectile.getPosition());
        lifetime = projectile.getLifeTime();

        isSimulated = false;

        return this;
    }

    public void simulateTick() {
        position = position.move(velocity);
        lifetime -= World.getInstance().getTimePerTick();

        isSimulated = true;
    }

    public boolean isSimulated() {
        return isSimulated;
    }

    public int getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public Vector getDirection() {
        return velocity;
    }

    public double getSpeed() {
        return velocity.getLength();
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public int getRemainingLifetimeTicks() {
        return (int) Math.ceil(lifetime / World.getInstance().getTimePerTick());
    }

    public Line getLastTickTrajectory() {
        return new Line(position.move(velocity.reverse()), position);
    }

    public Line getTrajectory() {
        return new Line(position, position.move(velocity.increase(getRemainingLifetimeTicks())));
    }

    @Override
    public String toString() {
        return position.toString();
    }
}
