Professor Porker's Legendary Dungeons - Legendary Summon System Notes
====================================================================

This file explains the logic behind the legendary summon system.

This is a plain text documentation file. It is not Java code and should be ignored by
Minecraft/Fabric during normal gameplay. It is only here to help explain how the
summon folder is organized and how the summon workflow is supposed to work.


====================================================================
BIG PICTURE
====================================================================

The legendary summon system is designed around this workflow:

1. A trigger detects that a player is near or interacting with a summon point.
2. The trigger creates a SummonContext.
3. LegendarySummonService checks all registered legendary summon definitions.
4. Each summon definition has its own condition file.
5. If a condition passes, LegendarySummonHelper spawns the Cobblemon PokemonEntity.
6. If the Pokemon successfully spawns, that summon's aftermath file runs.

The important idea is:

    Different ways to START a summon.
    One shared way to CHECK, SPAWN, and CLEAN UP after a summon.

This means armor stands, custom blocks, dungeon phases, or future world-condition
systems can all eventually use the same central summon pipeline.


====================================================================
MAIN FILES IN THE summon FOLDER
====================================================================

summon/LegendarySummonService.java
---------------------------------
This is the central coordinator.

Trigger files call LegendarySummonService.trySummon(context).

The service does this:

1. Loops through every registered LegendarySummonDefinition.
2. Asks each definition's condition file if the summon is allowed.
3. If a condition passes, it gets a prepared SummonContext.
4. Calls LegendarySummonHelper to spawn the Pokemon.
5. If the Pokemon spawns successfully, calls that definition's aftermath file.

The service does not know all the details of Rayquaza, Diancie, Hoopa, etc.
It only coordinates the universal summon process.


summon/LegendarySummonRegistry.java
-----------------------------------
This file stores the list of all legendary summons that currently exist.

For now, Rayquaza is the only registered summon.

Later, adding another simple legendary should usually mean:

1. Create a new condition file in summon/condition.
2. Create a new aftermath file in summon/aftermath.
3. Add a new LegendarySummonDefinition to LegendarySummonRegistry.

A new always-running ticker should NOT be needed for every legendary.


summon/LegendarySummonDefinition.java
-------------------------------------
This stores the data/config for a specific legendary summon.

A definition includes things like:

- internal ID
- Cobblemon species name
- Pokemon level
- condition armor stand tag
- spawn marker armor stand tag
- search radiuses
- condition logic file
- aftermath logic file

For Rayquaza, the definition currently uses:

- id: rayquaza
- species: rayquaza
- level: 70
- condition tag: pp_rayquaza_conditions
- spawn marker tag: pp_summon_rayquaza


summon/LegendarySummonHelper.java
---------------------------------
This file performs the actual Cobblemon spawn.

This is the replacement for relying on the /spawnpokemon command.

It creates the Pokemon using Cobblemon's Java-side classes, roughly like this:

    PokemonProperties -> Pokemon -> PokemonEntity -> addFreshEntity(...)

Other summon files should not manually create Cobblemon PokemonEntities unless
there is a special reason. Most summons should call this helper.


summon/SummonContext.java
-------------------------
This stores information about one specific summon attempt.

For an armor stand summon, the context can include:

- the ServerLevel
- the player who got close enough
- the main pp_legendary_summon armor stand
- the specific condition armor stand
- the final spawn position
- the trigger type

The context starts with only the basic trigger information.
Then the condition file fills in more details if the summon requirements are met.


summon/SummonTriggerType.java
-----------------------------
This describes how a summon attempt started.

Currently used:

- ENTITY_PROXIMITY

Future possible uses:

- BLOCK_INTERACTION
- BLOCK_TICK
- DUNGEON_PHASE


====================================================================
SUBFOLDERS
====================================================================

summon/trigger/
---------------
This folder contains files that detect when a summon attempt should begin.

Current file:

    EntityLegendarySummonTicker.java

This file checks once per second for players near armor stands tagged:

    pp_legendary_summon

This armor stand is the main marker that says:

    "This area may contain a legendary summon."

The trigger file does not know all the details of Rayquaza.
It only detects that the player is close enough to a legendary summon area,
then passes the attempt to LegendarySummonService.


summon/condition/
-----------------
This folder contains files that determine whether a specific legendary's summon
requirements are met.

Current file:

    RayquazaEmeraldBlockCondition.java

This file checks Rayquaza's specific requirements:

1. Find pp_rayquaza_conditions near pp_legendary_summon.
2. Check if pp_rayquaza_conditions is holding an emerald block.
3. Find pp_summon_rayquaza as the preferred spawn position.
4. If pp_summon_rayquaza is missing, fall back to pp_rayquaza_conditions.

Simple future summons can use simple condition files.
Complex future summons can have their own custom condition files.

Examples:

    DiancieCrystalHeartCondition.java
    HoopaPortalCondition.java
    RegigigasTempleCondition.java


summon/aftermath/
-----------------
This folder contains files that run after a Pokemon successfully spawns.

Current file:

    RayquazaAftermath.java

This file currently does things like:

- remove the emerald block from the Rayquaza condition marker
- add a used tag so the same marker cannot summon again
- play sounds near the spawn location
- send a message to nearby players

Aftermath should only run after the Pokemon actually spawns successfully.

This is important because if spawning fails, the required item should not be
consumed and the summon should not be marked as used.


====================================================================
ENTITY / ARMOR STAND SUMMON MARKER FORMAT
====================================================================

The current armor stand based system uses up to three marker roles.

1. Main summon area marker
--------------------------

Tag:

    pp_legendary_summon

Purpose:

    This is the main marker that the ticker searches for.

    If a player is within 10 blocks of an armor stand with this tag, the summon
    system begins checking whether any registered legendary conditions are met.

Think of this as:

    "The player is now inside a legendary summon area."


2. Pokemon-specific condition marker
------------------------------------

Format:

    pp_whateverpokemon_conditions

Rayquaza example:

    pp_rayquaza_conditions

Purpose:

    This marker is used to check the specific requirements for that Pokemon.

For Rayquaza, pp_rayquaza_conditions is checked to see if it is holding an
emerald block.

In the future, another Pokemon could use something like:

    pp_diancie_conditions
    pp_hoopa_conditions
    pp_regigigas_conditions

Each of those could have its own condition file with different logic.


3. Pokemon-specific spawn marker
--------------------------------

Format:

    pp_summon_whateverpokemon

Rayquaza example:

    pp_summon_rayquaza

Purpose:

    This is the preferred location where the Pokemon should appear.

For Rayquaza:

    If pp_summon_rayquaza exists nearby, Rayquaza spawns there.
    If pp_summon_rayquaza does not exist nearby, Rayquaza falls back to spawning
    at the pp_rayquaza_conditions marker.


====================================================================
RAYQUAZA EXAMPLE - FULL WORKFLOW
====================================================================

The current Rayquaza summon works like this:

1. A player walks near an armor stand tagged:

       pp_legendary_summon

2. EntityLegendarySummonTicker sees that the player is within 10 blocks.

3. The ticker creates a SummonContext and sends it to:

       LegendarySummonService.trySummon(context)

4. LegendarySummonService checks every registered summon in:

       LegendarySummonRegistry

   For now, the only registered summon is Rayquaza.

5. The Rayquaza definition sends the context to:

       RayquazaEmeraldBlockCondition

6. RayquazaEmeraldBlockCondition searches near the pp_legendary_summon marker
   for an armor stand tagged:

       pp_rayquaza_conditions

7. If no pp_rayquaza_conditions marker is found, nothing happens.

8. If pp_rayquaza_conditions is found, the condition checks whether it is
   holding an emerald block.

9. If there is no emerald block, nothing happens.

10. If there is an emerald block, the condition searches near
    pp_rayquaza_conditions for an armor stand tagged:

       pp_summon_rayquaza

11. If pp_summon_rayquaza is found, Rayquaza will spawn there.

12. If pp_summon_rayquaza is not found, Rayquaza will spawn at the location of:

       pp_rayquaza_conditions

13. The prepared context is returned to LegendarySummonService.

14. LegendarySummonService calls LegendarySummonHelper.

15. LegendarySummonHelper creates and spawns a level 70 Rayquaza directly as a
    Cobblemon PokemonEntity.

16. If the spawn succeeds, LegendarySummonService calls:

       RayquazaAftermath

17. RayquazaAftermath removes the emerald block, marks the marker as used,
    plays sounds, and sends a message to nearby players.


====================================================================
MULTIPLE TAGS ON THE SAME ARMOR STAND
====================================================================

Armor stands can have multiple tags.

The current setup should support combining the Rayquaza marker roles onto the
same armor stand.

For example, this should work:

    pp_legendary_summon
    pp_rayquaza_conditions

In that setup, the same armor stand is both:

    - the main area marker
    - the Rayquaza condition marker

The player gets close to that armor stand, and the system then checks that same
armor stand for the emerald block.

This should also work:

    pp_legendary_summon
    pp_rayquaza_conditions
    pp_summon_rayquaza

In that setup, the same armor stand is:

    - the main area marker
    - the Rayquaza condition marker
    - the Rayquaza spawn marker

Rayquaza should spawn at that armor stand's position.

Even though this can work, separating the markers may be cleaner for large
structures. For example:

    pp_legendary_summon        = invisible trigger near the entrance or center
    pp_rayquaza_conditions     = pedestal / item check location
    pp_summon_rayquaza         = dramatic spawn location

Use multiple tags on one armor stand for simple setups.
Use separate armor stands for more controlled structure layouts.


====================================================================
MULTIPLAYER NOTES
====================================================================

The current design is intended to be multiplayer friendly because summon state
is tied to the specific armor stand markers, not a global variable.

For example:

- Player A can be near one pp_legendary_summon marker with no emerald block.
- Player B can be near another pp_legendary_summon marker with a valid Rayquaza
  emerald block setup.

Only Player B's valid setup should trigger.

After Rayquaza successfully spawns, the aftermath adds a used tag:

    pp_rayquaza_summoned

This helps prevent the same Rayquaza condition marker from being reused.

The emerald block is also removed only after the Pokemon successfully spawns.


====================================================================
ADDING FUTURE LEGENDARIES
====================================================================

For a future legendary, the expected workflow is:

1. Add a new condition file in summon/condition.

   Example:

       DiancieCrystalHeartCondition.java

2. Add a new aftermath file in summon/aftermath.

   Example:

       DiancieAftermath.java

3. Add a new LegendarySummonDefinition to LegendarySummonRegistry.

   Example tags:

       pp_diancie_conditions
       pp_summon_diancie

4. Place the matching armor stands in the structure.

The main ticker should not need to change for every new Pokemon.

The goal is:

    One entity ticker.
    Many summon definitions.
    Specific condition files only when needed.
    Specific aftermath files only when needed.


====================================================================
SUMMARY
====================================================================

The current entity-based legendary summon pattern is:

    pp_legendary_summon
        main area marker checked by the ticker

    pp_whateverpokemon_conditions
        Pokemon-specific condition marker

    pp_summon_whateverpokemon
        Pokemon-specific spawn marker

For Rayquaza:

    pp_legendary_summon
        starts the summon check when player is nearby

    pp_rayquaza_conditions
        must hold emerald block

    pp_summon_rayquaza
        preferred Rayquaza spawn location

Core pipeline:

    EntityLegendarySummonTicker
        -> LegendarySummonService
        -> LegendarySummonRegistry
        -> LegendarySummonDefinition
        -> LegendarySummonCondition
        -> LegendarySummonHelper
        -> LegendarySummonAftermath

This structure keeps the system scalable, easier to debug, and safer for
multiplayer than creating a separate always-running ticker for every legendary.