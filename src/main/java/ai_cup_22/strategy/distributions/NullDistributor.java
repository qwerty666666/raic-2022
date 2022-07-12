package ai_cup_22.strategy.distributions;

public class NullDistributor implements Distributor {
    @Override
    public double get(double val) {
        return 0;
    }
}
