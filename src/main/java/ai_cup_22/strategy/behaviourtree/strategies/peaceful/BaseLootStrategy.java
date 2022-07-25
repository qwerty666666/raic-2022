package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.LookBackAction;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class BaseLootStrategy implements Strategy {
    protected final Unit unit;
    protected final ExploreStrategy exploreStrategy;
    protected final FightStrategy fightStrategy;

    protected BaseLootStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
        this.unit = unit;
        this.exploreStrategy = exploreStrategy;
        this.fightStrategy = fightStrategy;
    }

    @Override
    public Action getAction() {
        return getBestLoot()
                .map(loot -> {
                    World.getInstance().getGlobalStrategy().markLootAsTaken(loot, unit);

                    return (Action) new CompositeAction()
                                    .add(new TakeLootAction(loot))
                                    .add(new LookBackAction());
                })
                .orElse(exploreStrategy.getAction());
    }

    protected boolean canTakeLootOnlyAfterDisabledTime(Loot loot) {
        return loot.getPosition().getDistanceTo(unit.getPosition()) >=
                unit.getTicksToNewActionBeAvailable() * unit.getMaxForwardSpeedPerTick();
    }

    protected abstract List<Loot> getSuitableLoots();

    protected Optional<Loot> getBestLoot() {
        return getSuitableLoots().stream()
                .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + getOrder() + ") \n";
    }
}
