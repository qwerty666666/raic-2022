package ai_cup_22.strategy.distributions;

public class ExponentialDistributor implements Distributor {
    double minDist, maxDist, minVal, maxVal;

    public ExponentialDistributor(double minDist, double maxDist, double minVal, double maxVal) {
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    @Override
    public double get(double val) {
        if (val > maxDist) {
            return maxVal;
        }
        if (val < minDist) {
            return minVal;
        }

        return minVal + Math.pow(val / (maxDist - minDist), 2) * (maxVal - minVal);
    }
}
