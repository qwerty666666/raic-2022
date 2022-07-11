package ai_cup_22.strategy.models;

import ai_cup_22.model.Item.Ammo;
import ai_cup_22.strategy.geometry.Position;

public class AmmoLoot extends Loot {
    private int count;
    private int weaponId;

    public AmmoLoot(ai_cup_22.model.Loot loot) {
        super(loot);

        if (loot.getItem() instanceof Ammo ammo) {
            count = ammo.getAmount();
            weaponId = ammo.getWeaponTypeIndex();
        }
    }

    public int getCount() {
        return count;
    }

    public int getWeaponId() {
        return weaponId;
    }
}
