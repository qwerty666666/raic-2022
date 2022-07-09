package ai_cup_22.strategy.models;

import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;

public class Obstacle {
    private final Circle circle;
    private final int id;
    private final boolean canSeeThrough;
    private final boolean canShootThrough;

    public Obstacle(ai_cup_22.model.Obstacle obstacle) {
        this.id = obstacle.getId();
        this.circle = new Circle(new Position(obstacle.getPosition()), obstacle.getRadius());
        this.canSeeThrough = obstacle.isCanSeeThrough();
        this.canShootThrough = obstacle.isCanShootThrough();
    }

    public Circle getCircle() {
        return circle;
    }

    public int getId() {
        return id;
    }

    public boolean isCanSeeThrough() {
        return canSeeThrough;
    }

    public boolean isCanShootThrough() {
        return canShootThrough;
    }


    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Obstacle o && o.getId() == getId();
    }
}
