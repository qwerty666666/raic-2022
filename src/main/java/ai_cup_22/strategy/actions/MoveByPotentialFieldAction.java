package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.Comparator;

public class MoveByPotentialFieldAction implements Action {
    @Override
    public void apply(Unit unit, UnitOrder order) {
        var target = getFieldWithMaxScore(unit.getPosition(), unit.getPotentialField()).getPosition();

        new MoveToAction(target).apply(unit, order);
    }

    private Score getFieldWithMaxScore(Position from, PotentialField potentialField) {
        return potentialField.getScores().stream()
                .sorted(Comparator.comparingDouble(score -> score.getPosition().getDistanceTo(from)))
                .limit(4)
                .max(Comparator.comparingDouble(Score::getScore))
                .orElse(null);
    }
}
