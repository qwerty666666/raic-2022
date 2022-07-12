package ai_cup_22.strategy.utils;

import java.util.stream.Collectors;

public class StringUtils {
    public static String pad(String s, int n) {
        return s.lines()
                .map(line -> ".".repeat(n) + line)
                .collect(Collectors.joining("\n"));
    }
}
