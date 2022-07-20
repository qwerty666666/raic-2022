package ai_cup_22;

import ai_cup_22.model.Constants;
import ai_cup_22.model.Game;
import ai_cup_22.model.Order;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.Unit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyStrategy {
    private World world;
    private Constants constants;
    private int totalTime;
    private int maxTime;

    public MyStrategy(Constants constants) {
        this.constants = constants;
    }

    public Order getOrder(Game game, DebugInterface debugInterface) {
//        // Get current size of heap in bytes
//        long heapSize = Runtime.getRuntime().totalMemory();
//        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
//        long heapMaxSize = Runtime.getRuntime().maxMemory();
//        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
//        long heapFreeSize = Runtime.getRuntime().freeMemory();
//
//        System.out.println(heapSize + " " + heapMaxSize + " " + heapFreeSize);


        Map<Integer, UnitOrder> orders = new HashMap<>();
        long start = System.currentTimeMillis();

        if (DebugData.isEnabled) {
            DebugData.getInstance().getDefaultLayer().clear();
        }

        if (game.getCurrentTick() == 0) {
            initWorld(game);
//            updateObstaclesDebugLayer();
        } else if (game.getCurrentTick() == 1) {
            world.getStaticPotentialField().fillStaticData(world);
        }

        world.updateTick(game);


        world.getMyUnits().values().stream()
                .sorted(Comparator.comparing(Unit::isSpawned).reversed()
                        .thenComparingDouble(Unit::getId)
                )
                .forEach(unit -> {
                    var action = unit.getBehaviourTree().getStrategy().getAction();

                    orders.computeIfAbsent(unit.getId(), id -> {
                        // default action - do nothing
                        var unitOrder = new UnitOrder(new Vec2(0, 0), new Vec2(0, 0), null);

                        action.apply(unit, unitOrder);

                        return unitOrder;
                    });
                });


        if (DebugData.isEnabled) {
            updateUnitsDebugLayer();
            updateLootsDebugLayer();
            //            updatePositionsDebugLayer();
            updateDefaultDebugLayer();

            for (var unit : world.getMyUnits().values()) {
                DebugData.getInstance().getCursorPosition().ifPresent(target -> {
//                        unit.getPotentialField().getScoresNear(target).forEach(point -> {
//                            if (point == null) {
//                                DebugData.getInstance().getDefaultLayer().add(new Text("null", unit.getPosition()));
//                            } else {
//                                DebugData.getInstance().getDefaultLayer().add(
//                                        new CircleDrawable(new Circle(point.getPosition(), 1), Colors.BLUE_TRANSPARENT)
//                                );
//                            }
//                        });


//                        var pathFinder = new DijkstraPathFinder(unit.getPotentialField(), unit.getPosition());
//                        new PathDrawable(pathFinder.findPath(unit.getPosition(), target).getPathPositions()).draw(debugInterface);


//                                            var line = new ai_cup_22.strategy.geometry.Line(unit.getPosition(), target);
//                                            DebugData.getInstance().getDefaultLayer().add(new ai_cup_22.strategy.debug.primitives.Line(line, Colors.BLUE_TRANSPARENT));

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


        var tickTime = (System.currentTimeMillis() - start);
        totalTime += tickTime;
        maxTime = (int) Math.max(maxTime, tickTime);
        System.out.println(World.getInstance().getCurrentTick() + " " + tickTime + " " + totalTime + " | " +
                maxTime + " " + (totalTime / (world.getCurrentTick() + 1)));


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

    private void updateMouseDebugLayer() {
        DebugData.getInstance().getMouseLayer().update(world);
    }


    public void debugUpdate(int displayedTick, DebugInterface debugInterface) {
        debugInterface.setAutoFlush(false);

        updateCursor(debugInterface);

        DebugData.getInstance().draw(debugInterface);

        debugInterface.flush();
    }

    private void updateCursor(DebugInterface debugInterface) {
        var position = debugInterface.getState().getCursorWorldPosition();

        DebugData.getInstance().setCursorPosition(position == null ? null : new Position(position));

        if (Arrays.asList(debugInterface.getState().getPressedKeys()).contains("MouseRight")) {
            DebugData.getInstance().setClickPosition(
                    DebugData.getInstance().getClickPosition()
                            .map(p -> (Position) null)
                            .filter(Objects::nonNull)
                            .orElse(new Position(debugInterface.getState().getCursorWorldPosition()))
            );
        }

        updateMouseDebugLayer();
    }

    public void finish() {
    }
}