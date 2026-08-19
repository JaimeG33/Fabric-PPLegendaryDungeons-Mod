package porker.pp_legendarydungeons.features.dungeon_pokemon;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import porker.pp_legendarydungeons.LegendaryDungeons;
import porker.pp_legendarydungeons.dungeon_rules.instance.DungeonRuleSavedData;
import porker.pp_legendarydungeons.features.dungeon_factions.DungeonFactionService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * One bounded lifecycle manager for all loaded dungeon Pokémon.
 *
 * Persistence is written into the Pokémon entity's own NBT by
 * DungeonPokemonEventRegistrar. This runtime map intentionally tracks loaded
 * entities only; a later load event restores the record after chunk reload.
 */
public final class DungeonPokemonManager {
    public static final String MANAGED_ENTITY_TAG = "pp_dungeon_pokemon";

    private static final int MIN_UPDATE_INTERVAL = 5;
    private static final int MAX_UPDATE_INTERVAL = 100;

    private static final double MAX_DETECTION_RANGE = 96.0D;
    private static final double MAX_CHASE_RANGE = 192.0D;
    private static final double MAX_HOME_RADIUS = 192.0D;

    private static final Map<UUID, DungeonPokemonRecord> RECORDS =
            new HashMap<>();
    private static final Set<String> REPORTED_EFFECT_LOOKUP_FAILURES =
            new HashSet<>();
    private static final Set<String> REPORTED_EFFECT_APPLICATION_FAILURES =
            new HashSet<>();

    private DungeonPokemonManager() {
    }

    public static void register(
            PokemonEntity entity,
            ResourceLocation profileId,
            BlockPos homePosition,
            String instanceId
    ) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        entity.addTag(MANAGED_ENTITY_TAG);
        entity.setPersistenceRequired();
        DungeonPokemonDeathEffectBridge.resetForRegistration(entity);

        DungeonPokemonRecord record = new DungeonPokemonRecord(
                entity.getUUID(),
                entity.getPokemon().getUuid(),
                level.dimension(),
                profileId,
                homePosition.immutable(),
                instanceId == null ? "" : instanceId
        );

        RECORDS.put(entity.getUUID(), record);
    }

    public static Optional<DungeonPokemonRecord> getRecord(UUID entityUuid) {
        return Optional.ofNullable(RECORDS.get(entityUuid));
    }

    public static Optional<DungeonPokemonRecord> getRecordByPokemonUuid(
            UUID pokemonUuid
    ) {
        if (pokemonUuid == null) {
            return Optional.empty();
        }

        return RECORDS.values().stream()
                .filter(record -> record.pokemonUuid().equals(pokemonUuid))
                .findFirst();
    }

    public static void unregister(UUID entityUuid) {
        RECORDS.remove(entityUuid);
        DungeonPokemonDeathEffectBridge.clear(entityUuid);
    }

    public static void onCaptured(
            MinecraftServer server,
            UUID pokemonUuid
    ) {
        Iterator<Map.Entry<UUID, DungeonPokemonRecord>> iterator =
                RECORDS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, DungeonPokemonRecord> entry = iterator.next();
            DungeonPokemonRecord record = entry.getValue();

            if (!record.pokemonUuid().equals(pokemonUuid)) {
                continue;
            }

            ServerLevel level = server.getLevel(record.dimension());

            if (level != null) {
                Entity entity = level.getEntity(record.entityUuid());

                if (entity instanceof PokemonEntity pokemon) {
                    clearProfileEffects(pokemon, record.profileId());
                    CobblemonAggressionBridge.clearTarget(pokemon);
                    pokemon.removeTag(MANAGED_ENTITY_TAG);
                }
            }

            DungeonPokemonDeathEffectBridge.clear(record.entityUuid());
            iterator.remove();
        }
    }

    public static void tick(
            MinecraftServer server,
            long tickCount
    ) {
        Iterator<Map.Entry<UUID, DungeonPokemonRecord>> iterator =
                RECORDS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, DungeonPokemonRecord> entry = iterator.next();
            DungeonPokemonRecord record = entry.getValue();
            ServerLevel level = server.getLevel(record.dimension());

            if (level == null) {
                DungeonPokemonDeathEffectBridge.clear(record.entityUuid());
                iterator.remove();
                continue;
            }

            Entity resolved = level.getEntity(record.entityUuid());

            /*
             * An unloaded entity will be restored by POKEMON_ENTITY_LOAD using
             * the NBT written on unload. The runtime record can be released now.
             */
            if (!(resolved instanceof PokemonEntity pokemon)) {
                DungeonPokemonDeathEffectBridge.clear(record.entityUuid());
                iterator.remove();
                continue;
            }

            if (pokemon.getPokemon().isPlayerOwned()
                    || pokemon.getOwnerUUID() != null) {
                clearProfileEffects(pokemon, record.profileId());
                CobblemonAggressionBridge.clearTarget(pokemon);
                DungeonPokemonDeathEffectBridge.clear(record.entityUuid());
                iterator.remove();
                continue;
            }

            /*
             * Cobblemon 1.7.3 normally generates drops at the beginning of its
             * 60-tick death sequence. Observe dead managed entities every post
             * server tick so armed callbacks execute at tick 59, immediately
             * before Cobblemon removes the entity at tick 60.
             */
            if (!pokemon.isAlive()) {
                CobblemonAggressionBridge.clearTarget(pokemon);
                DungeonPokemonDeathEffectBridge.observeDeathAndTick(pokemon);
                continue;
            }

            Optional<DungeonPokemonProfileJson> profileOptional =
                    DungeonPokemonProfileRegistry.get(record.profileId());

            if (profileOptional.isEmpty()) {
                CobblemonAggressionBridge.clearTarget(pokemon);
                DungeonPokemonDeathEffectBridge.clear(record.entityUuid());
                iterator.remove();
                continue;
            }

            DungeonPokemonProfileJson profile = profileOptional.get();
            DungeonPokemonProfileJson.Aggression aggression =
                    profile.aggression == null
                            ? new DungeonPokemonProfileJson.Aggression()
                            : profile.aggression;

            int interval = clamp(
                    aggression.update_interval_ticks,
                    MIN_UPDATE_INTERVAL,
                    MAX_UPDATE_INTERVAL
            );

            int bucket = Math.floorMod(record.entityUuid().hashCode(), interval);

            if (Math.floorMod(tickCount, interval) != bucket) {
                continue;
            }

            updateOne(
                    server,
                    level,
                    pokemon,
                    record,
                    profile,
                    aggression,
                    tickCount
            );
        }
    }

    private static void updateOne(
            MinecraftServer server,
            ServerLevel level,
            PokemonEntity pokemon,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson profile,
            DungeonPokemonProfileJson.Aggression aggression,
            long tickCount
    ) {
        if (!aggression.enabled
                || !instanceAllowsAggression(server, record, aggression)
                || pokemon.isBattling()) {
            CobblemonAggressionBridge.clearTarget(pokemon);
            returnHomeIfNeeded(pokemon, record, aggression);
            return;
        }

        LivingEntity target = pokemon.getTarget();

        if (!isValidTarget(
                level,
                pokemon,
                target,
                record,
                profile,
                aggression
        )) {
            if (target != null) {
                CobblemonAggressionBridge.clearTarget(pokemon);
            }

            target = findTarget(level, pokemon, record, profile, aggression);
        }

        if (target != null) {
            /*
             * Combat effects are independent of whether Cobblemon or an optional
             * AI mod currently marks the entity busy. Attack animations and move
             * execution can hold a busy lock for most of an encounter, so gating
             * effects on isBusy() can prevent them from ever being applied.
             */
            refreshCombatEffects(
                    level,
                    pokemon,
                    record.profileId(),
                    profile
            );

            if (!pokemon.isBusy()) {
                /*
                 * Fight or Flight and other AI mods may clear only Brain memory
                 * while leaving the inherited target reference. Refreshing the
                 * bridge at this bounded cadence keeps dungeon intent authoritative
                 * without interrupting an active Cobblemon task.
                 */
                CobblemonAggressionBridge.setTarget(pokemon, target);
            }
        } else {
            returnHomeIfNeeded(pokemon, record, aggression);
        }
    }

    private static boolean instanceAllowsAggression(
            MinecraftServer server,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        if (!aggression.requires_active_dungeon_instance) {
            return true;
        }

        if (record.instanceId() == null || record.instanceId().isBlank()) {
            return false;
        }

        return DungeonRuleSavedData
                .get(server)
                .get(record.instanceId())
                .map(instance -> instance.rulesAreActive())
                .orElse(false);
    }

    private static LivingEntity findTarget(
            ServerLevel level,
            PokemonEntity pokemon,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson profile,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        double detectionRange = clamp(
                aggression.detection_range,
                0.0D,
                MAX_DETECTION_RANGE
        );

        if (detectionRange <= 0.0D) {
            return null;
        }

        List<LivingEntity> candidates = new ArrayList<>();

        if (DungeonFactionService.shouldTargetPlayer(profile, aggression)) {
            for (ServerPlayer player : level.players()) {
                if (isValidPlayer(player, aggression)
                        && withinDetection(pokemon, player, detectionRange)
                        && withinChaseHome(player, record, aggression)) {
                    candidates.add(player);
                }
            }
        }

        List<DungeonPokemonProfileJson.TargetRule> nonPlayerRules =
                nonPlayerRules(profile);

        if (!nonPlayerRules.isEmpty()
                || DungeonFactionService.hasFactionEnemies(profile)) {
            AABB box = pokemon.getBoundingBox().inflate(detectionRange);

            candidates.addAll(level.getEntitiesOfClass(
                    LivingEntity.class,
                    box,
                    candidate ->
                            candidate != pokemon
                                    && candidate.isAlive()
                                    && !DungeonFactionService.areAllies(pokemon, candidate)
                                    && (matchesAnyNonPlayerRule(candidate, nonPlayerRules)
                                            || DungeonFactionService.isHostileTo(
                                                    profile,
                                                    candidate
                                            ))
                                    && withinChaseHome(candidate, record, aggression)
            ));
        }

        return candidates.stream()
                .min(Comparator.comparingDouble(candidate -> pokemon.distanceToSqr(candidate)))
                .orElse(null);
    }

    private static boolean isValidTarget(
            ServerLevel level,
            PokemonEntity pokemon,
            LivingEntity target,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson profile,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        if (target == null
                || !target.isAlive()
                || target.level() != level
                || !withinChaseHome(target, record, aggression)) {
            return false;
        }

        if (target instanceof ServerPlayer player) {
            return DungeonFactionService.shouldTargetPlayer(profile, aggression)
                    && isValidPlayer(player, aggression);
        }

        return !DungeonFactionService.areAllies(pokemon, target)
                && (matchesAnyNonPlayerRule(target, nonPlayerRules(profile))
                        || DungeonFactionService.isHostileTo(profile, target));
    }

    private static boolean isValidPlayer(
            ServerPlayer player,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        if (!player.isAlive()) {
            return false;
        }

        if (aggression.exclude_spectators && player.isSpectator()) {
            return false;
        }

        return !aggression.exclude_creative || !player.isCreative();
    }

    private static boolean withinDetection(
            PokemonEntity pokemon,
            LivingEntity target,
            double detectionRange
    ) {
        return pokemon.distanceToSqr(target)
                <= detectionRange * detectionRange;
    }

    private static boolean withinChaseHome(
            LivingEntity target,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        double chaseRange = clamp(
                aggression.chase_range,
                0.0D,
                MAX_CHASE_RANGE
        );

        if (chaseRange <= 0.0D) {
            return false;
        }

        BlockPos home = record.homePosition();

        return target.distanceToSqr(
                home.getX() + 0.5D,
                home.getY() + 0.5D,
                home.getZ() + 0.5D
        ) <= chaseRange * chaseRange;
    }

    private static boolean hasPlayerRule(DungeonPokemonProfileJson profile) {
        if (profile.targets == null) {
            return false;
        }

        return profile.targets.stream()
                .filter(java.util.Objects::nonNull)
                .map(rule -> normalized(rule.type))
                .anyMatch("player"::equals);
    }

    private static List<DungeonPokemonProfileJson.TargetRule> nonPlayerRules(
            DungeonPokemonProfileJson profile
    ) {
        if (profile.targets == null || profile.targets.isEmpty()) {
            return List.of();
        }

        return profile.targets.stream()
                .filter(java.util.Objects::nonNull)
                .filter(rule -> !normalized(rule.type).equals("player"))
                .toList();
    }

    private static boolean matchesAnyNonPlayerRule(
            LivingEntity candidate,
            List<DungeonPokemonProfileJson.TargetRule> rules
    ) {
        for (DungeonPokemonProfileJson.TargetRule rule : rules) {
            String type = normalized(rule.type);
            String value = rule.value == null ? "" : rule.value.trim();

            switch (type) {
                case "entity_tag" -> {
                    if (candidate.getTags().contains(value)) {
                        return true;
                    }
                }
                case "scoreboard_team" -> {
                    if (candidate.getTeam() != null
                            && candidate.getTeam().getName().equals(value)) {
                        return true;
                    }
                }
                case "entity_type_tag" -> {
                    try {
                        ResourceLocation id = ResourceLocation.parse(value);
                        TagKey<EntityType<?>> tag =
                                TagKey.create(Registries.ENTITY_TYPE, id);

                        if (candidate.getType().is(tag)) {
                            return true;
                        }
                    } catch (Exception ignored) {
                        // Invalid IDs were already rejected by profile validation.
                    }
                }
                default -> {
                    // Unknown types were already rejected by validation.
                }
            }
        }

        return false;
    }

    private static void returnHomeIfNeeded(
            PokemonEntity pokemon,
            DungeonPokemonRecord record,
            DungeonPokemonProfileJson.Aggression aggression
    ) {
        double homeRadius = clamp(
                aggression.home_radius,
                0.0D,
                MAX_HOME_RADIUS
        );

        BlockPos home = record.homePosition();
        double distanceSquared = pokemon.distanceToSqr(
                home.getX() + 0.5D,
                home.getY() + 0.5D,
                home.getZ() + 0.5D
        );

        if (distanceSquared <= homeRadius * homeRadius) {
            return;
        }

        PathNavigation navigation = pokemon.getNavigation();

        if (navigation.isDone()) {
            navigation.moveTo(
                    home.getX() + 0.5D,
                    home.getY(),
                    home.getZ() + 0.5D,
                    clamp(aggression.return_speed, 0.1D, 3.0D)
            );
        }
    }

    private static void refreshCombatEffects(
            ServerLevel level,
            PokemonEntity pokemon,
            ResourceLocation profileId,
            DungeonPokemonProfileJson profile
    ) {
        if (profile.combat_effects == null
                || profile.combat_effects.isEmpty()) {
            return;
        }

        for (DungeonPokemonProfileJson.CombatEffect configured :
                profile.combat_effects) {
            if (configured == null) {
                continue;
            }

            int refreshInterval = clamp(
                    configured.refresh_interval_ticks,
                    1,
                    1200
            );
            int configuredAmplifier = clamp(
                    configured.amplifier,
                    0,
                    255
            );

            Optional<Holder.Reference<MobEffect>> effect =
                    effectHolder(level, configured.effect);

            if (effect.isEmpty()) {
                continue;
            }

            MobEffectInstance existing = pokemon.getEffect(effect.get());

            if (existing != null) {
                DungeonPokemonDeathEffectBridge.armConfiguredEffect(
                        pokemon,
                        profileId,
                        configured,
                        configuredAmplifier
                );

                if (existing.getAmplifier() >= configuredAmplifier
                        && existing.getDuration() > refreshInterval + 5) {
                    continue;
                }
            }

            boolean applied = pokemon.addEffect(new MobEffectInstance(
                    effect.get(),
                    clamp(configured.duration_ticks, 1, 72_000),
                    configuredAmplifier,
                    configured.ambient,
                    configured.show_particles,
                    configured.show_icon
            ));

            if (pokemon.getEffect(effect.get()) != null) {
                DungeonPokemonDeathEffectBridge.armConfiguredEffect(
                        pokemon,
                        profileId,
                        configured,
                        configuredAmplifier
                );
            }

            String reportKey =
                    profileId + "|" + pokemon.getUUID() + "|" + configured.effect;

            if (applied) {
                REPORTED_EFFECT_APPLICATION_FAILURES.remove(reportKey);
                LegendaryDungeons.LOGGER.debug(
                        "[Dungeon Pokemon Effects] Applied {} amplifier={} to entity={} profile={}.",
                        configured.effect,
                        configuredAmplifier,
                        pokemon.getUUID(),
                        profileId
                );
            } else if (REPORTED_EFFECT_APPLICATION_FAILURES.add(reportKey)) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Effects] Entity {} rejected effect {} for profile {}.",
                        pokemon.getUUID(),
                        configured.effect,
                        profileId
                );
            }
        }
    }

    private static void clearProfileEffects(
            PokemonEntity pokemon,
            ResourceLocation profileId
    ) {
        if (!(pokemon.level() instanceof ServerLevel level)) {
            return;
        }

        DungeonPokemonProfileRegistry.get(profileId).ifPresent(profile -> {
            if (profile.combat_effects == null) {
                return;
            }

            for (DungeonPokemonProfileJson.CombatEffect configured :
                    profile.combat_effects) {
                if (configured == null) {
                    continue;
                }

                effectHolder(level, configured.effect)
                        .ifPresent(pokemon::removeEffect);
            }
        });
    }

    private static Optional<Holder.Reference<MobEffect>> effectHolder(
            ServerLevel level,
            String effectId
    ) {
        String reportKey =
                effectId == null || effectId.isBlank()
                        ? "<blank>"
                        : effectId.trim();

        try {
            ResourceLocation id = ResourceLocation.parse(reportKey);
            Optional<Holder.Reference<MobEffect>> holder =
                    level.registryAccess()
                            .registryOrThrow(Registries.MOB_EFFECT)
                            .getHolder(id);

            if (holder.isEmpty()) {
                if (REPORTED_EFFECT_LOOKUP_FAILURES.add(reportKey)) {
                    LegendaryDungeons.LOGGER.warn(
                            "[Dungeon Pokemon Effects] Unknown mob effect {} in the active server registry.",
                            reportKey
                    );
                }
            } else {
                REPORTED_EFFECT_LOOKUP_FAILURES.remove(reportKey);
            }

            return holder;
        } catch (Exception exception) {
            if (REPORTED_EFFECT_LOOKUP_FAILURES.add(reportKey)) {
                LegendaryDungeons.LOGGER.warn(
                        "[Dungeon Pokemon Effects] Failed to resolve mob effect {}: {}",
                        reportKey,
                        exception.toString()
                );
            }

            return Optional.empty();
        }
    }

    private static String normalized(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void clear() {
        RECORDS.clear();
        REPORTED_EFFECT_LOOKUP_FAILURES.clear();
        REPORTED_EFFECT_APPLICATION_FAILURES.clear();
        DungeonPokemonDeathEffectBridge.clearAll();
    }

    public static int loadedCount() {
        return RECORDS.size();
    }
}
