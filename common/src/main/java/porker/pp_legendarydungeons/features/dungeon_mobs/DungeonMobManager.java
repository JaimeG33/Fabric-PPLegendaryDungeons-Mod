package porker.pp_legendarydungeons.features.dungeon_mobs;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;
import porker.pp_legendarydungeons.features.dungeon_factions.DungeonFactionService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Bounded runtime manager for vanilla mobs participating in dungeon factions.
 *
 * <p>Step 2 does not spawn these mobs from structure markers yet. Registration
 * is supplied by the persistent NBT mixin and by the public register method
 * that Step 3's datapackable spawner will call.</p>
 */
public final class DungeonMobManager {
    public static final String MANAGED_ENTITY_TAG = "pp_dungeon_mob";

    private static final int MIN_UPDATE_INTERVAL = 5;
    private static final int MAX_UPDATE_INTERVAL = 100;
    private static final double MAX_DETECTION_RANGE = 96.0D;
    private static final double MAX_CHASE_RANGE = 192.0D;
    private static final double MAX_HOME_RADIUS = 192.0D;

    private static final Map<UUID, DungeonMobRecord> RECORDS = new HashMap<>();
    private static boolean reportedMissingMixin = false;

    private DungeonMobManager() {
    }

    /**
     * Registers a newly created vanilla mob and stores the same encounter state
     * on the entity so it survives chunk unload and server restart.
     */
    public static boolean register(
            Mob mob,
            DungeonMobPersistentData data
    ) {
        if (mob == null
                || data == null
                || mob instanceof PokemonEntity
                || !(mob.level() instanceof ServerLevel level)) {
            return false;
        }

        if (!(mob instanceof DungeonMobDataHolder holder)) {
            if (!reportedMissingMixin) {
                reportedMissingMixin = true;
                LegendaryDungeons.LOGGER.error(
                        "Dungeon mob persistence mixin is unavailable; managed vanilla mobs cannot be registered"
                );
            }
            return false;
        }

        holder.ppLegendaryDungeons$setDungeonMobData(data);
        mob.addTag(MANAGED_ENTITY_TAG);
        mob.setPersistenceRequired();

        RECORDS.put(
                mob.getUUID(),
                new DungeonMobRecord(mob.getUUID(), level.dimension(), data)
        );
        return true;
    }

    /** Called by the Mob NBT mixin after a saved managed entity is restored. */
    public static void registerLoaded(
            Mob mob,
            DungeonMobPersistentData data
    ) {
        register(mob, data);
    }

    public static Optional<DungeonMobRecord> getRecord(UUID entityUuid) {
        return Optional.ofNullable(RECORDS.get(entityUuid));
    }

    public static void unregister(UUID entityUuid) {
        RECORDS.remove(entityUuid);
        VanillaMobAggressionBridge.forget(entityUuid);
    }

    public static void tick(MinecraftServer server, long tickCount) {
        Iterator<Map.Entry<UUID, DungeonMobRecord>> iterator =
                RECORDS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, DungeonMobRecord> entry = iterator.next();
            DungeonMobRecord record = entry.getValue();
            ServerLevel level = server.getLevel(record.dimension());

            if (level == null) {
                VanillaMobAggressionBridge.forget(record.entityUuid());
                iterator.remove();
                continue;
            }

            Entity resolved = level.getEntity(record.entityUuid());
            if (!(resolved instanceof Mob mob) || mob instanceof PokemonEntity) {
                VanillaMobAggressionBridge.forget(record.entityUuid());
                iterator.remove();
                continue;
            }

            if (!mob.isAlive()) {
                VanillaMobAggressionBridge.clearTarget(mob);
                iterator.remove();
                continue;
            }

            DungeonMobPersistentData data = record.data();
            DungeonMobPersistentData.Aggression aggression = data.aggression();
            int interval = clamp(
                    aggression.updateIntervalTicks(),
                    MIN_UPDATE_INTERVAL,
                    MAX_UPDATE_INTERVAL
            );
            int bucket = Math.floorMod(record.entityUuid().hashCode(), interval);

            if (Math.floorMod(tickCount, interval) != bucket) {
                continue;
            }

            updateOne(server, level, mob, data, aggression);
        }
    }

    private static void updateOne(
            MinecraftServer server,
            ServerLevel level,
            Mob mob,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        if (!aggression.enabled()) {
            return;
        }

        if (!instanceAllowsAggression(server, data, aggression)) {
            LivingEntity current = mob.getTarget();
            if (current != null && !isVanillaPlayerTarget(current, data, aggression)) {
                VanillaMobAggressionBridge.clearTarget(mob);
            }
            if (mob.getTarget() == null) {
                returnHomeIfNeeded(mob, data, aggression);
            }
            return;
        }

        LivingEntity target = mob.getTarget();
        if (!isValidTarget(level, mob, target, data, aggression)) {
            if (target != null) {
                VanillaMobAggressionBridge.clearTarget(mob);
            }
            target = findTarget(level, mob, data, aggression);
        }

        if (target != null) {
            VanillaMobAggressionBridge.setTarget(mob, target);
        } else {
            returnHomeIfNeeded(mob, data, aggression);
        }
    }

    private static boolean instanceAllowsAggression(
            MinecraftServer server,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        if (!aggression.requiresActiveDungeonInstance()) {
            return true;
        }

        if (data.instanceId().isBlank()) {
            return false;
        }

        return DungeonRuleSavedData
                .get(server)
                .get(data.instanceId())
                .map(instance -> instance.rulesAreActive())
                .orElse(false);
    }

    private static LivingEntity findTarget(
            ServerLevel level,
            Mob mob,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        double detectionRange = clamp(
                aggression.detectionRange(),
                0.0D,
                MAX_DETECTION_RANGE
        );

        if (detectionRange <= 0.0D) {
            return null;
        }

        List<LivingEntity> candidates = new ArrayList<>();

        if (shouldProactivelyTargetPlayer(data)) {
            for (ServerPlayer player : level.players()) {
                if (isValidPlayer(player, aggression)
                        && withinDetection(mob, player, detectionRange)
                        && withinChaseHome(player, data, aggression)) {
                    candidates.add(player);
                }
            }
        }

        if (DungeonFactionService.hasFactionEnemies(data.factionId())) {
            AABB box = mob.getBoundingBox().inflate(detectionRange);
            candidates.addAll(level.getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    candidate ->
                            candidate != mob
                                    && candidate.isAlive()
                                    && !DungeonFactionService.areAllies(mob, candidate)
                                    && DungeonFactionService.isHostileTo(
                                            data.factionId(),
                                            candidate
                                    )
                                    && withinChaseHome(candidate, data, aggression)
            ));
        }

        return candidates.stream()
                .min(Comparator.comparingDouble(candidate -> mob.distanceToSqr(candidate)))
                .orElse(null);
    }

    private static boolean isValidTarget(
            ServerLevel level,
            Mob mob,
            LivingEntity target,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        if (target == null
                || !target.isAlive()
                || target.level() != level
                || !withinChaseHome(target, data, aggression)) {
            return false;
        }

        if (target instanceof ServerPlayer player) {
            return (shouldProactivelyTargetPlayer(data)
                    || isVanillaPlayerRelation(data))
                    && isValidPlayer(player, aggression);
        }

        return !DungeonFactionService.areAllies(mob, target)
                && DungeonFactionService.isHostileTo(data.factionId(), target);
    }

    private static boolean isVanillaPlayerTarget(
            LivingEntity target,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        return target instanceof ServerPlayer player
                && isVanillaPlayerRelation(data)
                && isValidPlayer(player, aggression)
                && withinChaseHome(player, data, aggression);
    }

    private static boolean shouldProactivelyTargetPlayer(
            DungeonMobPersistentData data
    ) {
        return DungeonFactionService.PLAYER_RELATION_HOSTILE.equals(
                data.playerRelation()
        );
    }

    private static boolean isVanillaPlayerRelation(
            DungeonMobPersistentData data
    ) {
        return DungeonFactionService.PLAYER_RELATION_VANILLA.equals(
                data.playerRelation()
        );
    }

    private static boolean isValidPlayer(
            ServerPlayer player,
            DungeonMobPersistentData.Aggression aggression
    ) {
        if (!player.isAlive()) {
            return false;
        }
        if (aggression.excludeSpectators() && player.isSpectator()) {
            return false;
        }
        return !aggression.excludeCreative() || !player.isCreative();
    }

    private static boolean withinDetection(
            Mob mob,
            LivingEntity target,
            double detectionRange
    ) {
        return mob.distanceToSqr(target) <= detectionRange * detectionRange;
    }

    private static boolean withinChaseHome(
            LivingEntity target,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        double chaseRange = clamp(
                aggression.chaseRange(),
                0.0D,
                MAX_CHASE_RANGE
        );
        if (chaseRange <= 0.0D) {
            return false;
        }

        BlockPos home = data.homePosition();
        return target.distanceToSqr(
                home.getX() + 0.5D,
                home.getY() + 0.5D,
                home.getZ() + 0.5D
        ) <= chaseRange * chaseRange;
    }

    private static void returnHomeIfNeeded(
            Mob mob,
            DungeonMobPersistentData data,
            DungeonMobPersistentData.Aggression aggression
    ) {
        double homeRadius = clamp(
                aggression.homeRadius(),
                0.0D,
                MAX_HOME_RADIUS
        );
        BlockPos home = data.homePosition();
        double distanceSquared = mob.distanceToSqr(
                home.getX() + 0.5D,
                home.getY() + 0.5D,
                home.getZ() + 0.5D
        );

        if (distanceSquared <= homeRadius * homeRadius) {
            return;
        }

        PathNavigation navigation = mob.getNavigation();
        if (navigation.isDone()) {
            navigation.moveTo(
                    home.getX() + 0.5D,
                    home.getY(),
                    home.getZ() + 0.5D,
                    clamp(aggression.returnSpeed(), 0.1D, 3.0D)
            );
        }
    }

    public static void clear() {
        // Do not erase persistent entity NBT during shutdown. The runtime index
        // is rebuilt by the Mob load mixin when entities return.
        RECORDS.clear();
        VanillaMobAggressionBridge.clearAll();
        reportedMissingMixin = false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
