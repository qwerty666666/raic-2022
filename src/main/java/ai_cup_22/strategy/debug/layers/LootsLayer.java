package ai_cup_22.strategy.debug.layers;

import ai_cup_22.strategy.World;
import ai_cup_22.strategy.debug.Colors;
import ai_cup_22.strategy.debug.primitives.CircleDrawable;
import ai_cup_22.strategy.debug.primitives.Text;
import ai_cup_22.strategy.geometry.Circle;

public class LootsLayer extends DrawLayer {
    public void update(World world) {
        clear();

        for (var loot: world.getAmmoLoots().values()) {
            add(new CircleDrawable(new Circle(loot.getPosition(), 0.8), Colors.GREEN_TRANSPARENT));
            add(new Text(Integer.toString(loot.getCount()), loot.getPosition()));
        }
        for (var loot: World.getInstance().getWeaponLoots().values()) {
            add(new CircleDrawable(new Circle(loot.getPosition(), 0.8), Colors.GREEN_TRANSPARENT));
        }
        for (var loot: World.getInstance().getShieldLoots().values()) {
            add(new CircleDrawable(new Circle(loot.getPosition(), 0.8), Colors.GREEN_TRANSPARENT));
        }
    }
}
