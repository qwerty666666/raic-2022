package ai_cup_22;

import ai_cup_22.model.Constants;
import ai_cup_22.model.Game;
import ai_cup_22.model.Order;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.utils.MovementUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MyStrategy {
    private World world;
    private Constants constants;

    public MyStrategy(Constants constants) {
        this.constants = constants;
    }

    public Order getOrder(Game game, DebugInterface debugInterface) {
        Map<Integer, UnitOrder> orders = new HashMap<>();
        long start = System.currentTimeMillis();

        if (DebugData.isEnabled) {
            DebugData.getInstance().getDefaultLayer().clear();
        }

        if (game.getCurrentTick() == 0) {
            initWorld(game);
//            updateObstaclesDebugLayer();
        } else {
            if (game.getCurrentTick() == 1) {
                world.getStaticPotentialField().buildGraph();
            }

            world.updateTick(game);

            for (var unit : world.getMyUnits().values()) {
                var action = unit.getBehaviourTree().getStrategy().getAction();

                // default action - do nothing
                orders.computeIfAbsent(unit.getId(), id -> {
                    var unitOrder = new UnitOrder(new Vec2(0, 0), new Vec2(0, 0), null);

                    action.apply(unit, unitOrder);

                    return unitOrder;
                });
            }

            if (DebugData.isEnabled) {
                updateUnitsDebugLayer();
                updateLootsDebugLayer();
                //            updatePositionsDebugLayer();
                updateDefaultDebugLayer();

                for (var unit : world.getMyUnits().values()) {
                    DebugData.getInstance().getCursorPosition().ifPresent(target -> {
                        //                     DijkstraPathFinder.minThreatPathFinder(unit.getPotentialField());
                        //                     new PotentialFieldDrawable(unit.getPotentialField()).draw(debugInterface);

                        //                    var path = new AStarPathFinder(unit.getPotentialField()).findPath(unit.getPosition(), target);
                        //                    new PathDrawable(path.getPathPositions()).draw(debugInterface);

                        //                    var line = new Line(unit.getPosition(), target);
                        //                    DebugData.getInstance().getDefaultLayer().add(new ai_cup_22.strategy.debug.primitives.Line(line, Colors.BLUE_TRANSPARENT));

                        //                    World.getInstance().getObstacles().values().stream()
                        //                            .forEach(obstacle -> {
                        //                                obstacle.getCircle().getTangentPoints(unit.getPosition()).forEach(p -> {
                        //                                    DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(new Circle(p, 0.5), Colors.BLUE_TRANSPARENT));
                        //                                });
                        //                                line.getIntersectionPoints(obstacle.getCircle()).forEach(p -> {
                        //                                    DebugData.getInstance().getDefaultLayer().add(new CircleDrawable(new Circle(p, 0.5), Colors.BLUE_TRANSPARENT));
                        //                                });
                        //                            });
                    });
                }

                DebugData.getInstance().getDefaultLayer().show(debugInterface);
            }
        }

        System.out.println(World.getInstance().getCurrentTick() + " " + (System.currentTimeMillis() - start));

        return new Order(orders);
    }

    private void initWorld(Game game) {
        World.init(constants, game);
        world = World.getInstance();
    }

    private void updateDefaultDebugLayer() {
        DebugData.getInstance().getDefaultLayer().update(world);
    }

    private void updateLootsDebugLayer() {
        DebugData.getInstance().getLootsLayer().update(world);
    }

    private void updateObstaclesDebugLayer() {
        DebugData.getInstance().getObstaclesLayer().reinit(world);
    }

    private void updateUnitsDebugLayer() {
        DebugData.getInstance().getUnitsLayer().reinit(world);
    }

    private void updatePositionsDebugLayer() {
        DebugData.getInstance().getPositionsLayer().reinit(world);
    }

    public void debugUpdate(int displayedTick, DebugInterface debugInterface) {
        updateCursorPosition(debugInterface);

        DebugData.getInstance().draw(debugInterface);
    }

    private void updateCursorPosition(DebugInterface debugInterface) {
        var position = debugInterface.getState().getCursorWorldPosition();

        DebugData.getInstance().setCursorPosition(position == null ? null : new Position(position));
    }

    public void finish() {
    }
}