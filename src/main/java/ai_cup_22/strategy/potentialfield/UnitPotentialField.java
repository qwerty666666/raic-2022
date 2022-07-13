package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.List;

public class UnitPotentialField implements PotentialField {
    public static final double FIELD_RADIUS = 30;
    private List<Score> scores;
    private Unit unit;
    private Graph graph;

    public UnitPotentialField(Unit unit) {
        this.unit = unit;
    }

    public void refresh() {
        scores = World.getInstance().getStaticPotentialField().getScoresInCircle(new Circle(unit.getPosition(), FIELD_RADIUS));
        scores.forEach(Score::reset);
        graph = null;
    }

    @Override
    public List<Score> getScores() {
        return scores;
    }

    @Override
    public Graph getGraph() {
        if (graph == null) {
            graph = new Graph(this);
        }
        return graph;
    }

    @Override
    public Position getCenter() {
        return unit.getPosition();
    }
}
