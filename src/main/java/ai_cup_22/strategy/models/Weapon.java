package ai_cup_22.strategy.models;

import ai_cup_22.model.WeaponProperties;
import ai_cup_22.strategy.World;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Weapon {
    private static Map<Integer, Weapon> weapons = new HashMap<>();

    private final double damage;
    private final double spread;

    private Weapon(WeaponProperties w) {
        damage = w.getProjectileDamage();
        spread = w.getSpread();
    }

    public double getDamage() {
        return damage;
    }

    public double getSpread() {
        return spread;
    }

    public static Weapon get(int id) {
        return weapons.computeIfAbsent(id, i -> new Weapon(World.getInstance().getConstants().getWeapons()[id]));
    }
}
