package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.models.Unit;
import java.util.List;

public class UnitPotentialField implements PotentialField {
    public static final double FIELD_RADIUS = 30;
    private List<Score> scores;
    private Unit unit;

    public UnitPotentialField(Unit unit) {
        this.unit = unit;
    }

    public void refresh() {
        scores = World.getInstance().getStaticPotentialField().getScoresInCircle(new Circle(unit.getPosition(), FIELD_RADIUS));
        scores.forEach(Score::reset);
    }

    @Override
    public List<Score> getScores() {
        return scores;
    }
}
