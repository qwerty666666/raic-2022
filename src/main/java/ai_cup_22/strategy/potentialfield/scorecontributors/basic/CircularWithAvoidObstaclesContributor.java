package ai_cup_22.strategy.potentialfield.scorecontributors.basic;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.potentialfield.BaseScoreContributor;
import ai_cup_22.strategy.potentialfield.Score;
import ai_cup_22.strategy.potentialfield.ScoreContributor;
import java.util.List;
import java.util.stream.Collectors;

public class CircularWithAvoidObstaclesContributor extends BaseScoreContributor {
    private final ScoreContributor scoreContributor;
    private final double multiplier;
    private final List<CircleSegment> obstacleSegments;

    public CircularWithAvoidObstaclesContributor(String contributionReason, Position position, ScoreContributor scoreContributor,
            double maxDist, double multiplier) {
        super(contributionReason, false);
        this.scoreContributor = scoreContributor;
        this.multiplier = multiplier;

        var contributionCircle = new Circle(position, maxDist);

        obstacleSegments = World.getInstance().getNonShootThroughObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getDistanceTo(position) < maxDist)
                .map(Obstacle::getCircle)
                .map(circle -> {
                    var centerAngle = new Vector(position, circle.getCenter()).getAngle();
                    var angle = Math.asin(circle.getRadius() / circle.getCenter().getDistanceTo(position));

                    return new CircleSegment(contributionCircle, centerAngle, angle * 2);
                })
                .collect(Collectors.toList());

//        obstacleSegments.forEach(s -> DebugData.getInstance().getDefaultLayer().addSegment(s));
    }

    @Override
    public boolean shouldContribute(Score score) {
        return scoreContributor.shouldContribute(score);
    }

    @Override
    public double getScoreValue(Score score) {
        var mul = 1.;

        for (var segment: obstacleSegments) {
            if (segment.contains(score.getPosition())) {
                mul = this.multiplier;
                break;
            }
        }

        return scoreContributor.getScoreValue(score) * mul;
    }
}
