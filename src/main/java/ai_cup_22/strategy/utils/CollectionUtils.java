package ai_cup_22.strategy.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionUtils {
    public static <T> Set<T> diff(Set<T> s1, Set<T> s2) {
        return s1.stream().filter(i -> !s2.contains(i)).collect(Collectors.toSet());
    }

    public static <T> Set<T> intersect(Set<T> s1, Set<T> s2) {
        return s1.stream().filter(s2::contains).collect(Collectors.toSet());
    }

    public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
        var res = new HashSet<>(s1);
        res.addAll(s2);
        return res;
    }
}
