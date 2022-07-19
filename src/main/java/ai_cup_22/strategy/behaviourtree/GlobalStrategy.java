package ai_cup_22.strategy.behaviourtree;

import ai_cup_22.strategy.models.Loot;
import java.util.HashSet;
import java.util.Set;

public class GlobalStrategy {
    private final Set<Integer> takenLoots = new HashSet<>();

    public void updateTick() {
        takenLoots.clear();
    }

    public void markLootAsTaken(Loot loot) {
        takenLoots.add(loot.getId());
    }

    public boolean isLootTaken(Loot loot) {
        return takenLoots.contains(loot.getId());
    }
}
