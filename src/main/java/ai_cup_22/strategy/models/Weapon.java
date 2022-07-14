package ai_cup_22.strategy.models;

import ai_cup_22.model.WeaponProperties;
import ai_cup_22.strategy.World;
import java.util.HashMap;
import java.util.Map;

public class Weapon {
    private static Map<Integer, Weapon> weapons = new HashMap<>();

    private final WeaponProperties properties;
    private final int id;

    private Weapon(WeaponProperties properties, int id) {
        this.properties = properties;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getDps() {
        return getDamage() * properties.getRoundsPerSecond();
    }

    public double getMaxDistance() {
        return properties.getProjectileSpeed();
    }

    public double getSpeedPerTick() {
        return properties.getProjectileSpeed() * World.getInstance().getTimePerTick();
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

    public int getMaxBulletCount() {
        return properties.getMaxInventoryAmmo();
    }

    public static Weapon get(int id) {
        return weapons.computeIfAbsent(id, i -> new Weapon(World.getInstance().getConstants().getWeapons()[id], id));
    }
}
