package ai_cup_22.strategy.utils;

public class MathUtils {
    public static double restrict(double min, double max, double val) {
        return Math.min(max, Math.max(min, val));
    }
}
