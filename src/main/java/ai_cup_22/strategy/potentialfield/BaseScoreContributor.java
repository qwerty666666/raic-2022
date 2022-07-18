package ai_cup_22.strategy.potentialfield;

public abstract class BaseScoreContributor implements ScoreContributor {
    protected final String contributionReason;
    protected final boolean isStatic;

    public BaseScoreContributor(String contributionReason) {
        this(contributionReason, false);
    }

    public BaseScoreContributor(String contributionReason, boolean isStatic) {
        this.contributionReason = contributionReason;
        this.isStatic = isStatic;
    }

    @Override
    public String getContributionReason(Score score) {
        return contributionReason;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }
}
