package ai_cup_22.strategy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final List<T> pool;
    private final Supplier<T> creator;
    private int cur;

    public ObjectPool(int initSize, Supplier<T> creator) {
        this.pool = new ArrayList<>(initSize);
        this.creator = creator;
    }

    public T getNext() {
        if (cur == pool.size()) {
            pool.add(creator.get());
        }

        return pool.get(cur++);
    }

    public void reset() {
        cur = 0;
    }
}
