package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.behaviourtree.strategies.fight.FightStrategy;
import ai_cup_22.strategy.distributions.CumulativeExponentialDistributor;
import ai_cup_22.strategy.distributions.LinearDistributor;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Weapon;
import ai_cup_22.strategy.models.WeaponLoot;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootWeaponStrategy extends BaseLootStrategy {
    private final Unit unit;
    private final double maxLootDist;
    private Optional<Loot> bestLoot;
    private Map<Loot, Double> weaponScores = new HashMap<>();

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
            if (!isSmallZone()) {
                return MIN_ORDER;
            }
            if (unit.getBulletCount() >= 5) {
                return MIN_ORDER;
            }
        }

        return getBestLoot()
                .map(loot -> {
                    var score = this.weaponScores.get(loot);
                    if (!unit.hasWeapon() || score > getScoreForWeapon(unit.getWeapon().getId(), 0)) {
                        return score;
                    }
                    return MIN_ORDER;
                })
                .orElse(MIN_ORDER);
    }

    public boolean shouldSwapWeapon() {
        if (!unit.hasWeapon()) {
            return true;
        }
        return getOrder() != MIN_ORDER;
    }

    @Override
    protected List<Loot> getSuitableLoots() {
        return World.getInstance().getWeaponLoots().values().stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < maxLootDist)
                .collect(Collectors.toList());
    }

    @Override
    protected Optional<Loot> getBestLoot() {
        if (bestLoot == null) {
            bestLoot = findBestLoot();
        }
        return bestLoot;
    }

    private Optional<Loot> findBestLoot() {
        var loots = getSuitableLoots().stream()
                .filter(loot -> !World.getInstance().getGlobalStrategy().isLootTakenByOtherUnit(loot, unit))
                .collect(Collectors.toList());

        if (loots.isEmpty()) {
            return Optional.empty();
        }

        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        var pathFinder = new AStarPathFinder(unit.getPotentialField());

        var paths = loots.stream()
                .collect(Collectors.toMap(
                        loot -> loot,
                        loot -> pathFinder.findPath(unit.getPosition(), loot.getPosition())
                ));

        this.weaponScores = paths.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> getScoreToTakeWeapon((WeaponLoot) e.getKey(), e.getValue())));

        // search by min sum of treats on the path
        // and min by distance if there is no treat on the path
        var loot = weaponScores.entrySet().stream()
                .max(Comparator.comparingDouble(e -> weaponScores.get(e.getKey())))
                .map(Entry::getKey)
                .orElseThrow();

        return Optional.ofNullable(loot);
    }

    private boolean isSmallZone() {
        return World.getInstance().getZone().getRadius() < 130;
    }

    private double getScoreToTakeWeapon(WeaponLoot loot, Path path) {
        var weaponId = loot.getWeaponId();
        return getScoreForWeapon(weaponId, path.getDistance());
    }

    private double getScoreForWeapon(int weaponId, double dist) {
        if (weaponId != Weapon.BOW_ID) {
            return MIN_ORDER;
        }

        var lootCount = World.getInstance().getAmmoLoots(weaponId).stream()
                .mapToInt(AmmoLoot::getCount)
                .sum();
        var myBulletCount = unit.getBulletCount(weaponId);

        if (isSmallZone() && lootCount + myBulletCount == 0) {
            return 0;
        }

        var maxBulletCount = Weapon.get(weaponId).getMaxBulletCount();

        var countMul = 1.;
        if (isSmallZone()) {
            countMul = new LinearDistributor(maxBulletCount / 5., maxBulletCount, 0, 1)
                    .get(lootCount + myBulletCount);
        }

        var distMul = new CumulativeExponentialDistributor(0, maxLootDist, 0, 1)
                .get(dist);
        var priority = Weapon.getPriority(weaponId);

        return priority * countMul * distMul;
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getAllEnemyUnits().stream()
                .filter(enemy -> enemy.isSpawned() && enemy.getBulletCount() > 0)
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 15))
                .collect(Collectors.toList());

        return new SumCompositeScoreContributor("loot ammo force")
                .add(new ZoneScoreContributor(unit.getPotentialField()))
                .add(enemyScoreContributors);
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
