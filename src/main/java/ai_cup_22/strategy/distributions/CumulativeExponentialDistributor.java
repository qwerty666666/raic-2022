package ai_cup_22.strategy.distributions;

public class CumulativeExponentialDistributor implements Distributor {
    double minDist, maxDist, minVal, maxVal;

    public CumulativeExponentialDistributor(double minDist, double maxDist, double minVal, double maxVal) {
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    @Override
    public double get(double val) {
        // the plot is reversed
        if (val > maxDist) {
            return minVal;
        }
        if (val < minDist) {
            return maxVal;
        }

        return minVal + (1 - Math.pow(val / (maxDist - minDist), 2)) * (maxVal - minVal);
    }
}
