package ai_cup_22.strategy.utils;

public class MathUtils {
    public static double restrict(double min, double max, double val) {
        return Math.min(max, Math.max(min, val));
    }

    /**
     * [0, 2 * PI)
     */
    public static double normalizeAngle(double rad) {
        while (rad > Math.PI * 2) {
            rad -= Math.PI * 2;
        }
        while (rad < 0) {
            rad += Math.PI * 2;
        }
        return rad;
    }
}
