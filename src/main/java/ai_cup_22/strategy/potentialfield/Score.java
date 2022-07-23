package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.debug.DebugData;
import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.List;

public class Score implements Cloneable {
    private Position position;
    private double score;
    /**
     * Sum of all NON-STATIC NEGATIVE scores
     */
    private double threatScore;
    private double initialScore;
    private List<Contribution> contributions = new ArrayList<>();
    private int x;
    private int y;
    private boolean isUnreachable;

    private Score() {
    }

    public Score(Position position, int x, int y) {
        this.position = position;
        this.x = x;
        this.y = y;
    }

    public Score(Position position) {
        this.position = position;
        this.x = Integer.MIN_VALUE;
        this.y = Integer.MIN_VALUE;
    }

    public void increaseScore(Contribution contribution) {
        increaseScore(contribution.getValue());
        contributions.add(contribution);
    }

    public void increaseScore(double value) {
        score += value;
        if (value < 0) {
            threatScore += value;
        }
    }

    public double getThreatScore() {
        return threatScore;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    public boolean isUnreachable() {
        return isUnreachable;
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

    public void setIsUnreachable() {
        this.isUnreachable = true;
    }

    public double getNonStaticScore() {
        return score - initialScore;
    }

    public void reset() {
        this.score = initialScore;
        if (DebugData.isEnabled) {
            this.contributions.removeIf(c -> !c.isStatic);
        }
        this.threatScore = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

    @Override
    public Score clone() throws CloneNotSupportedException {
        var clone = new Score();

        clone.position = position;
        clone.score = score;
        clone.threatScore = threatScore;
        clone.initialScore = initialScore;
        clone.contributions = new ArrayList<>(contributions);
        clone.x = x;
        clone.y = y;
        clone.isUnreachable = isUnreachable;

        return clone;
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
