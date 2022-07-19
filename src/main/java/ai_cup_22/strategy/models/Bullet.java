package ai_cup_22.strategy.models;

import ai_cup_22.model.Projectile;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import java.util.Comparator;

public class Bullet {
    private Position position;
    private Vector velocity;
    private int id;
    private boolean isEnemy;
    private double lifetime;
    private boolean isSimulated;
    private int unitId;
    private Position endTrajectoryPosition;
    private int type;

    public Bullet(Projectile projectile) {
        this.velocity = new Vector(projectile.getVelocity()).increase(World.getInstance().getTimePerTick());
        this.id = projectile.getId();
        this.isEnemy = projectile.getShooterPlayerId() != World.getInstance().getMyId();
        this.unitId = projectile.getShooterId();
        this.type = projectile.getWeaponTypeIndex();

        updateTick(projectile);
    }

    public Bullet updateTick(Projectile projectile) {
        position = new Position(projectile.getPosition());
        lifetime = projectile.getLifeTime();

        isSimulated = false;

        if (endTrajectoryPosition == null) {
            var trajectory = getTrajectoryWithoutObstacles();
            endTrajectoryPosition = World.getInstance().getNonShootThroughObstacles().stream()
                    .filter(obstacle -> obstacle.getCircle().isIntersect(trajectory))
                    .flatMap(obstacle -> trajectory.getIntersectionPoints(obstacle.getCircle()).stream())
                    .min(Comparator.comparingDouble(intersectPoint -> intersectPoint.getDistanceTo(position)))
                    .orElse(trajectory.getEnd());
        }

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

    public Vector getVelocity() {
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

    public Line getTrajectoryWithoutObstacles() {
        return new Line(position, position.move(velocity.increase(getRemainingLifetimeTicks())));
    }

    public Line getTrajectory() {
        return new Line(position, endTrajectoryPosition);
    }

    public int getPassedTicks() {
        var maxTicks = World.getInstance().getConstants().getWeapons()[type].getProjectileLifeTime() *
                World.getInstance().getConstants().getTicksPerSecond();
        return (int) maxTicks - getRemainingLifetimeTicks();
    }

    public int getStartTick() {
        return World.getInstance().getCurrentTick() - getPassedTicks();
    }

    public Line getTrajectoryForFullLifetime() {
        return new Line(position.move(velocity.increase(getPassedTicks()).reverse()), endTrajectoryPosition);
    }

    public int getWeaponId() {
        return type;
    }

    public int getUnitId() {
        return unitId;
    }

    public boolean isWand() {
        return type == Weapon.WAND_ID;
    }

    public boolean isStaff() {
        return type == Weapon.STAFF_ID;
    }

    public boolean isBow() {
        return type == Weapon.BOW_ID;
    }

    @Override
    public String toString() {
        return position.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bullet bullet = (Bullet) o;

        return id == bullet.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
