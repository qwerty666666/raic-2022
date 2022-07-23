package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.pathfinding.Graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnitPotentialField implements PotentialField {
    private static final Map<Position, CacheItem> cache = new HashMap<>();

    protected Map<Position, Score> scores;
    protected Graph graph;
    protected Circle circle;

    protected UnitPotentialField(Position position) {
        circle = new Circle(position, Constants.PF_RADIUS);
        scores = World.getInstance().getStaticPotentialField().getScoresInCircle(circle);
        scores.values().forEach(Score::reset);
    }

    public static UnitPotentialField getCachedOrCreate(Unit unit) {
        // get cached
        var position = new Position((int) unit.getPosition().getX(), (int) unit.getPosition().getY());

        if (!cache.containsKey(position)) {
            var pf = new UnitPotentialField(position);

            cache.put(position, new UnitPotentialField.CacheItem(pf));
            return pf;
        } else {
            var cacheItem = cache.get(position);
            cacheItem.lifetimeTicks = 0;

            var pf = cacheItem.getPotentialField();

            // reset PF
            pf.scores.values().forEach(Score::reset);
            pf.graph = null;

            return pf;
        }
    }

    public static void updateCache() {
        for (var item: new ArrayList<>(cache.values())) {
            item.increaseLifetimeTicks();
            if (item.getLifetimeTicks() > Constants.PF_CACHE_MAX_LIFETIME_TICKS) {
                cache.remove(item.getPosition());
            }
        }
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
        return circle.getCenter();
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


    private static class CacheItem {
        private UnitPotentialField potentialField;
        private Position position;
        private int lifetimeTicks;

        public CacheItem(UnitPotentialField potentialField) {
            this.potentialField = potentialField;
            this.position = potentialField.getCenter();
        }

        public void increaseLifetimeTicks() {
            lifetimeTicks += 1;
        }

        public UnitPotentialField getPotentialField() {
            return potentialField;
        }

        public int getLifetimeTicks() {
            return lifetimeTicks;
        }

        public Position getPosition() {
            return position;
        }
    }
}
