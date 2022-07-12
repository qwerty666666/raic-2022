package ai_cup_22.strategy.actions.basic;

import ai_cup_22.model.Action;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.models.Unit;

public class OrderAction {
    protected int startTick;
    protected int finishTick;
    protected int targetId;

    public OrderAction() {
        this.startTick = this.finishTick = World.getInstance().getCurrentTick();
    }

    public boolean isFinished() {
        return finishTick <= World.getInstance().getCurrentTick();
    }

    public int getStartTick() {
        return startTick;
    }

    public int getFinishTick() {
        return finishTick;
    }

    public int getTargetId() {
        return targetId;
    }

    public void updateTick(Unit unit, Action action) {
        if (this.targetId != 0) {
            World.getInstance().getAmmoLoots().remove(targetId);
            World.getInstance().getShieldLoots().remove(targetId);
            World.getInstance().getWeaponLoots().remove(targetId);
        }

        this.finishTick = action == null ? World.getInstance().getCurrentTick() : action.getFinishTick();
    }
}
