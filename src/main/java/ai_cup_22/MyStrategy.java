package ai_cup_22;

import ai_cup_22.model.Constants;
import ai_cup_22.model.Game;
import ai_cup_22.model.Order;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.LookToAction;
import ai_cup_22.strategy.actions.MoveToAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.PathDrawable;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.pathfinding.AStarPathFinder;
import java.util.Collections;
import java.util.Comparator;

public class MyStrategy {
    private World world;
    private Constants constants;

    public MyStrategy(Constants constants) {
        this.constants = constants;
    }

    public Order getOrder(Game game, DebugInterface debugInterface) {
        if (game.getCurrentTick() == 0) {
            initWorld(game);
//            updateObstaclesDebugLayer();
            return new Order(Collections.emptyMap());
        }

        if (game.getCurrentTick() == 1) {
            world.getStaticPotentialField().buildGraph();
        }

        world.updateTick(game);




        java.util.HashMap<Integer, UnitOrder> orders = new java.util.HashMap<>();

        for (var unit: world.getMyUnits().values()) {
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
            updatePositionsDebugLayer();
            updateDefaultDebugLayer();

//        new PotentialFieldDrawable(world.getStaticPotentialField()).draw(debugInterface);

            DebugData.getInstance().getDefaultLayer().show(debugInterface);

//            for (var unit: world.getMyUnits().values()) {
//                DebugData.getInstance().getCursorPosition().ifPresent(target -> {
//                    var path = new AStarPathFinder().findPath(unit.getPotentialField(), unit.getPosition(), target);
//                    new PathDrawable(path).draw(debugInterface);
//                });
//            }
        }

        return new Order(orders);
    }

    private void initWorld(Game game) {
        World.init(constants, game);
        world = World.getInstance();
    }

    private void updateDefaultDebugLayer() {
        DebugData.getInstance().getDefaultLayer().update(world);
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

    public void debugUpdate(DebugInterface debugInterface) {
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