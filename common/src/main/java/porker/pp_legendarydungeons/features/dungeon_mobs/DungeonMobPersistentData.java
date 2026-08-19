package porker.pp_legendarydungeons.features.dungeon_mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.features.dungeon_factions.DungeonFactionService;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Persistent encounter state stored directly in managed vanilla-mob NBT.
 *
 * <p>Step 2 deliberately stores a snapshot of the small amount of behavior the
 * runtime manager needs. Step 3 will create datapackable mob profiles and use
 * them to construct this same data object when a structure marker spawns a
 * managed mob.</p>
 */
public record DungeonMobPersistentData(
        ResourceLocation profileId,
        BlockPos homePosition,
        ResourceLocation factionId,
        String playerRelation,
        String instanceId,
        Aggression aggression
) {
    public static final String ROOT_NBT_KEY =
            "pp_legendarydungeons:DungeonMob";

    private static final ResourceLocation MANUAL_PROFILE =
            ResourceLocation.parse("pp_legendarydungeons:manual");

    public DungeonMobPersistentData {
        profileId = profileId == null ? MANUAL_PROFILE : profileId;
        homePosition = homePosition == null
                ? BlockPos.ZERO
                : homePosition.immutable();
        factionId = Objects.requireNonNull(factionId, "factionId");
        playerRelation = normalizePlayerRelation(playerRelation);
        instanceId = instanceId == null ? "" : instanceId.trim();
        aggression = aggression == null ? Aggression.defaults() : aggression;
    }

    /**
     * Returns an equivalent immutable snapshot with a resolved encounter home.
     * Used when command-spawn NBT is read before Minecraft applies the final
     * summon coordinates.
     */
    public DungeonMobPersistentData withHomePosition(BlockPos resolvedHome) {
        return new DungeonMobPersistentData(
                profileId,
                resolvedHome,
                factionId,
                playerRelation,
                instanceId,
                aggression
        );
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        root.putString("Profile", profileId.toString());
        root.putString("Faction", factionId.toString());
        root.putString("PlayerRelation", playerRelation);
        root.putString("Instance", instanceId);

        CompoundTag home = new CompoundTag();
        home.putInt("X", homePosition.getX());
        home.putInt("Y", homePosition.getY());
        home.putInt("Z", homePosition.getZ());
        root.put("Home", home);

        root.put("Aggression", aggression.save());
        return root;
    }

    public static Optional<DungeonMobPersistentData> load(
            CompoundTag root,
            BlockPos fallbackHome
    ) {
        if (root == null) {
            return Optional.empty();
        }

        String factionValue = root.getString("Faction");
        if (factionValue == null || factionValue.isBlank()) {
            return Optional.empty();
        }

        try {
            ResourceLocation faction = ResourceLocation.parse(factionValue.trim());
            ResourceLocation profile = MANUAL_PROFILE;

            String profileValue = root.getString("Profile");
            if (profileValue != null && !profileValue.isBlank()) {
                profile = ResourceLocation.parse(profileValue.trim());
            }

            BlockPos home = fallbackHome == null
                    ? BlockPos.ZERO
                    : fallbackHome.immutable();

            if (root.contains("Home")) {
                CompoundTag homeTag = root.getCompound("Home");
                home = new BlockPos(
                        homeTag.getInt("X"),
                        homeTag.getInt("Y"),
                        homeTag.getInt("Z")
                );
            }

            String relation = root.getString("PlayerRelation");
            String instance = root.getString("Instance");
            Aggression aggression = root.contains("Aggression")
                    ? Aggression.load(root.getCompound("Aggression"))
                    : Aggression.defaults();

            return Optional.of(new DungeonMobPersistentData(
                    profile,
                    home,
                    faction,
                    relation,
                    instance,
                    aggression
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static String normalizePlayerRelation(String value) {
        String normalized = value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case DungeonFactionService.PLAYER_RELATION_HOSTILE,
                 DungeonFactionService.PLAYER_RELATION_NEUTRAL,
                 DungeonFactionService.PLAYER_RELATION_FACTION_RETALIATORY,
                 DungeonFactionService.PLAYER_RELATION_VANILLA -> normalized;
            default -> DungeonFactionService.PLAYER_RELATION_NEUTRAL;
        };
    }

    public record Aggression(
            boolean enabled,
            boolean excludeCreative,
            boolean excludeSpectators,
            boolean requiresActiveDungeonInstance,
            double detectionRange,
            double chaseRange,
            double homeRadius,
            double returnSpeed,
            int updateIntervalTicks
    ) {
        public static Aggression defaults() {
            return new Aggression(
                    true,
                    true,
                    true,
                    false,
                    32.0D,
                    48.0D,
                    32.0D,
                    1.0D,
                    10
            );
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Enabled", enabled);
            tag.putBoolean("ExcludeCreative", excludeCreative);
            tag.putBoolean("ExcludeSpectators", excludeSpectators);
            tag.putBoolean(
                    "RequiresActiveDungeonInstance",
                    requiresActiveDungeonInstance
            );
            tag.putDouble("DetectionRange", detectionRange);
            tag.putDouble("ChaseRange", chaseRange);
            tag.putDouble("HomeRadius", homeRadius);
            tag.putDouble("ReturnSpeed", returnSpeed);
            tag.putInt("UpdateIntervalTicks", updateIntervalTicks);
            return tag;
        }

        private static Aggression load(CompoundTag tag) {
            Aggression defaults = defaults();

            return new Aggression(
                    booleanOrDefault(tag, "Enabled", defaults.enabled),
                    booleanOrDefault(
                            tag,
                            "ExcludeCreative",
                            defaults.excludeCreative
                    ),
                    booleanOrDefault(
                            tag,
                            "ExcludeSpectators",
                            defaults.excludeSpectators
                    ),
                    booleanOrDefault(
                            tag,
                            "RequiresActiveDungeonInstance",
                            defaults.requiresActiveDungeonInstance
                    ),
                    doubleOrDefault(
                            tag,
                            "DetectionRange",
                            defaults.detectionRange
                    ),
                    doubleOrDefault(tag, "ChaseRange", defaults.chaseRange),
                    doubleOrDefault(tag, "HomeRadius", defaults.homeRadius),
                    doubleOrDefault(tag, "ReturnSpeed", defaults.returnSpeed),
                    intOrDefault(
                            tag,
                            "UpdateIntervalTicks",
                            defaults.updateIntervalTicks
                    )
            );
        }

        private static boolean booleanOrDefault(
                CompoundTag tag,
                String key,
                boolean fallback
        ) {
            return tag.contains(key) ? tag.getBoolean(key) : fallback;
        }

        private static double doubleOrDefault(
                CompoundTag tag,
                String key,
                double fallback
        ) {
            return tag.contains(key) ? tag.getDouble(key) : fallback;
        }

        private static int intOrDefault(
                CompoundTag tag,
                String key,
                int fallback
        ) {
            return tag.contains(key) ? tag.getInt(key) : fallback;
        }
    }
}
