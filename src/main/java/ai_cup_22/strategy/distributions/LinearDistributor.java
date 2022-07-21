package ai_cup_22.strategy.distributions;

public class LinearDistributor implements Distributor {
    double minDist, maxDist, scoreOnMinDist, scoreOnMaxDist;

    public LinearDistributor(double minDist, double maxDist, double scoreOnMinDist, double scoreOnMaxDist) {
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.scoreOnMinDist = scoreOnMinDist;
        this.scoreOnMaxDist = scoreOnMaxDist;
    }

    @Override
    public double get(double val) {
        if (val > maxDist) {
            return scoreOnMaxDist;
        }
        if (val < minDist) {
            return scoreOnMinDist;
        }
        return scoreOnMinDist + (val - minDist) / (maxDist - minDist) * (scoreOnMaxDist - scoreOnMinDist);
    }
}
