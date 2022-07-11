package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.TakeLootAction;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.MaxCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.PathDistanceScoreContributor;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.SumCompositeScoreContributor;
import ai_cup_22.strategy.potentialfield.ZoneScoreContributor;
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
        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        // TODO it is better to hide instead
        if (World.getInstance().getAmmoLoots(unit.getWeapon().getId()).isEmpty()) {
            return exploreStrategy.getAction();
        }

        var loot = getNearestAmmoLoot();
        if (unit.canTakeLoot(loot)) {
            return new TakeLootAction(loot);
        }

        return new MoveByPotentialFieldAction();
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 30))
                .toList();

        var lootContributors = World.getInstance().getAmmoLoots(unit.getWeapon().getId()).stream()
//                .map(loot -> new LinearScoreContributor(loot.getPosition(), PotentialField.MAX_VALUE, 0, 100))
                .map(loot -> new PathDistanceScoreContributor(loot.getPosition(), unit.getPotentialField(), 100, 0, 100))
                .toList();

        return new SumCompositeScoreContributor()
                .add(new ZoneScoreContributor())
                .add(enemyScoreContributors)
                .add(new MaxCompositeScoreContributor()
                        .add(lootContributors)
                );
    }

    private AmmoLoot getNearestAmmoLoot() {
        return World.getInstance().getAmmoLoots().values().stream()
                .filter(loot -> loot.getWeaponId() == unit.getWeapon().getId())
                .min(Comparator.comparingDouble(loot -> unit.getPosition().getDistanceTo(loot.getPosition())))
                .orElse(null);
    }
}
