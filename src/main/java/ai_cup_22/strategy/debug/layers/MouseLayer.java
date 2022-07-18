package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.debug.primitives.Line;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Vector;

public class MouseLayer extends DrawLayer {
    public void update(World world) {
        clear();
        addDistLine();
    }

    private void addDistLine() {
        DebugData.getInstance().getCursorPosition().ifPresent(position -> {
            DebugData.getInstance().getClickPosition().ifPresent(clickedPos -> {
                add(new Text(String.format("%.3f", position.getDistanceTo(clickedPos)), position, 0.15, new Vector(1, 0.5)));
                add(new Line(position, clickedPos, Colors.GRAY_TRANSPARENT));
            });
        });
    }
}
