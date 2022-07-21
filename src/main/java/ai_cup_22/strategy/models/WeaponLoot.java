package ai_cup_22.strategy.models;

import ai_cup_22.model.Item;

public class WeaponLoot extends Loot {
    private int weaponId;

    public WeaponLoot(ai_cup_22.model.Loot loot) {
        super(loot);

        if (loot.getItem() instanceof Item.Weapon) {
            weaponId = ((Item.Weapon)loot.getItem()).getTypeIndex();
        }
    }

    public int getWeaponId() {
        return weaponId;
    }
}
