package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.Map;

public class UnitPotentialField implements PotentialField {
    public static final double FIELD_RADIUS = 30;
    protected Map<Position, Score> scores;
    protected Unit unit;
    protected Graph graph;
    protected Circle circle;

    public UnitPotentialField(Unit unit) {
        this.unit = unit;

        circle = new Circle(unit.getPosition(), FIELD_RADIUS);
        scores = World.getInstance().getStaticPotentialField().getScoresInCircle(circle);
        scores.values().forEach(Score::reset);
    }

    @Override
    public Map<Position, Score> getScores() {
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

    @Override
    public Circle getCircle() {
        return circle;
    }

    @Override
    public Score getScoreByIndex(int x, int y) {
        var score = World.getInstance().getStaticPotentialField().getScoreByIndex(x, y);
        if (score == null) {
            return null;
        }
        return scores.get(score.getPosition());
    }
}
