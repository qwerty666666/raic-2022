package ai_cup_22.strategy.debug.primitives;

import ai_cup_22.DebugInterface;
import ai_cup_22.debugging.Color;
import ai_cup_22.strategy.actions.MoveByPotentialFieldAction;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.potentialfield.PotentialField;
import java.util.stream.Collectors;

public class PotentialFieldDrawable implements Drawable {
    private final PotentialField potentialField;

    public PotentialFieldDrawable(PotentialField potentialField) {
        this.potentialField = potentialField;
    }

    @Override
    public void draw(DebugInterface debugInterface) {
        potentialField.getScores().values().stream()
                .filter(score -> score.getScore() != PotentialField.UNREACHABLE_VALUE)
                .filter(score -> score.getScore() != 0)
                .filter(score -> score.getPosition().getDistanceTo(potentialField.getCenter()) < 10)
                .map(score -> new CircleDrawable(new Circle(score.getPosition(), 0.5), getColor(score.getScore())))
                .forEach(circle -> circle.draw(debugInterface));

        var nodeScores = MoveByPotentialFieldAction.getNodeScores(potentialField.getGraph());

        potentialField.getGraph().getNodes().values().stream()
//                .filter(node -> node.getScoreValue() != 0)
                .filter(score -> score.getPosition().getDistanceTo(potentialField.getCenter()) < 10)
                .map(node -> new Text(String.format("sc: %.2f\nres: (%.2f)", node.getScoreValue(), nodeScores.get(node)), node.getPosition(), 0.15))
//                .map(node -> new Text(String.format("%.2f", node.getScore().getScore()), node.getPosition(), 0.2))
                .forEach(circle -> circle.draw(debugInterface));

        DebugData.getInstance().getCursorPosition()
                .filter(pos -> potentialField.getCircle().contains(pos))
                .ifPresent(pos -> {
                    var node = potentialField.getGraph().getNodes().get(new Position((int)pos.getX(), (int)pos.getY()));
                    if (node != null) {
                        var text = "Score: " + node.getScoreValue() + "\n";
                        text += "Dist: " + node.getDist() + "\n\n";
                        text += node.getScore().getContributions().stream()
                                .map(contribution -> contribution.getReason() + " (" + contribution.getValue() + ")")
                                .collect(Collectors.joining("\n"));
                        text += "\n\n";
                        text += "res: " + nodeScores.get(node);
                        new Text(text, node.getPosition(), 0.5, new Vector(0, -0.5)).draw(debugInterface);
                    }
                });
//        potentialField.getGraph().getNodes().values().stream()
////                .map(node -> new Text(String.format("%.2f", node.getScore().getScore()), node.getPosition(), 0.2))
//                .filter(score -> score.getPosition().getDistanceTo(potentialField.getCenter()) < 10)
//                .forEach(node -> {
//                    if (node.getParent() != null) {
//                        new Line(node.getPosition(), node.getParent().getPosition(), Colors.GRAY_TRANSPARENT).draw(debugInterface);
//                    }
//                });
    }

    private Color getColor(double force) {
        var delta = 256. / (PotentialField.MAX_VALUE - PotentialField.MIN_VALUE);
        var val = (force - PotentialField.MIN_VALUE) * delta / 255;

        return new Color(1 - val, val , 0, 0.5);
    }
}
