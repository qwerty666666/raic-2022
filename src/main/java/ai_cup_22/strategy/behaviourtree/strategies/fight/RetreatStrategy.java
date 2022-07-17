package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveByPathAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.DijkstraPathFinder;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.ZoneScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.SumCompositeScoreContributor;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

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

        unit.getPotentialField().reset();
        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        return new CompositeAction()
                .add(new MoveByPotentialFieldAction())
                .add(new LookToAction(lookPoint));
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 30))
                .collect(Collectors.toList());

        return new SumCompositeScoreContributor("retreat")
                .add(new ZoneScoreContributor())
                .add(enemyScoreContributors);
    }

    private Optional<Unit> getNearestEnemy() {
        return World.getInstance().getEnemyUnits().values().stream()
                .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)));
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
