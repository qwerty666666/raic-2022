package ai_cup_22.strategy.potentialfield.scorecontributors;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.models.Zone;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.ConstantOutCircleScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
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
                .getIntersectionPointsAsRay(zone.getCircle())
                .stream()
                .min(Comparator.comparingDouble(p -> p.getSquareDistanceTo(score.getPosition())))
                .orElseThrow();

        var distToNewZone = new Line(zone.getNewCenter(), nearestZonePosition).getLength() - zone.getNewRadius();
        var zoneSpeedPerTick = distToNewZone / zone.getTicksToNewZone();

        return new FirstMatchCompositeScoreContributor("Zone")
                .add(new ConstantOutCircleScoreContributor(zone.getCircle().enlarge(10), PotentialField.MIN_VALUE))
                .add(s -> isScoreOutOfZone(s, zone),
                        new LinearScoreContributor(nearestZonePosition, MIN_SCORE, PotentialField.MIN_VALUE, 10)
                )
                .add(new LinearScoreContributor(nearestZonePosition, MIN_SCORE, 0, RADIUS_TICKS * zoneSpeedPerTick))
                .getScoreValue(score);
    }

    @Override
    public String getContributionReason(Score score) {
        return "Zone";
    }

    private boolean isScoreOutOfZone(Score score, Zone zone) {
        return score.getPosition().getSquareDistanceTo(zone.getCenter()) >= zone.getRadius() * zone.getRadius();
    }
}
