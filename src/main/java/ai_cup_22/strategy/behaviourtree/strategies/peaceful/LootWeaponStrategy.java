package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.NullStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.composite.FirstMatchCompositeStrategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.distributions.FirstMatchDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
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
                .filter(this::canTakeLootOnlyAfterDisabledTime)
                .map(loot -> {
                    var dist = unit.getPosition().getDistanceTo(loot.getPosition());

                    return new LinearDistributor(0, maxLootDist, 1, 0)
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
