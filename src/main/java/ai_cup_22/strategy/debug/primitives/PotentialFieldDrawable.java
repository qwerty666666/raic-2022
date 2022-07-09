package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.models.potentialfield.PotentialField;
import ai_cup_22.strategy.models.potentialfield.StaticPotentialField;

public class PotentialFieldDrawable implements Drawable {
    private final PotentialField potentialField;

    public PotentialFieldDrawable(PotentialField potentialField) {
        this.potentialField = potentialField;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        potentialField.getForces().entrySet().stream()
                .filter(e -> e.getValue() != PotentialField.UNREACHABLE_VALUE)
                .filter(e -> e.getKey().getDistanceTo(new Position(0, 0)) < 20)
                .map(e -> new CircleDrawable(new Circle(e.getKey(), 0.5), getColor(e.getValue())))
                .forEach(circle -> circle.draw(debugInterface));

        potentialField.getForces().entrySet().stream()
                .filter(e -> e.getValue() != PotentialField.UNREACHABLE_VALUE)
                .filter(e -> e.getKey().getDistanceTo(new Position(0, 0)) < 20)
                .map(e -> new Text(String.format("%.2f", e.getValue()), e.getKey(), 0.2))
                .forEach(circle -> circle.draw(debugInterface));
    }

    private Color getColor(double force) {
        var delta = 256. / (PotentialField.MAX_VALUE - PotentialField.MIN_VALUE);
        var val = (force - PotentialField.MIN_VALUE) * delta / 255;

        return new Color(1 - val, val , 0, 0.5);
    }
}
