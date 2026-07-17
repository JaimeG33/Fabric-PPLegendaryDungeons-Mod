package porker.pp_legendarydungeons.features.wtraders.json;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import porker.pp_legendarydungeons.LegendaryDungeons;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validation for datapack-defined functional-map targets and random-map groups.
 */
public final class MapMetadataJsonValidator {
    private MapMetadataJsonValidator() {
    }

    public static boolean validateMapTarget(
            ResourceLocation fileId,
            MapTargetJson target
    ) {
        if (target == null) {
            warn(fileId, "Map target JSON parsed to null.");
            return false;
        }

        if (!requireResourceLocation(fileId, target.id, "map target id")) {
            return false;
        }

        if (WTraderJsonValues.isBlank(target.displayName)) {
            warn(fileId, "Map target {} is missing display_name.", target.id);
            return false;
        }

        if (!validateDescriptionLines(
                fileId,
                target.id,
                target.descriptionLinesOrEmpty()
        )) {
            return false;
        }

        Set<String> uniqueGroups = new HashSet<>();

        for (String groupId : target.groupsOrEmpty()) {
            if (!requireResourceLocation(fileId, groupId, "group id")) {
                return false;
            }

            if (!uniqueGroups.add(groupId)) {
                warn(fileId, "Map target {} repeats group {}.", target.id, groupId);
                return false;
            }
        }

        if (!target.groupsOrEmpty().isEmpty()
                && WTraderJsonValues.isBlank(target.structureTag)) {
            warn(
                    fileId,
                    "Map target {} belongs to random groups but is missing structure_tag.",
                    target.id
            );
            return false;
        }

        if (!WTraderJsonValues.isBlank(target.structureTag)
                && !requireResourceLocation(
                        fileId,
                        target.structureTag,
                        "structure_tag"
                )) {
            return false;
        }

        return validateAliases(fileId, target.id, target.aliasesOrEmpty());
    }

    public static boolean validateMapGroup(
            ResourceLocation fileId,
            MapGroupJson group
    ) {
        if (group == null) {
            warn(fileId, "Map group JSON parsed to null.");
            return false;
        }

        if (!requireResourceLocation(fileId, group.id, "map group id")) {
            return false;
        }

        if (WTraderJsonValues.isBlank(group.displayName)) {
            warn(fileId, "Map group {} is missing display_name.", group.id);
            return false;
        }

        if (!validateDescriptionLines(
                fileId,
                group.id,
                group.descriptionLinesOrEmpty()
        )) {
            return false;
        }

        return validateAliases(fileId, group.id, group.aliasesOrEmpty());
    }

    private static boolean validateDescriptionLines(
            ResourceLocation fileId,
            String ownerId,
            List<MapDescriptionLineJson> lines
    ) {
        if (lines.isEmpty()) {
            warn(fileId, "{} has no description_lines.", ownerId);
            return false;
        }

        for (int index = 0; index < lines.size(); index++) {
            MapDescriptionLineJson line = lines.get(index);

            if (line == null || WTraderJsonValues.isBlank(line.text)) {
                warn(fileId, "{} has an empty description line at index {}.", ownerId, index);
                return false;
            }

            if (!WTraderJsonValues.isBlank(line.color)) {
                ChatFormatting formatting = ChatFormatting.getByName(line.color);

                if (formatting == null || !formatting.isColor()) {
                    warn(
                            fileId,
                            "{} description line {} has invalid color {}.",
                            ownerId,
                            index,
                            line.color
                    );
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean validateAliases(
            ResourceLocation fileId,
            String ownerId,
            List<String> aliases
    ) {
        Set<String> uniqueAliases = new HashSet<>();

        for (String alias : aliases) {
            if (WTraderJsonValues.isBlank(alias)) {
                warn(fileId, "{} has a blank alias.", ownerId);
                return false;
            }

            if (!uniqueAliases.add(alias)) {
                warn(fileId, "{} repeats alias {}.", ownerId, alias);
                return false;
            }
        }

        return true;
    }

    private static boolean requireResourceLocation(
            ResourceLocation fileId,
            String value,
            String fieldName
    ) {
        if (WTraderJsonValues.isBlank(value)) {
            warn(fileId, "Missing {}.", fieldName);
            return false;
        }

        try {
            ResourceLocation.parse(value);
            return true;
        } catch (Exception exception) {
            warn(fileId, "Invalid {} resource location: {}.", fieldName, value);
            return false;
        }
    }

    private static void warn(
            ResourceLocation fileId,
            String message,
            Object... args
    ) {
        LegendaryDungeons.LOGGER.warn(
                "[Map Metadata JSON] {}: " + message,
                prepend(fileId, args)
        );
    }

    private static Object[] prepend(ResourceLocation fileId, Object[] args) {
        Object[] output = new Object[args.length + 1];
        output[0] = fileId;
        System.arraycopy(args, 0, output, 1, args.length);
        return output;
    }
}
