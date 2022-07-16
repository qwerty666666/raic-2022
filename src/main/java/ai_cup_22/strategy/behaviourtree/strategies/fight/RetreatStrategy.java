package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveByPathAction;
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

        getPotentialFieldScoreContributor().contribute(unit.getPotentialField());

        var pathFinder = DijkstraPathFinder.minThreatPathFinder(unit.getPotentialField());
        var path = pathFinder.findPath(unit.getPosition(), getBestPointToRetreat());

        return new CompositeAction()
                .add(new MoveByPathAction(path))
                .add(new LookToAction(lookPoint));
    }

    private Position getBestPointToRetreat() {
        return unit.getPotentialField().getGraph().getNodes().values().stream()
                .min((node1, node2) -> {
                    // find node with positive score value
                    // if there are no positive score value, then take node with min
                    if (node1.getScoreValue() >= 0 && node2.getScoreValue() < 0) {
                        return -1;
                    }

                    if (node2.getScoreValue() >= 0 && node1.getScoreValue() < 0) {
                        return 1;
                    }

                    if (node1.getScoreValue() < 0 && node2.getScoreValue() < 0) {
                        // cmp avg threat per step on the path
                        return Double.compare(-node1.getDist() / node1.getSteps(), -node2.getDist() / node2.getSteps());
                    }

                    // node1.score > 0 && node2.score > 0
                    return Double.compare(
                            node1.getPosition().getSquareDistanceTo(unit.getPosition()),
                            node2.getPosition().getSquareDistanceTo(unit.getPosition())
                    );
                })
                .orElseThrow()
                .getPosition();
    }

    private boolean isThereAreThreatenBullets() {
        return World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .anyMatch(bullet -> {
                    var safeDist = unit.getMaxForwardSpeedPerTick() + unit.getCircle().getRadius();
                    var distToUnit = bullet.getFullLifetimeTrajectory().getDistanceTo(unit.getPosition());

                    return distToUnit <= safeDist;
                } );
    }

    private ScoreContributor getPotentialFieldScoreContributor() {
        var enemyScoreContributors = World.getInstance().getEnemyUnits().values().stream()
                .map(enemy -> new LinearScoreContributor(enemy.getPosition(), PotentialField.MIN_VALUE, 0, 30))
                .collect(Collectors.toList());

        return new SumCompositeScoreContributor()
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
