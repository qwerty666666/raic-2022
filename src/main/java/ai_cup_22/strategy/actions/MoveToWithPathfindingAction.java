package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.PathFinder;
import ai_cup_22.strategy.pathfinding.SmoothingPathFinder;

public class MoveToWithPathfindingAction implements Action {
    private final Position target;
    private final PathFinder pathFinder = new SmoothingPathFinder(new AStarPathFinder());

    public MoveToWithPathfindingAction(Position target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.getPosition().getDistanceTo(target) < 2) {
            new MoveToAction(target).apply(unit, order);
            return;
        }

        var path = pathFinder.findPath(unit.getPotentialField(), unit.getPosition(), target).getPathPositions();

        unit.setCurrentPath(path);

        if (path != null && path.size() > 1) {
            var nextPosition = path.get(1);

            var velocity = new Vector(unit.getPosition(), nextPosition);
            order.setTargetVelocity(velocity.normalizeToLength(10).toVec2());

            // look to target by default
            new LookToAction(nextPosition).apply(unit, order);
        }
    }
}
