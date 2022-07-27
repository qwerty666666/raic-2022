package ai_cup_22.strategy.models;

import ai_cup_22.model.WeaponProperties;
import ai_cup_22.strategy.Constants;
import ai_cup_22.strategy.World;
import java.util.HashMap;
import java.util.Map;

public class Weapon {
    public static final int WAND_ID = 0;
    public static final int STAFF_ID = 1;
    public static final int BOW_ID = 2;

    private static Map<Integer, Weapon> weapons = new HashMap<>();

    private final WeaponProperties properties;
    private final int id;

    private Weapon(WeaponProperties properties, int id) {
        this.properties = properties;
        this.id = id;
    }

    public boolean isWand() {
        return id == WAND_ID;
    }

    public boolean isStaff() {
        return id == STAFF_ID;
    }

    public boolean isBow() {
        return id == BOW_ID;
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

    public int getCoolDownTicks() {
        return (int) (Constants.TICKS_PER_SECOND / properties.getRoundsPerSecond());
    }

    public double getAimFieldOfView() {
        return properties.getAimFieldOfView();
    }

    public double getAimSpeedModifier() {
        return properties.getAimMovementSpeedModifier();
    }

    public double getAimTime() {
        return properties.getAimTime();
    }

    public int getAimTicks() {
        return (int)Math.ceil(properties.getAimTime() / World.getInstance().getTimePerTick());
    }

    public double getAimRotationSpeed() {
        return properties.getAimRotationSpeed();
    }

    public int getMaxBulletCount() {
        return properties.getMaxInventoryAmmo();
    }

    public static Weapon get(int id) {
        if (!weapons.containsKey(id)) {
            weapons.put(id, new Weapon(World.getInstance().getConstants().getWeapons()[id], id));
        }
        return weapons.get(id);
    }

    public static double getPriority(int weaponId) {
        if (weaponId == BOW_ID) {
            return 1;
        }
        if (weaponId == STAFF_ID) {
            return 0.5;
        }
        return 0.2;
    }
}
