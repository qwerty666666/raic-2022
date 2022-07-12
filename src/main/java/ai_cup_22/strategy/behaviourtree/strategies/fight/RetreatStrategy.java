package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.DodgeBulletsAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction.GetScoreValueStrategy;
import ai_cup_22.strategy.actions.basic.AimAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.Comparator;
import java.util.Optional;

public class RetreatStrategy implements Strategy {
    private final Unit unit;

    public RetreatStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        return MAX_ORDER;
    }

    @Override
    public Action getAction() {
        var lookPoint = getNearestEnemy()
                .map(Unit::getPosition)
                .orElseGet(() -> unit.getPosition().move(unit.getDirection()));

        if (isThereAreThreatenBullets()) {
            return new CompositeAction()
                    .add(new LookToAction(lookPoint))
                    .add(new DodgeBulletsAction());
        } else {
            getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

            return new CompositeAction()
                    .add(new MoveByPotentialFieldAction(GetScoreValueStrategy.NON_STATIC_SCORE))
                    .add(new LookToAction(lookPoint));
        }
    }

    private boolean isThereAreThreatenBullets() {
        return World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .anyMatch(bullet -> {
                    var safeDist = unit.getMaxForwardSpeedPerTick() + unit.getCircle().getRadius();
                    var distToUnit = bullet.getTrajectory().getDistanceTo(unit.getPosition());

                    return distToUnit <= safeDist;
                } );
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 40))
                .toList();

        return new SumCompositeScoreContributor()
                .add(new ZoneScoreContributor())
                .add(enemyScoreContributors);
    }

    private Optional<Unit> getNearestEnemy() {
        return World.getInstance().getEnemyUnits().values().stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)));
    }
}
