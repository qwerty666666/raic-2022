package ai_cup_22.strategy.potentialfield;

import ai_cup_22.strategy.geometry.Position;
import java.util.ArrayList;
import java.util.List;

public class Score {
    private Position position;
    private double score;
    private double initialScore;
    private List<Score> adjacent = new ArrayList<>();

    public Score(Position position) {
        this.position = position;
    }

    public void increaseScore(double delta) {
        score += delta;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Position getPosition() {
        return position;
    }

    public double getScore() {
        return score;
    }

    public void setInitialScore(double initialScore) {
        this.initialScore = initialScore;
    }

    public void reset() {
        this.score = initialScore;
    }

    public List<Score> getAdjacent() {
        return adjacent;
    }

    public void addAdjacent(Score score) {
        adjacent.add(score);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Score s && position.equals(s.getPosition());
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        return position + " -> " + score;
    }
}
