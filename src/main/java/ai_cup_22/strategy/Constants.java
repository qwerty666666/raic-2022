package ai_cup_22.strategy;

public class Constants {
    public static final int TICKS_PER_SECOND = 30;
    public static final double USER_RADIUS = 1;

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

    public static final double PF_OUT_OF_ZONE_MIN_SCORE = -300;
    public static final double PF_OUT_OF_ZONE_MAX_SCORE = -50;
    public static final double PF_OUT_OF_ZONE_DIST = 10;
    public static final double PF_ZONE_MIN_SCORE = PF_OUT_OF_ZONE_MAX_SCORE;
    public static final double PF_ZONE_MAX_SCORE = 0;
    public static final double PF_ZONE_DIST_TICKS = 30;
    public static final double PF_ZONE_MIN_THREAT_DIST = 10;


    public static final double PF_TREE_MIN_SCORE = -10;
    public static final double PF_TREE_DIST = 1.5;

    public static final double STRATEGY_EXPLORE_ORDER = 0.1;
    public static final double STRATEGY_MOVE_TO_PRIORITY_ENEMY_ORDER = STRATEGY_EXPLORE_ORDER + 0.01;
}
