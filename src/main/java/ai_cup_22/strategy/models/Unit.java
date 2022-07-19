package ai_cup_22.strategy.models;

import ai_cup_22.model.Sound;
import ai_cup_22.strategy.World;
import ai_cup_22.strategy.actions.basic.ActionBlockingAction;
import ai_cup_22.strategy.behaviourtree.BehaviourTree;
import ai_cup_22.strategy.geometry.Circle;
import ai_cup_22.strategy.geometry.CircleSegment;
import ai_cup_22.strategy.geometry.Line;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.geometry.Vector;
import ai_cup_22.strategy.potentialfield.PotentialField;
import ai_cup_22.strategy.potentialfield.UnitPotentialField;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Unit {
    public static final int TICKS_TO_RUN_ASIDE_BY_UNIT_RADIUS = 8;
    public static final double DEFAULT_SAFE_DIST = 15;

    private static int idGenerator = -10000000;

    private int id;
    private Circle circle;
    private ai_cup_22.model.Unit unit;
    private Vector direction;
    private UnitPotentialField potentialField;
    private List<Position> currentPath;
    private BehaviourTree behaviourTree = new BehaviourTree(this);
    private ActionBlockingAction lastAction;
    private boolean isAiming;
    private Position lookPosition;
    private boolean isPhantom;
    private int lastUpdateTick = -1;
    private Integer weapon;

    public Unit(int id) {
        this.id = id;
    }

    public Unit() {
        id = idGenerator++;
    }

    public void updateTick(ai_cup_22.model.Unit unit) {
        this.id = unit.getId();
        this.unit = unit;
        this.circle = new Circle(new Position(unit.getPosition()), World.getInstance().getConstants().getUnitRadius());
        this.direction = new Vector(unit.getDirection());
        this.currentPath = Collections.emptyList();
        if (this.lastAction != null) {
            lastAction.updateTick(this, unit.getAction());
        }
        this.isAiming = false;
        this.lookPosition = null;
        this.lastUpdateTick = World.getInstance().getCurrentTick();
        this.isPhantom = false;
        this.weapon = unit.getWeapon();

        if (isMe()) {
            if (this.potentialField == null) {
                this.potentialField = new UnitPotentialField(this);
            }
            this.potentialField.refresh();
        }
    }

    public void updateBySound(Sound sound) {
        this.circle = new Circle(new Position(sound.getPosition()), World.getInstance().getConstants().getUnitRadius());
        this.lastUpdateTick = World.getInstance().getCurrentTick();

        var soundProperties = World.getInstance().getConstants().getSounds()[sound.getTypeIndex()];
        if (soundProperties.getName().equals("Wand")) {
            weapon = Weapon.WAND_ID;
        } else if (soundProperties.getName().equals("Staff")) {
            weapon = Weapon.STAFF_ID;
        } else if (soundProperties.getName().equals("Bow")) {
            weapon = Weapon.BOW_ID;
        }

        isPhantom = true;
    }

    public void updateByBullet(Bullet bullet) {
        if (lastUpdateTick < bullet.getStartTick()) {
            this.circle = new Circle(bullet.getTrajectoryForFullLifetime().getStart(), World.getInstance().getConstants().getUnitRadius());
            this.lastUpdateTick = bullet.getStartTick();
            this.weapon = bullet.getWeaponId();
            this.isPhantom = true;
            this.id = bullet.getUnitId();
        }
    }

    public Unit setPhantom(boolean phantom) {
        isPhantom = phantom;
        return this;
    }

    public boolean isPhantom() {
        return isPhantom;
    }

    public boolean isSpawned() {
        return getRemainingSpawnTicks() <= 0;
    }

    public int getRemainingSpawnTicks() {
        if (unit == null) {
            return 0;
        }

        var remainingSpawnTime = unit.getRemainingSpawnTime() == null ? 0 : unit.getRemainingSpawnTime();

        return Math.max(0, lastUpdateTick + (int) (Math.ceil(remainingSpawnTime / World.getInstance().getTimePerTick())) -
                World.getInstance().getCurrentTick());
    }

    public int getLastUpdateTick() {
        return lastUpdateTick;
    }

    public int getTicksSinceLastUpdate() {
        return World.getInstance().getCurrentTick() - lastUpdateTick;
    }

    public Circle getPossibleLocationCircle() {
        return new Circle(getPosition(), getTicksSinceLastUpdate() * ai_cup_22.strategy.Constants.UNIT_MAX_SPEED_PER_TICK);
    }

    public Unit setLookPosition(Position lookPosition) {
        this.lookPosition = lookPosition;
        return this;
    }

    public Position getLookPosition() {
        return lookPosition;
    }

    public boolean isAiming() {
        return isAiming;
    }

    public Unit setAiming(boolean aiming) {
        isAiming = aiming;
        return this;
    }

    public BehaviourTree getBehaviourTree() {
        return behaviourTree;
    }

    public List<Position> getCurrentPath() {
        return currentPath;
    }

    public Unit setCurrentPath(List<Position> currentPath) {
        this.currentPath = currentPath;
        return this;
    }

    public PotentialField getPotentialField() {
        return potentialField;
    }

    public int getId() {
        return id;
    }

    public boolean isMe() {
        return unit.getPlayerId() == World.getInstance().getMyId();
    }

    public Position getPosition() {
        return circle.getCenter();
    }

    public Vector getDirection() {
        return direction;
    }

    public Vector getVelocityPerTick() {
        return new Vector(unit.getVelocity()).increase(1. / World.getInstance().getConstants().getTicksPerSecond());
    }

    public double getSpeed() {
        return getVelocityPerTick().getLength();
    }

    public double getMaxForwardSpeedPerTick() {
        return World.getInstance().getConstants().getMaxUnitForwardSpeed() *  World.getInstance().getTimePerTick();
    }

    public double getMaxBackwardSpeedPerTick() {
        return World.getInstance().getConstants().getMaxUnitBackwardSpeed() *  World.getInstance().getTimePerTick();
    }

    public Circle getCircle() {
        return circle;
    }

    public int getTicksToFullAim() {
        return getWeaponOptional().map(Weapon::getAimTicks).orElse(0);
    }

    public double getAimRotationSpeed() {
        return getWeaponOptional().map(Weapon::getAimRotationSpeed)
                .orElseGet(() -> World.getInstance().getConstants().getRotationSpeed());
    }

    public double getMaxSpeedPerTick() {
        return World.getInstance().getConstants().getMaxUnitForwardSpeed() * World.getInstance().getTimePerTick();
    }

    public int getRemainingCoolDownTicks() {
        return Math.max(0, unit.getNextShotTick() - World.getInstance().getCurrentTick());
    }

    public boolean isCoolDown() {
        return getRemainingCoolDownTicks() <= 0;
    }

    public boolean canSee(Position p) {
        return getViewSegment().contains(p);
    }

    public boolean canShoot(Position position, Unit targetUnit) {
        var line = new Line(getPosition(), position);

        return World.getInstance().getNonShootThroughObstacles().stream()
                .noneMatch(obstacle -> obstacle.getCircle().isIntersect(line))
                &&
                World.getInstance().getMyUnits().values().stream()
                        .filter(u -> u.getId() != this.getId() && u.isSpawned())
                        .noneMatch(u -> u.getCircle().isIntersect(line))
                &&
                World.getInstance().getEnemyUnits().values().stream()
                        .filter(u -> u.getId() != targetUnit.getId() && u.isSpawned())
                        .noneMatch(u -> u.getCircle().isIntersect(line));
    }

    public boolean canShoot(Unit enemy) {
        return canShoot(enemy.getPosition(), enemy);
    }

    public boolean hasWeapon() {
        return getWeapon() != null;
    }

    public int getBulletCount() {
        return hasWeapon() ? unit.getAmmo()[unit.getWeapon()] : 0;
    }

    public int getMaxBulletCount() {
        return hasWeapon() ? getWeapon().getMaxBulletCount() : 0;
    }

    public Weapon getWeapon() {
        if (weapon == null) {
            return null;
        }
        return Weapon.get(weapon);
    }

    public Optional<Weapon> getWeaponOptional() {
        return Optional.ofNullable(getWeapon());
    }

    public CircleSegment getShootingSegment() {
        return new CircleSegment(
                new Circle(getPosition(), World.getInstance().getConstants().getViewDistance()),
                direction.getAngle(),
                hasWeapon() ? Math.toRadians(getWeapon().getSpread()) : 0
        );
    }

    public CircleSegment getViewSegment() {
        var fieldOfView = Math.toRadians(World.getInstance().getConstants().getFieldOfView());
        var aimFieldOfView = hasWeapon() ? Math.toRadians(getWeapon().getAimFieldOfView()) : fieldOfView;

        return new CircleSegment(
                new Circle(getPosition(), World.getInstance().getConstants().getViewDistance()),
                direction.getAngle(),
                fieldOfView - (fieldOfView - aimFieldOfView) * getAim()
        );
    }

    public double getAim() {
        return unit.getAim();
    }

    public double getAimSpeedModifier() {
        return getWeaponOptional().map(Weapon::getAimSpeedModifier).orElse(0.);
    }

    public double getAimChangePerTick() {
        return getWeaponOptional().map(w -> World.getInstance().getTimePerTick() / w.getAimTime()).orElse(0.);
    }

    public double getDistanceTo(Unit u) {
        return u.getPosition().getDistanceTo(this.getPosition());
    }

    public boolean canTakeLoot(Loot loot) {
        return circle.contains(loot.getPosition());
    }

    public int getShieldPotions() {
        return unit.getShieldPotions();
    }

    public int getMaxShieldPotions() {
        return World.getInstance().getConstants().getMaxShieldPotionsInInventory();
    }

    public double getShield() {
        return unit.getShield();
    }

    public double getMaxShield() {
        return World.getInstance().getConstants().getMaxShield();
    }

    public boolean canDoNewAction() {
        return getTicksToNewActionBeAvailable() == 0;
    }

    public int getTicksToNewActionBeAvailable() {
        return lastAction == null ? 0 : lastAction.getFinishTick() - World.getInstance().getCurrentTick();
    }

    public double getHealth() {
        return unit.getHealth();
    }

    public int ticksToStartHealthRegeneration() {
        return Math.max(0, unit.getHealthRegenerationStartTick() - World.getInstance().getCurrentTick());
    }

    public boolean isRegeneratingHealth() {
        return getHealth() < getMaxHealth() && ticksToStartHealthRegeneration() == 0;
    }

    public double getMaxHealth() {
        return World.getInstance().getConstants().getUnitHealth();
    }

    public double getFullHealth() {
        return getShield() + getHealth();
    }

    public ActionBlockingAction getLastAction() {
        return lastAction;
    }

    public Unit setLastAction(ActionBlockingAction lastAction) {
        this.lastAction = lastAction;
        return this;
    }

    public double getThreatenDistanceFor(Unit unit) {
        return getWeaponOptional()
                .map(w -> {
                    if (w.isStaff()) {
                        return DEFAULT_SAFE_DIST;
                    }
                    return 2 + w.getSpeedPerTick() + w.getSpeedPerTick() * (TICKS_TO_RUN_ASIDE_BY_UNIT_RADIUS + 2);
                })
                .orElse(DEFAULT_SAFE_DIST);
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Unit) && ((Unit)obj).getId() == getId();
    }

    @Override
    public String toString() {
        return getId() + " " + getPosition();
    }
}
