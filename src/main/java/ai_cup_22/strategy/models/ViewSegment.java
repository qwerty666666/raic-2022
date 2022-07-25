package ai_cup_22.strategy.models;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ViewSegment {
    private List<NonSeeSegment> nonSeeSegments;
    private CircleSegment viewSegment;

    public ViewSegment(Unit unit) {
        this.nonSeeSegments = getNonSeeSegments(unit);
        this.viewSegment = getViewSegment(unit);
    }

    private CircleSegment getViewSegment(Unit unit) {
        var fieldOfView = Math.toRadians(World.getInstance().getConstants().getFieldOfView());
        var aimFieldOfView = unit.getWeaponOptional()
                .map(weapon -> Math.toRadians(weapon.getAimFieldOfView()))
                .orElse(fieldOfView);

        return new CircleSegment(
                new Circle(unit.getPosition(), Constants.USER_VIEW_DIST),
                unit.getDirection().getAngle(),
                fieldOfView - (fieldOfView - aimFieldOfView) * unit.getAim()
        );
    }

    private List<NonSeeSegment> getNonSeeSegments(Unit unit) {
        if (!World.getInstance().getConstants().isViewBlocking() || !unit.isSpawned()) {
            return Collections.emptyList();
        }

        var obstacles = World.getInstance().getNonLookThroughObstacles().values().stream()
                .filter(obstacle -> obstacle.getCenter().getSquareDistanceTo(unit.getPosition()) < Constants.USER_VIEW_DIST * Constants.USER_VIEW_DIST)
                .map(Obstacle::getCircle)
                .collect(Collectors.toList());

        obstacles.addAll(
                // Can see through non spawned units.
                // Phantom units are out of my view field (therefore do not consider them).
                Stream.concat(
                                World.getInstance().getMyUnits().values().stream(),
                                World.getInstance().getEnemyUnits().values().stream()
                        )
                        .filter(Unit::isSpawned)
                        .filter(u -> u.getId() != unit.getId())
                        .map(Unit::getCircle)
                        .collect(Collectors.toList())
        );

        return obstacles.stream()
                .map(obstacle -> new NonSeeSegment(unit.getPosition(), obstacle))
                .collect(Collectors.toList());
    }

    public CircleSegment getCircleSegment() {
        return viewSegment;
    }

    public boolean canSee(Position pos) {
        if (!viewSegment.contains(pos)) {
            return false;
        }

        for (var segment: nonSeeSegments) {
            if (!segment.canSee(pos)) {
                return false;
            }
        }

        return true;
    }

    public boolean canSee(Circle circle) {
        if (!viewSegment.contains(circle)) {
            return false;
        }

        for (var segment: nonSeeSegments) {
            if (!segment.canSee(circle)) {
                return false;
            }
        }

        return true;
    }


    private static class NonSeeSegment {
        private CircleSegment segment;
        private Position obstacleCenter;
        private Position myPosition;
        private double distToObstacle;

        public NonSeeSegment(Position myPosition, Circle obstacle) {
            var centerAngle = new Vector(myPosition, obstacle.getCenter()).getAngle();
            var angle = Math.asin(obstacle.getRadius() / obstacle.getCenter().getDistanceTo(myPosition));

            this.segment = new CircleSegment(new Circle(myPosition, Constants.USER_VIEW_DIST), centerAngle, angle * 2);
            this.obstacleCenter = obstacle.getCenter();
            this.myPosition = myPosition;
            this.distToObstacle = myPosition.getDistanceTo(obstacleCenter);
        }

        public boolean canSee(Position pos) {
            if (segment.contains(pos)) {
                return myPosition.getDistanceTo(pos) < distToObstacle;
            }
            return true;
        }

        public boolean canSee(Circle circle) {
            if (!canSee(circle.getCenter())) {
                return false;
            }

            return circle.getCenter().getDistanceTo(myPosition) < distToObstacle &&
                    segment.getBoundaries().stream()
                            .anyMatch(circle::isIntersect);
        }
    }




//    private Position myPosition;
//    private CircleSegment viewSegment;
//    private List<Circle> obstacles;
//
//    public ViewSegment(Unit unit) {
//        this.myPosition = unit.getPosition();
//        this.viewSegment = unit.getViewSegment();
//        this.obstacles = getObstacles(unit);
//    }
//
//    private List<Circle> getObstacles(Unit unit) {
//        if (!World.getInstance().getConstants().isViewBlocking() || !unit.isSpawned()) {
//            return Collections.emptyList();
//        }
//
//        var obstacles = World.getInstance().getObstaclesInRange(unit.getPosition(), Constants.USER_VIEW_DIST).stream()
//                .filter(obstacle -> !obstacle.isCanSeeThrough())
//                .map(Obstacle::getCircle)
//                .collect(Collectors.toList());
//
//        obstacles.addAll(
//                // Can see through non spawned units.
//                // Phantom units are out of my view field (therefore do not consider them).
//                Stream.concat(
//                                World.getInstance().getMyUnits().values().stream(),
//                                World.getInstance().getEnemyUnits().values().stream()
//                        )
//                        .filter(u -> u.isSpawned() && u.getId() != unit.getId())
//                        .map(Unit::getCircle)
//                        .collect(Collectors.toList())
//        );
//
//        return obstacles;
//    }
//
//    public boolean canSee(Position pos) {
//        if (!viewSegment.contains(pos)) {
//            return false;
//        }
//
//        var line = new Line(pos, myPosition);
//
//        for (var obstacle: obstacles) {
//            if (obstacle.isIntersect(line)) {
//                return false;
//            }
//        }
//
//        return true;
//    }
}
