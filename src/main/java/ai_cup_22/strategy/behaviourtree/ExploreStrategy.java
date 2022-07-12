package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.CompositeAction;
import ai_cup_22.strategy.actions.MoveToWithPathfindingAction;
import ai_cup_22.strategy.actions.RotateAction;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Zone;
import java.util.Random;

public class ExploreStrategy implements Strategy {
    private final Unit unit;
    private Position positionToExplore;
    private Random random = new Random();

    public ExploreStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        return 0.1;
    }

    @Override
    public Action getAction() {
        if (positionToExplore == null || !getZone().contains(positionToExplore) ||
                unit.getViewSegment().contains(positionToExplore)) {
            positionToExplore = getNewPositionToExplore();
        }

        return new CompositeAction()
                .add(new MoveToWithPathfindingAction(positionToExplore))
                .add(new RotateAction());
    }

    private Position getNewPositionToExplore() {
        var radius = random.nextDouble(0, getZone().getRadius());
        var angle = random.nextDouble(0, Math.PI * 2);

        return getZone().getCenter().move(
                new Vector(radius * Math.cos(angle), radius * Math.sin(angle))
        );
    }

    private Zone getZone() {
        return World.getInstance().getZone();
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
