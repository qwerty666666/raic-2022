package ai_cup_22.strategy.models;

import ai_cup_22.strategy.geometry.Position;

public class Loot {
    private final ai_cup_22.model.Loot loot;
    private final Position position;

    public Loot(ai_cup_22.model.Loot loot) {
        this.loot = loot;
        position = new Position(loot.getPosition());
    }

    public int getId() {
        return loot.getId();
    }

    public Position getPosition() {
        return position;
    }
}
