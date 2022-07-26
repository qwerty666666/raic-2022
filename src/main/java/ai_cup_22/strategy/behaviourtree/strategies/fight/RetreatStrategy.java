package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.LookBackAction;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Unit;
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
        unit.getPotentialField().reset();
        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        return new CompositeAction()
                .add(new MoveByPotentialFieldAction())
                .add(new LookBackAction());
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(
                        "Enemy: " + enemy,
                        enemy.getPosition(),
                        PotentialField.MIN_VALUE,
                        0,
                        Constants.PF_RETREAT_ENEMY_DIST
                ))
                .collect(Collectors.toList());

        var phantomEnemiesScoreContributors = World.getInstance().getPhantomEnemies().values().stream()
                .map(enemy -> new LinearScoreContributor(
                        "Phantom Enemy: " + enemy.getPosition(),
                        enemy.getPosition(),
                        PotentialField.MIN_VALUE,
                        0,
                        Constants.PF_RETREAT_ENEMY_DIST
                ))
                .collect(Collectors.toList());

        return new SumCompositeScoreContributor("retreat")
                .add(new ZoneScoreContributor(unit.getPotentialField()))
                .add(enemyScoreContributors)
                .add(phantomEnemiesScoreContributors);
    }

    private Optional<Unit> getNearestEnemy() {
        if (!World.getInstance().getEnemyUnits().isEmpty()) {
            return World.getInstance().getEnemyUnits().values().stream()
                    .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)));
        } else {
            return World.getInstance().getPhantomEnemies().values().stream()
                    .min(Comparator.comparingDouble(enemy -> enemy.getDistanceTo(unit)));
        }
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
