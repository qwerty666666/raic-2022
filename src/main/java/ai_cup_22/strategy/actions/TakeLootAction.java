package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.actions.basic.PickupAction;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;

public class TakeLootAction implements Action {
    private final Loot loot;

    public TakeLootAction(Loot loot) {
        this.loot = loot;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        if (unit.isStayOnLoot(loot)) {
            new CompositeAction()
                    .add(new MoveToAction(loot.getPosition()))
                    .add(new PickupAction(loot))
                    .apply(unit, order);
        } else {
            // by default move to given loot
            if (unit.isSpawned()) {
                // for dodge bullet in best direction
                new LinearScoreContributor(loot.getPosition(), 50, 10, 100)
                        .contribute(unit.getPotentialField());

                new MoveToWithPathfindingAction(loot.getPosition()).apply(unit, order);
            } else {
                new MoveToAction(loot.getPosition()).apply(unit, order);
            }
        }
    }
}
