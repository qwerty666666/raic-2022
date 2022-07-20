package ai_cup_22.strategy.potentialfield.scorecontributors;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Zone;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.ConstantOutCircleScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.basic.LinearScoreContributor;
import ai_cup_22.strategy.potentialfield.scorecontributors.composite.FirstMatchCompositeScoreContributor;
import java.util.Comparator;

public class ZoneScoreContributor implements ScoreContributor {
    private final Zone zone;
    private final PotentialField potentialField;
    private final double zoneSpeedPerTick;

    public ZoneScoreContributor(PotentialField potentialField) {
        this.zone = World.getInstance().getZone();
        this.potentialField = potentialField;
        this.zoneSpeedPerTick = getZoneSpeedAtPosition(zone, potentialField.getCenter());
    }

    private double getZoneSpeedAtPosition(Zone zone, Position position) {
        var nearestZonePosition = new Line(zone.getNewCenter(), position)
                .getIntersectionPointsAsRay(zone.getCircle())
                .stream()
                .min(Comparator.comparingDouble(p -> p.getSquareDistanceTo(position)))
                .orElseThrow();

        var distToNewZone = new Line(zone.getNewCenter(), nearestZonePosition).getLength() - zone.getNewRadius();

        return distToNewZone / zone.getTicksToNewZone();
    }

    @Override
    public boolean shouldContribute(Score score) {
        return zone.getRadius() - potentialField.getCenter().getDistanceTo(zone.getCenter()) < 60;
    }

    @Override
    public double getScoreValue(Score score) {
        return new FirstMatchCompositeScoreContributor("Zone")
                .add(new ConstantOutCircleScoreContributor(
                        zone.getCircle().enlarge(Constants.PF_OUT_OF_ZONE_DIST),
                        Constants.PF_OUT_OF_ZONE_MIN_SCORE
                ))
                .add(new LinearScoreContributor(
                        zone.getCenter(),
                        Constants.PF_OUT_OF_ZONE_MAX_SCORE,
                        Constants.PF_OUT_OF_ZONE_MIN_SCORE,
                        zone.getRadius(),
                        zone.getRadius() + Constants.PF_OUT_OF_ZONE_DIST
                ))
                .add(new LinearScoreContributor(
                        zone.getCenter(),
                        Constants.PF_ZONE_MAX_SCORE,
                        Constants.PF_ZONE_MIN_SCORE,
                        zone.getRadius() - Math.max(Constants.PF_ZONE_DIST_TICKS * zoneSpeedPerTick, Constants.PF_ZONE_MIN_THREAT_DIST),
                        zone.getRadius()
                ))
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
