package ai_cup_22.strategy.actions;

import ai_cup_22.model.UnitOrder;
import ai_cup_22.strategy.actions.basic.MoveToAction;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;

public class MoveByPotentialFieldAction implements Action {
    private final GetScoreValueStrategy getScoreValueStrategy;

    public MoveByPotentialFieldAction() {
        this(GetScoreValueStrategy.WHOLE_SCORE);
    }

    public MoveByPotentialFieldAction(GetScoreValueStrategy getScoreValueStrategy) {
        this.getScoreValueStrategy = getScoreValueStrategy;
    }

    @Override
    public void apply(Unit unit, UnitOrder order) {
        var target = getFieldWithMaxScore(unit.getPosition(), unit.getPotentialField()).getPosition();

        new MoveToAction(target).apply(unit, order);
    }

    private Score getFieldWithMaxScore(Position from, PotentialField potentialField) {
        return potentialField.getScoresAround(from).stream()
                .filter(score -> !score.isUnreachable())
                .max(Comparator.comparingDouble(getScoreValueStrategy.getGetScoreValueFunc()))
                .orElseThrow();
    }

    public enum GetScoreValueStrategy {
        WHOLE_SCORE (Score::getScore),
        NON_STATIC_SCORE (Score::getNonStaticScore);

        private final ToDoubleFunction<Score> getScoreValueFunc;

        GetScoreValueStrategy(ToDoubleFunction<Score> getScoreValueFunc) {
            this.getScoreValueFunc = getScoreValueFunc;
        }

        public ToDoubleFunction<Score> getGetScoreValueFunc() {
            return getScoreValueFunc;
        }
    }

}
