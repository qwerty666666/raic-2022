package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;

public class TakeAmmoStrategy implements Strategy {
    private final Unit unit;
    private final ExploreStrategy exploreStrategy;

    public TakeAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.exploreStrategy = exploreStrategy;
    }

    @Override
    public Action getAction() {
        var loot = getNearestAmmoLoot();

        if (loot != null) {
            return new TakeLootAction(loot);
        }

        return exploreStrategy.getAction();
    }

    private AmmoLoot getNearestAmmoLoot() {
        return World.getInstance().getAmmoLoots().values().stream()
                .filter(loot -> loot.getWeaponId() == unit.getWeapon().getId())
                .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())))
                .orElse(null);
    }
}
