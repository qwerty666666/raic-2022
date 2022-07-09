package ai_cup_22;

import ai_cup_22.model.Constants;
import ai_cup_22.model.Game;
import ai_cup_22.model.Order;
import ai_cup_22.model.UnitOrder;
import ai_cup_22.model.Vec2;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.LookToAction;
import ai_cup_22.strategy.actions.MoveToAction;
import ai_cup_22.strategy.actions.ShootAction;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.PotentialFieldDrawable;
import ai_cup_22.strategy.geometry.Position;
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
        }

        world.updateTick(game);




        java.util.HashMap<Integer, UnitOrder> orders = new java.util.HashMap<>();

        for (var unit: world.getMyUnits().values()) {
            var action = new CompositeAction();

            if (world.getEnemyUnits().isEmpty()) {
                action
                        .add(new MoveToAction(new Position(0, 0)))
                        .add(new LookToAction(unit.getDirection().rotate(Math.PI / 2).getEndPosition()));
            } else {
                var enemiesUnderAttack = world.getEnemyUnits().values().stream()
                        .filter(unit::canShoot)
                        .sorted(Comparator.comparingDouble(a -> a.getDistanceTo(unit)))
                        .toList();

                if (!enemiesUnderAttack.isEmpty()) {
                    action
                            .add(new MoveToAction(new Position(0, 0)))
                            .add(new ShootAction(enemiesUnderAttack.get(0)));
                } else {
                    var target = world.getEnemyUnits().values().stream()
                            .min(Comparator.comparingDouble(a -> a.getDistanceTo(unit)))
                            .orElse(null);

                    action
                            .add(new MoveToAction(new Position(0, 0)))
                            .add(new LookToAction(target));
                }
            }

            orders.computeIfAbsent(unit.getId(), id -> {
                var unitOrder = new UnitOrder(new Vec2(0, 0), new Vec2(0, 0), null);

                action.apply(unit, unitOrder);

                return unitOrder;
            });
        }



        updateUnitsDebugLayer();
        updatePositionsDebugLayer();
        updateDefaultDebugLayer();

//        new PotentialFieldDrawable(world.getStaticPotentialField()).draw(debugInterface);

        DebugData.getInstance().getDefaultLayer().show(debugInterface);



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
        DebugData.getInstance().draw(debugInterface);
    }

    public void finish() {
    }
}