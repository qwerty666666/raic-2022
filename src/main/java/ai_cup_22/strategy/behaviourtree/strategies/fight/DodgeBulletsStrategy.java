package ai_cup_22.strategy.behaviourtree.strategies.fight;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.Action;
import ai_cup_22.strategy.actions.DodgeBulletsAction;
import ai_cup_22.strategy.behaviourtree.Strategy;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Unit;
import java.util.List;
import java.util.stream.Collectors;

public class DodgeBulletsStrategy implements Strategy {
    private final Unit unit;

    public DodgeBulletsStrategy(Unit unit) {
        this.unit = unit;
    }

    @Override
    public double getOrder() {
        return !getBullets().isEmpty() ? MAX_ORDER : MIN_ORDER;
    }

    @Override
    public Action getAction() {
        return new DodgeBulletsAction();
    }

    private List<Bullet> getBullets() {
        return World.getInstance().getBullets().values().stream()
                .filter(bullet -> bullet.getUnitId() != unit.getId())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return Strategy.toString(this);
    }
}
