package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;

public class MoveToWithPathfindingAction implements Action {
    private final Position target;

    public MoveToWithPathfindingAction(Position target) {
        this.target = target;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var pathFinder = new AStarPathFinder(unit.getPotentialField());
        var path = pathFinder.findPath(unit.getPosition(), target)
                    .smooth();

        new MoveByPathAction(path).apply(unit, order);
    }
}
