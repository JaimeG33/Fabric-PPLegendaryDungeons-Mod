package porker.pp_legendarydungeons.summon.condition;

import porker.pp_legendarydungeons.summon.LegendarySummonDefinition;
import porker.pp_legendarydungeons.summon.SummonContext;

import java.util.Optional;

/**
 * A condition decides whether a legendary summon is allowed to happen.
 *
 * It does NOT spawn the Pokémon.
 * It only checks requirements and prepares the final summon context.
 *
 * Example:
 * RayquazaEmeraldBlockCondition checks:
 * - Is there a nearby pp_rayquaza_conditions armor stand?
 * - Is it holding an emerald block?
 * - Where should Rayquaza spawn?
 *
 * If the condition passes, return Optional.of(preparedContext).
 * If the condition fails, return Optional.empty().
 */
public interface LegendarySummonCondition {
    Optional<SummonContext> prepareContext(
            SummonContext context,
            LegendarySummonDefinition definition
    );
}