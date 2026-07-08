package porker.pp_legendarydungeons.items.maps_findrandom;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

/**
 * One possible specific structure that a random map can resolve into.
 *
 * structureTagId must be a structure tag id, not a direct structure id.
 */
public record RandomMapTarget(
        String id,
        String displayName,
        String structureTagId,
        List<Component> descriptionLines
) {
    public TagKey<Structure> structureTag() {
        return TagKey.create(Registries.STRUCTURE, ResourceLocation.parse(structureTagId));
    }
}
