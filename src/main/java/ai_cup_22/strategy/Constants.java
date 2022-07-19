package ai_cup_22.strategy;

public class Constants {
    public static final double UNIT_MAX_SPEED_PER_TICK = 1./3;
    public static final double SAFE_DIST = 30;
    public static final double MAX_LOOT_STRATEGY_DIST = 100;

    public static final int PHANTOM_UNIT_LIFE_MAX_TICKS = 400;

    public static final double PF_ENEMY_THREATEN_DIST_MIN_SCORE = ai_cup_22.strategy.potentialfield.PotentialField.MIN_VALUE;
    public static final double PF_ENEMY_THREATEN_DIST_MAX_SCORE = -20;

    public static final double PF_ENEMY_HOLD_DISTANCE_MAX_SCORE = 50;
    public static final double PF_ENEMY_HOLD_DISTANCE_MIN_SCORE = 30;
    public static final double PF_ENEMY_HOLD_DISTANCE_DIST = 3;

    public static final double PF_NON_TARGET_ENEMY_MIN_SCORE = PF_ENEMY_THREATEN_DIST_MIN_SCORE;
    public static final double PF_NON_TARGET_ENEMY_MAX_SCORE = PF_ENEMY_THREATEN_DIST_MAX_SCORE;

    public static final double PF_PHANTOM_ENEMY_MIN_SCORE = PF_ENEMY_THREATEN_DIST_MIN_SCORE;
    public static final double PF_PHANTOM_ENEMY_MAX_SCORE = PF_ENEMY_THREATEN_DIST_MAX_SCORE;

    public static final double PF_ALLY_MIN_SCORE = -40;
    public static final double PF_ALLY_MAX_SCORE = -20;
    public static final double PF_ALLY_DIST = 3;

    public static final double PF_RETREAT_ENEMY_DIST = 44;
}
