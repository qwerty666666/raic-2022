package ai_cup_22.strategy.models.potentialfield;

import ai_cup_22.strategy.geometry.Position;

public class Score {
    private Position position;
    private double score;

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
}
