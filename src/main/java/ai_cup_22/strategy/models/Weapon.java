package ai_cup_22.strategy.models;

import ai_cup_22.model.WeaponProperties;
import ai_cup_22.strategy.World;
import java.util.HashMap;
import java.util.Map;

public class Weapon {
    private static Map<Integer, Weapon> weapons = new HashMap<>();

    private final WeaponProperties properties;

    private Weapon(WeaponProperties properties) {
        this.properties = properties;
    }

    public double getDamage() {
        return properties.getProjectileDamage();
    }

    public double getSpread() {
        return properties.getSpread();
    }

    public double getAimFieldOfView() {
        return properties.getAimFieldOfView();
    }

    public int getAimTicks() {
        return (int)Math.ceil(properties.getAimTime() / World.getInstance().getTimePerTick());
    }

    public static Weapon get(int id) {
        return weapons.computeIfAbsent(id, i -> new Weapon(World.getInstance().getConstants().getWeapons()[id]));
    }
}
