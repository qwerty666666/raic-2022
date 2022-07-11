package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;
import java.util.List;

public class DodgeBulletsAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var bullets = World.getInstance().getBullets().values().stream()
                .filter(bullet -> isBulletTreatsUnit(unit, bullet))
                .toList();

        if (bullets.isEmpty()) {
            return;
        }

        var nearestBullet = getNearestBullet(bullets, unit);
        var positionToDodge = getPositionToDodgeBullet(unit, nearestBullet);

        new MoveToAction(positionToDodge).apply(unit, order);
    }

    private Position getPositionToDodgeBullet(Unit unit, Bullet bullet) {
        var midPosition = bullet.getTrajectory().getProjection(unit.getPosition());
        var vector = bullet.getTrajectory().toVector();

        var pos1 = midPosition.move(
                vector
                        .rotate(Math.PI / 2)
                        .normalizeToLength(unit.getCircle().getRadius() + unit.getMaxSpeedPerTick() + 0.1)
        );
        var pos2 = unit.getPosition().move(
                vector
                        .rotate(-Math.PI / 2)
                        .normalizeToLength(unit.getCircle().getRadius() + unit.getMaxSpeedPerTick() + 0.1)
        );

        return unit.getPosition().getSquareDistanceTo(pos1) < unit.getPosition().getSquareDistanceTo(pos2) ? pos1 : pos2;
    }

    private Bullet getNearestBullet(List<Bullet> bullets, Unit unit) {
        return bullets.stream()
                .min(Comparator.comparingDouble(b -> b.getPosition().getDistanceTo(unit.getPosition())))
                .orElse(null);
    }

    private boolean isBulletTreatsUnit(Unit unit, Bullet bullet) {
        return unit.getCircle().enlarge(0.3).isIntersect(bullet.getTrajectory());
    }
}
