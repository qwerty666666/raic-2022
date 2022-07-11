package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import ai_cup_22.strategy.pathfinding.Path;
import ai_cup_22.strategy.potentialfield.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.SumCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.ZoneScoreContributor;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class TakeAmmoStrategy implements Strategy {
    public static final double MAX_AMMO_DIST = 100;

    private final Unit unit;
    private final ExploreStrategy exploreStrategy;

    public TakeAmmoStrategy(Unit unit, ExploreStrategy exploreStrategy) {
        this.unit = unit;
        this.exploreStrategy = exploreStrategy;
    }

    @Override
    public Action getAction() {
        // TODO it is better to hide instead
        if (getSuitableAmmoLoots().isEmpty()) {
            return exploreStrategy.getAction();
        }

        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        return new TakeLootAction(getBestAmmo());
    }

    private Loot getBestAmmo() {
        var pathFinder = new AStarPathFinder(unit.getPotentialField());

        var paths = getSuitableAmmoLoots().stream()
                .collect(Collectors.toMap(
                        loot -> loot,
                        loot -> pathFinder.findPath(unit.getPosition(), loot.getPosition())
                ));

        // search by min sum of treats on the path
        // and min by distance if there is no treat on the path
        return paths.entrySet().stream()
                .min(
                        Comparator.comparingDouble(
                                (Entry<AmmoLoot, Path> e) -> e.getValue().getScores().stream()
                                        .filter(score -> score.getNonStaticScore() < 0)
                                        .mapToDouble(Score::getNonStaticScore)
                                        .sum()
                        )
                        .reversed()
                        .thenComparingDouble((Entry<AmmoLoot, Path> e) -> e.getValue().getDistance())
                )
                .orElseThrow()
                .getKey();
    }

    private List<AmmoLoot> getSuitableAmmoLoots() {
        return World.getInstance().getAmmoLoots(unit.getWeapon().getId()).stream()
                .filter(loot -> loot.getPosition().getDistanceTo(unit.getPosition()) < MAX_AMMO_DIST)
                .toList();
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 15))
                .toList();

        return new SumCompositeScoreContributor()
                .add(new ZoneScoreContributor())
                .add(enemyScoreContributors);
    }

    private AmmoLoot getNearestAmmoLoot() {
        return World.getInstance().getAmmoLoots().values().stream()
                .filter(loot -> loot.getWeaponId() == unit.getWeapon().getId())
                .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())))
                .orElse(null);
    }
}
