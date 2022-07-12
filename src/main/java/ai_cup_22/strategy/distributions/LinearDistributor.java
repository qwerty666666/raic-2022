package ai_cup_22.strategy.distributions;

public class LinearDistributor implements Distributor {
    double minDist, maxDist, minVal, maxVal;

    public LinearDistributor(double minDist, double maxDist, double minVal, double maxVal) {
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
        return minVal + (val - minDist) / (maxDist - minDist) * (maxVal - minVal);
    }
}
