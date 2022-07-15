package ai_cup_22.strategy.simulation.dodgebullets;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Obstacle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameBoard {
//    private List<Circle> obstacles;
//    private List<Bullet> bullets;
//    private Unit unit;
//    private Position targetDirection;
//    private int simulationDepth;
//
//    public GameBoard(ai_cup_22.strategy.models.Unit u, Position directionTarget, int simulationDepth) {
//        unit = new Unit(u.getCircle(), u.getSpeedVectorPerTick(), u.getDirection());
//        obstacles = getObstacles(u, simulationDepth);
//        this.simulationDepth = simulationDepth;
//        this.targetDirection = directionTarget;
//        bullets = World.getInstance().getBullets().values().stream()
//                .map(bullet -> new Bullet(bullet.getPosition(), bullet.getVelocity(), bullet.getRemainingLifetimeTicks()))
//                .collect(Collectors.toList());
//    }
//
//    private GameBoard() {
//    }
//
//    private List<Circle> getObstacles(ai_cup_22.strategy.models.Unit unit, int ticks) {
//        var maxDist = ticks * unit.getMaxForwardSpeedPerTick() + 5;
//
//        var obstacles = World.getInstance().getObstacles().values().stream()
//                .filter(obstacle -> obstacle.getCenter().getDistanceTo(unit.getPosition()) < maxDist)
//                .map(Obstacle::getCircle)
//                .collect(Collectors.toList());
//        var units = Stream.concat(
//                        World.getInstance().getMyUnits().values().stream(),
//                        World.getInstance().getEnemyUnits().values().stream()
//                )
//                .filter(u -> u.getId() != unit.getId())
//                .map(ai_cup_22.strategy.models.Unit::getCircle)
//                .collect(Collectors.toList());
//
//        obstacles.addAll(units);
//
//        return obstacles;
//    }
//
//    public SimulationResult simulate() {
//
//    }
//
//    private SimulationResult simulate(GameBoard gameBoard) {
//        var childGame = gameBoard.clone();
//    }
//
//    private SimulationResult simulateTick() {
//
//    }
//
//    protected GameBoard clone() {
//        var clone = new GameBoard();
//        clone.unit = unit;
//        clone.targetDirection = targetDirection;
//        clone.simulationDepth = simulationDepth;
//        clone.bullets = bullets;
//        clone.obstacles = obstacles;
//        return clone;
//    }
}
