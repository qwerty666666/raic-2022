package ai_cup_22.strategy;

import ai_cup_22.model.Constants;
import ai_cup_22.model.Game;
import ai_cup_22.model.Item.ShieldPotions;
import ai_cup_22.model.Item.Weapon;
import ai_cup_22.strategy.models.AmmoLoot;
import ai_cup_22.strategy.models.Bullet;
import ai_cup_22.strategy.models.Loot;
import ai_cup_22.strategy.models.Obstacle;
import ai_cup_22.strategy.models.Unit;
import ai_cup_22.strategy.models.Zone;
import ai_cup_22.strategy.potentialfield.StaticPotentialField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class World {
    private static World instance;

    private final Constants constants;
    private final int myId;
    private int currentTick;

    private final Map<Integer, Obstacle> obstacles;
    private final List<Obstacle> nonShootThroughObstacles;

    private final Map<Integer, Unit> enemyUnits;
    private final Map<Integer, Unit> myUnits;
    private final Map<Integer, Unit> allUnits;

    private StaticPotentialField staticPotentialField;
    private Zone zone;

    private Map<Integer, Bullet> bullets = new HashMap<>();

    private Map<Integer, Loot> weaponLoots = new HashMap<>();
    private Map<Integer, Loot> shieldLoots = new HashMap<>();
    private Map<Integer, AmmoLoot> ammoLoots = new HashMap<>();

    private World(Constants constants, Game game) {
        this.constants = constants;
        this.myId = game.getMyId();

        this.obstacles = Arrays.stream(constants.getObstacles())
                .map(Obstacle::new)
                .collect(Collectors.toMap(Obstacle::getId, o -> o));
        this.nonShootThroughObstacles = obstacles.values().stream()
                .filter(o -> !o.isCanShootThrough())
                .toList();

        this.enemyUnits = new HashMap<>(game.getPlayers().length * constants.getTeamSize());
        this.myUnits = new HashMap<>();
        this.allUnits = new HashMap<>();

        this.zone = new Zone();
    }

    private void initFirstTick() {
        this.staticPotentialField = new StaticPotentialField(this);
    }

    public static void init(Constants constants, Game game) {
        instance = new World(constants, game);
        instance.initFirstTick();
    }

    public void updateTick(Game game) {
        this.currentTick = game.getCurrentTick();

        updateZone(game);
        updateUnits(game);
        updateBullets(game);
        updateLoot(game);
    }

    private void updateLoot(Game game) {
        var seeingLoots = new HashSet<Integer>();

        for (var l: game.getLoot()) {
            if (l.getItem() instanceof Weapon) {
                weaponLoots.computeIfAbsent(l.getId(), id -> new Loot(l));
            } else if (l.getItem() instanceof ShieldPotions) {
                shieldLoots.computeIfAbsent(l.getId(), id -> new Loot(l));
            } else {
                ammoLoots.computeIfAbsent(l.getId(), id -> new AmmoLoot(l));
            }

            seeingLoots.add(l.getId());
        }

        removeDisappearedLoots(weaponLoots, seeingLoots);
        removeDisappearedLoots(ammoLoots, seeingLoots);
        removeDisappearedLoots(shieldLoots, seeingLoots);

        removeLootsOutOfZone(weaponLoots);
        removeLootsOutOfZone(ammoLoots);
        removeLootsOutOfZone(shieldLoots);
    }

    private void removeDisappearedLoots(Map<Integer, ? extends Loot> loots, Set<Integer> seeingLoots) {
        var disappearedLoot = loots.values().stream()
                .filter(loot -> getMyUnits().values().stream().anyMatch(unit -> unit.canSee(loot.getPosition())))
                .filter(loot -> !seeingLoots.contains(loot.getId()))
                .toList();
        for (var loot: disappearedLoot) {
            loots.remove(loot.getId());
        }
    }

    private void removeLootsOutOfZone(Map<Integer, ? extends Loot> loots) {
        loots.values().stream()
                .filter(loot -> !zone.contains(loot.getPosition()))
                .map(Loot::getId)
                .toList()
                .forEach(loots::remove);
    }

    private void updateBullets(Game game) {
        var disappearedBullets = new HashSet<>(bullets.keySet());

        // update visible bullets
        for (var projectile: game.getProjectiles()) {
            var bullet = bullets.computeIfAbsent(projectile.getId(), id -> new Bullet(projectile));

            bullet.updateTick(projectile);

            disappearedBullets.remove(bullet.getId());
        }

        // simulate tick for bullets out of view field
        for (var id: disappearedBullets) {
            var bullet = bullets.get(id);

            bullet.simulateTick();

            // remove disappeared bullets
            if (bullet.getRemainingLifetimeTicks() <= 0) {
                bullets.remove(id);
                continue;
            }

            // remove bullets that are disparaged in view field
            // FIXME field view
            var isNewPositionVisible = getMyUnits().values().stream()
                    .anyMatch(unit -> unit.getViewSegment().contains(bullet.getPosition()));
            if (isNewPositionVisible) {
                bullets.remove(id);
                continue;
            }

            // remove bullets which have hit my units
            var isHitMe = getMyUnits().values().stream()
                    .anyMatch(unit -> unit.getCircle().isIntersect(bullet.getLastTickTrajectory()));
            if (isHitMe) {
                bullets.remove(id);
            }
        }
    }

    private void updateUnits(Game game) {
        var spottedUnitIds = Arrays.stream(game.getUnits())
                .map(u -> u.getId())
                .collect(Collectors.toSet());

        myUnits.entrySet().removeIf(e -> !spottedUnitIds.contains(e.getKey()));
        enemyUnits.entrySet().removeIf(e -> !spottedUnitIds.contains(e.getKey()));
        allUnits.entrySet().removeIf(e -> !spottedUnitIds.contains(e.getKey()));

        for (var u: game.getUnits()) {
            Unit unit = allUnits.computeIfAbsent(u.getId(), uu -> new Unit());

            if (u.getPlayerId() == getMyId()) {
                myUnits.put(u.getId(), unit);
            } else {
                enemyUnits.put(u.getId(), unit);
            }

            unit.updateTick(u);
        }
    }

    private void updateZone(Game game) {
        zone.updateTick(game.getZone());
    }

    public Constants getConstants() {
        return instance.constants;
    }

    public Zone getZone() {
        return zone;
    }

    public static World getInstance() {
        return instance;
    }

    public int getMyId() {
        return myId;
    }

    public double getTimePerTick() {
        return 1. / constants.getTicksPerSecond();
    }

    public Map<Integer, Obstacle> getObstacles() {
        return obstacles;
    }

    public Map<Integer, Unit> getEnemyUnits() {
        return enemyUnits;
    }

    public Map<Integer, Unit> getMyUnits() {
        return myUnits;
    }

    public List<Obstacle> getNonShootThroughObstacles() {
        return nonShootThroughObstacles;
    }

    public Map<Integer, Unit> getAllUnits() {
        return allUnits;
    }

    public StaticPotentialField getStaticPotentialField() {
        return staticPotentialField;
    }

    public Map<Integer, Bullet> getBullets() {
        return bullets;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Map<Integer, Loot> getWeaponLoots() {
        return weaponLoots;
    }

    public Map<Integer, Loot> getShieldLoots() {
        return shieldLoots;
    }

    public Map<Integer, AmmoLoot> getAmmoLoots() {
        return ammoLoots;
    }

    public List<AmmoLoot> getAmmoLoots(int weaponId) {
        return ammoLoots.values().stream()
                .filter(loot -> loot.getWeaponId() == weaponId)
                .toList();
    }
}
