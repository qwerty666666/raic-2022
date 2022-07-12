package ai_cup_22.strategy.distributions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FirstMatchDistributor implements Distributor {
    private List<Node> nodes = new ArrayList<>();

    public FirstMatchDistributor add(Function<Double, Boolean> condition, Distributor distributor) {
        nodes.add(new Node(distributor, condition));
        return this;
    }

    @Override
    public double get(double val) {
        return nodes.stream()
                .filter(node -> node.condition.apply(val))
                .findFirst()
                .orElseGet(() -> new Node(new NullDistributor(), x -> true))
                .distributor
                .get(val);
    }

    private static class Node {
        public Distributor distributor;
        public Function<Double, Boolean> condition;

        public Node(Distributor distributor, Function<Double, Boolean> condition) {
            this.distributor = distributor;
            this.condition = condition;
        }
    }
}
