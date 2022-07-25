package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.Path;

public class MoveByPathAction implements Action {
    private final Path path;
    private final boolean setLookPosition;

    public MoveByPathAction(Path path, boolean setLookPosition) {
        this.path = path;
        this.setLookPosition = setLookPosition;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (this.path == null || this.path.getPathPositions().size() < 2) {
            return;
        }

        var path = this.path.getPathPositions();
        unit.setCurrentPath(path);

        var nextPosition = path.get(1);

        var action = new CompositeAction()
                .add(new MoveToAction(nextPosition));
        if (setLookPosition) {
            action.add(new LookToAction(nextPosition));
        }

        action.apply(unit, order);
    }
}
