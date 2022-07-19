package ai_cup_22.strategy.models;

import ai_cup_22.model.Item.Ammo;

public class AmmoLoot extends Loot {
    private int count;
    private int weaponId;

    public AmmoLoot(ai_cup_22.model.Loot loot) {
        super(loot);

        if (loot.getItem() instanceof Ammo) {
            count = ((Ammo)loot.getItem()).getAmount();
            weaponId = ((Ammo)loot.getItem()).getWeaponTypeIndex();
        }
    }

    public int getCount() {
        return count;
    }

    public int getWeaponId() {
        return weaponId;
    }
}
