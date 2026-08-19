package porker.pp_legendarydungeons.features.dungeon_factions;

import java.util.ArrayList;
import java.util.List;

/**
 * Datapack-backed relationship definition for one dungeon encounter faction.
 *
 * <p>Step 1 intentionally keeps the faction definition small. Membership lives
 * on encounter profiles, while this resource owns faction-to-faction hostility.
 * Player hostility remains an individual encounter-profile decision because
 * members of one faction may intentionally react to players differently.</p>
 */
public final class DungeonFactionProfileJson {
    public List<String> hostile_factions = new ArrayList<>();
}
