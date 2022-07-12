package ai_cup_22.strategy.distributions;

public class ConstDistributor implements Distributor {
    private double val;

    public ConstDistributor(double val) {
        this.val = val;
    }

    @Override
    public double get(double val) {
        return this.val;
    }
}
