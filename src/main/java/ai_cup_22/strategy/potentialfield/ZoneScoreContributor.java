package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Line;
import java.util.Comparator;

public class ZoneScoreContributor implements ScoreContributor {
    public static double MIN_SCORE = -50;
    public static double RADIUS_TICKS = 20;

    @Override
    public boolean shouldContribute(Score score) {
        return true;
    }

    @Override
    public double getScoreValue(Score score) {
        var zone = World.getInstance().getZone();
        var nearestZonePosition = new Line(zone.getCenter(), score.getPosition())
                .getIntersectionPoints(zone.getCircle())
                .stream()
                .min(Comparator.comparingDouble(p -> p.getDistanceTo(score.getPosition())))
                .orElse(null);

        var distToNewZone = new Line(zone.getNewCenter(), nearestZonePosition).getLength() - zone.getNewRadius();
        var speed = distToNewZone / zone.getTicksToNewZone();

        return new LinearScoreContributor(nearestZonePosition, MIN_SCORE, 0, RADIUS_TICKS * speed)
                .getScoreValue(score);
    }
}
