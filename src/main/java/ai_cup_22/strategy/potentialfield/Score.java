package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.List;

public class Score {
    private Position position;
    private double score;
    /**
     * Sum of all NON-STATIC NEGATIVE scores
     */
    private double threatScore;
    private double initialScore;
    private List<Score> adjacent = new ArrayList<>();
    private List<Contribution> contributions = new ArrayList<>();

    public Score(Position position) {
        this.position = position;
    }

    public void increaseScore(Contribution contribution) {
        contributions.add(contribution);
        score += contribution.getValue();
        if (contribution.getValue() < 0) {
            threatScore += contribution.getValue();
        }
    }

    public double getThreatScore() {
        return threatScore;
    }

    public boolean isUnreachable() {
        return getScore() == PotentialField.UNREACHABLE_VALUE;
    }

    public Position getPosition() {
        return position;
    }

    public double getScore() {
        return score;
    }

    public double getInitialScore() {
        return initialScore;
    }

    public void setInitialScore(double initialScore) {
        this.initialScore = initialScore;
    }

    public double getNonStaticScore() {
        return score - initialScore;
    }

    public void reset() {
        this.score = initialScore;
        this.contributions.removeIf(c -> !c.isStatic);
        this.threatScore = 0;
    }

    public List<Score> getAdjacent() {
        return adjacent;
    }

    public void addAdjacent(Score score) {
        adjacent.add(score);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Score) && position.equals(((Score)o).getPosition());
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        return position + " -> " + score;
    }


    public static class Contribution {
        private final String reason;
        private final double value;
        private final boolean isStatic;

        public Contribution(String reason, double value) {
            this(reason, value, false);
        }

        public Contribution(String reason, double value, boolean isStatic) {
            this.reason = reason;
            this.value = value;
            this.isStatic = isStatic;
        }

        public String getReason() {
            return reason;
        }

        public double getValue() {
            return value;
        }

        public boolean isStatic() {
            return isStatic;
        }
    }
}
