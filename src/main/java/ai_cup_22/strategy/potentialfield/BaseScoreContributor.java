package ai_cup_22.strategy.potentialfield;

public abstract class BaseScoreContributor implements ScoreContributor {
    private final String contributionReason;
    private final boolean isStatic;

    public BaseScoreContributor(String contributionReason) {
        this(contributionReason, false);
    }

    public BaseScoreContributor(String contributionReason, boolean isStatic) {
        this.contributionReason = contributionReason;
        this.isStatic = isStatic;
    }

    @Override
    public String getContributionReason() {
        return contributionReason;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }
}
