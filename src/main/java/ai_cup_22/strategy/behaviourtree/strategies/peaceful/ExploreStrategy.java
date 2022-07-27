package ai_cup_22.strategy.behaviourtree.strategies.peaceful;

import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.basic.LookToAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Zone;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ExploreStrategy implements Strategy {
    private static final Map<Unit, State> unitStates = new HashMap<>();
    private final Unit unit;

    public ExploreStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        return Constants.STRATEGY_EXPLORE_ORDER;
    }

    @Override
    public Action getAction() {
        if (!unitStates.containsKey(unit) || unitStates.get(unit).shouldUpdate()) {
            unitStates.put(unit, new State(unit));
        }

        var state = unitStates.get(unit);
        state.lastUpdateTick = World.getInstance().getCurrentTick();

        var vectorToExplore = new Vector(unit.getPosition(), state.positionToExplore).rotate(state.getNextAngle());
        DebugData.getInstance().getDefaultLayer().addLine(unit.getPosition(), unit.getPosition().move(vectorToExplore.normalizeToLength(10)), Colors.RED_TRANSPARENT);
        return new CompositeAction()
                .add(new MoveToWithPathfindingAction(state.positionToExplore))
                .add(new LookToAction(vectorToExplore));
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }


    private static class State {
        Position positionToExplore;
        static List<Double> angles;
        int ind;
        int lastUpdateTick;
        private Random random = new Random();
        Unit unit;

        static {
            angles = new ArrayList<>();
            for (double angle = -Math.PI * 5 / 6; angle <= Math.PI * 5 / 6; angle += Math.PI / 6) {
                angles.add(angle);
            }
            for (double angle = Math.PI * 5 / 6; angle >= -Math.PI * 5 / 6; angle -= Math.PI / 6) {
                angles.add(angle);
            }
        }

        public State(Unit unit) {
            this.unit = unit;
            positionToExplore = World.getInstance().getGlobalStrategy().getPointToExplore(unit)
                    .orElseGet(this::getNewPositionToExplore);
            this.lastUpdateTick = World.getInstance().getCurrentTick();
        }

        public boolean isStale() {
            return lastUpdateTick < World.getInstance().getCurrentTick() - 1;
        }

        public boolean isOutOfZone() {
            return !getZone().contains(positionToExplore);
        }

        public boolean isPositionInViewField() {
            return unit.getViewSegment().getCircleSegment().contains(positionToExplore);
        }

        public boolean shouldUpdate() {
            var newPoint = World.getInstance().getGlobalStrategy().getPointToExplore(unit);
            if (newPoint.isPresent()) {
                return isStale() || !newPoint.get().equals(positionToExplore);
            }

            return isStale() || isOutOfZone() || isPositionInViewField();
        }

        private Zone getZone() {
            return World.getInstance().getZone();
        }

        private Position getNewPositionToExplore() {
            var radius = random.nextDouble() * getZone().getRadius();
            var angle = random.nextDouble() * Math.PI * 2;

            return getZone().getCenter().move(
                    new Vector(radius * Math.cos(angle), radius * Math.sin(angle))
            );
        }

        public double getNextAngle() {
            var vectorToPositionToExplore = new Vector(unit.getPosition(), positionToExplore);
            var targetAngle = angles.get(ind);

            while (unit.getViewSegment().getCircleSegment().contains(vectorToPositionToExplore.rotate(targetAngle))) {
                ind++;
                if (ind == angles.size()) {
                    ind = 0;
                }

                targetAngle = angles.get(ind);
            }

            return targetAngle;
        }
    }
}
