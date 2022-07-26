package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.distributions.CumulativeExponentialDistributor;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import java.util.List;
import java.util.stream.Collectors;

public class LootWeaponStrategy extends BaseLootStrategy {
    private final Unit unit;
    private final double maxLootDist;

    public LootWeaponStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy) {
        this(unit, exploreStrategy, fightStrategy, Constants.MAX_LOOT_STRATEGY_DIST);
    }

    public LootWeaponStrategy(Unit unit, ExploreStrategy exploreStrategy, FightStrategy fightStrategy, double maxLootDist) {
        super(unit, exploreStrategy, fightStrategy);
        this.unit = unit;
        this.maxLootDist = maxLootDist;
    }

    @Override
    public double getOrder() {
        if (unit.hasWeapon() && unit.getWeapon().isBow()) {
            return MIN_ORDER;
        }

        return getBestLoot()
                .map(loot -> {
                    var dist = unit.getPosition().getDistanceTo(loot.getPosition());

                    return new CumulativeExponentialDistributor(0, maxLootDist, 0, 1)
                            .get(dist);
                })
                .orElse(0.);
    }

    @Override
    protected List<Loot> getSuitableLoots() {
        return World.getInstance().getWeaponLoots(Weapon.BOW_ID).stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
