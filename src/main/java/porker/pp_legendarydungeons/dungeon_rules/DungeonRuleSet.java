
package porker.pp_legendarydungeons.dungeon_rules;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Compact rule-override container.
 *
 * The same class is used for parent overrides and child-zone overrides.
 */
public final class DungeonRuleSet {
    private static final String NBT_PREFIX = "Rule_";
    private final EnumMap<DungeonRule, RuleDecision> decisions = new EnumMap<>(DungeonRule.class);

    public DungeonRuleSet() {
        for (DungeonRule rule : DungeonRule.values()) {
            decisions.put(rule, RuleDecision.INHERIT);
        }
    }

    public RuleDecision get(DungeonRule rule) {
        return decisions.getOrDefault(rule, RuleDecision.INHERIT);
    }

    public void set(DungeonRule rule, RuleDecision decision) {
        decisions.put(rule, decision == null ? RuleDecision.INHERIT : decision);
    }

    public EnumSet<DungeonRule> resolve(Set<DungeonRule> inherited) {
        EnumSet<DungeonRule> resolved = EnumSet.noneOf(DungeonRule.class);

        for (DungeonRule rule : DungeonRule.values()) {
            boolean inheritedValue = inherited.contains(rule);
            if (get(rule).resolve(inheritedValue)) {
                resolved.add(rule);
            }
        }

        return resolved;
    }

    public int toPackedInt() {
        int packed = 0;
        int shift = 0;

        for (DungeonRule rule : DungeonRule.values()) {
            packed |= (get(rule).ordinal() & 0b11) << shift;
            shift += 2;
        }

        return packed;
    }

    public static DungeonRuleSet fromPackedInt(int packed) {
        DungeonRuleSet set = new DungeonRuleSet();
        int shift = 0;

        for (DungeonRule rule : DungeonRule.values()) {
            set.set(rule, RuleDecision.fromOrdinalSafe((packed >> shift) & 0b11));
            shift += 2;
        }

        return set;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        for (Map.Entry<DungeonRule, RuleDecision> entry : decisions.entrySet()) {
            tag.putString(NBT_PREFIX + entry.getKey().name(), entry.getValue().name());
        }

        return tag;
    }

    public static DungeonRuleSet load(CompoundTag tag) {
        DungeonRuleSet set = new DungeonRuleSet();

        for (DungeonRule rule : DungeonRule.values()) {
            String key = NBT_PREFIX + rule.name();
            if (!tag.contains(key)) {
                continue;
            }

            try {
                set.set(rule, RuleDecision.valueOf(tag.getString(key)));
            } catch (IllegalArgumentException ignored) {
                set.set(rule, RuleDecision.INHERIT);
            }
        }

        return set;
    }
}
