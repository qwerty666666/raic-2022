package ai_cup_22.strategy.utils;

public class DistributionUtils {
    public static double linear(double pos, double minPos, double maxPos, double minVal, double maxVal) {
        if (pos > maxPos) {
            return maxVal;
        }
        if (pos < minPos) {
            return minVal;
        }
        return minVal + (pos - minPos) / (maxPos - minPos) * (maxVal - minVal);
    }
}
