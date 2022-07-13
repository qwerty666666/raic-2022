package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.pathfinding.SmoothingPathFinder;

public class MoveByPathAction implements Action {
    private final Path path;

    public MoveByPathAction(Path path) {
        this.path = path;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (this.path == null || this.path.getPathPositions().size() < 2) {
            return;
        }

        var path = this.path.getPathPositions();
        unit.setCurrentPath(path);

        var nextPosition = path.get(1);

        new CompositeAction()
                .add(new MoveToAction(nextPosition))
                .add(new LookToAction(nextPosition))
                .apply(unit, order);
    }
}
