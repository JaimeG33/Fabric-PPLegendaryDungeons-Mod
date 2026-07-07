Professor Porker's Legendary Dungeons - Summon System Summary
=============================================================

This file explains the basic logic behind the legendary summon system.


CORE IDEA
=========

The summon system is built around this flow:

    Trigger detects possible summon
        -> LegendarySummonService
        -> LegendarySummonRegistry
        -> LegendarySummonDefinition
        -> Condition file
        -> LegendarySummonHelper
        -> Aftermath file

The goal is to have one shared summon pipeline instead of making a separate
always-running ticker for every legendary Pokemon.


MAIN FILES
==========

LegendarySummonService.java
---------------------------
The central coordinator.

When a trigger finds a possible summon, it sends a SummonContext here.

This file:
1. Checks every registered summon.
2. Runs that summon's condition file.
3. If the condition passes, calls LegendarySummonHelper.
4. If the Pokemon successfully spawns, runs that summon's aftermath file.


LegendarySummonRegistry.java
----------------------------
Stores the list of legendary summons that exist.

For now, only Rayquaza is registered.

Future legendaries should usually be added here, instead of creating a new
ticker for every Pokemon.


LegendarySummonDefinition.java
------------------------------
Stores the setup/data for one legendary summon.

For Rayquaza, this includes:
- species: rayquaza
- level: 70
- condition tag: pp_rayquaza_conditions
- spawn marker tag: pp_summon_rayquaza
- condition file: RayquazaEmeraldBlockCondition
- aftermath file: RayquazaAftermath


SummonContext.java
------------------
Stores information about one summon attempt.

For armor stand summons, this can include:
- the server level
- the nearby player
- the pp_legendary_summon marker
- the condition marker
- the final spawn position
- the trigger type


LegendarySummonHelper.java
--------------------------
Actually spawns the Cobblemon PokemonEntity.

This replaces relying on /spawnpokemon.

Most future legendary summons should use this helper instead of directly
spawning Pokemon elsewhere.


FOLDERS
=======

trigger/
--------
Contains files that detect when a summon attempt should begin.

Current file:

    EntityLegendarySummonTicker.java

This checks once per second for players near armor stands tagged:

    pp_legendary_summon

That tag marks the general legendary summon area.


condition/
----------
Contains Pokemon-specific requirement checks.

Current file:

    RayquazaEmeraldBlockCondition.java

This checks whether Rayquaza's summon requirements are met.


aftermath/
----------
Contains logic that runs after a Pokemon successfully spawns.

Current file:

    RayquazaAftermath.java

This removes the emerald block, marks the summon as used, plays sounds, and
messages nearby players.


ARMOR STAND MARKER FORMAT
=========================

Entity/armor stand based summons use three marker roles.


1. Main summon area marker
--------------------------

Tag:

    pp_legendary_summon

Purpose:

    This is the main marker checked by EntityLegendarySummonTicker.

    If a player gets within 10 blocks of this marker, the summon system starts
    checking registered legendary conditions.


2. Pokemon-specific condition marker
------------------------------------

Format:

    pp_whateverpokemon_conditions

Rayquaza example:

    pp_rayquaza_conditions

Purpose:

    This marker stores/checks the specific condition for that Pokemon.

    For Rayquaza, this armor stand must be holding an emerald block.


3. Pokemon-specific spawn marker
--------------------------------

Format:

    pp_summon_whateverpokemon

Rayquaza example:

    pp_summon_rayquaza

Purpose:

    This is the preferred location where the Pokemon spawns.

    If this marker is missing, Rayquaza falls back to spawning at the
    pp_rayquaza_conditions marker.


RAYQUAZA EXAMPLE
================

Rayquaza currently works like this:

1. Player gets close to an armor stand tagged:

       pp_legendary_summon

2. EntityLegendarySummonTicker creates a SummonContext.

3. LegendarySummonService checks the registered summons.

4. RayquazaEmeraldBlockCondition searches near pp_legendary_summon for:

       pp_rayquaza_conditions

5. If pp_rayquaza_conditions is holding an emerald block, the condition passes.

6. The condition then searches for:

       pp_summon_rayquaza

7. If pp_summon_rayquaza exists, Rayquaza spawns there.

8. If pp_summon_rayquaza does not exist, Rayquaza spawns at:

       pp_rayquaza_conditions

9. LegendarySummonHelper spawns a level 70 Rayquaza.

10. RayquazaAftermath runs:
    - removes the emerald block
    - adds the used tag
    - plays sounds
    - sends a message to nearby players


MULTIPLE TAGS ON ONE ARMOR STAND
================================

Armor stands can have multiple tags.

This should work:

    pp_legendary_summon
    pp_rayquaza_conditions

In that setup, the same armor stand is both the main area marker and the
Rayquaza condition marker.

This should also work:

    pp_legendary_summon
    pp_rayquaza_conditions
    pp_summon_rayquaza

In that setup, the same armor stand is the area marker, condition marker, and
spawn marker. Rayquaza should spawn at that armor stand.

For simple tests, combining tags is fine.

For larger structures, separate armor stands may be cleaner:

    pp_legendary_summon      = player proximity trigger
    pp_rayquaza_conditions   = item/condition check
    pp_summon_rayquaza       = final spawn location


MULTIPLAYER NOTES
=================

The system is intended to be multiplayer friendly because the summon state is
tied to specific armor stand markers, not one global variable.

Example:

- Player A is near a summon marker with no emerald block.
- Player B is near another valid Rayquaza setup with an emerald block.

Only Player B's valid setup should summon Rayquaza.

After Rayquaza spawns, the system adds:

    pp_rayquaza_summoned

This helps prevent the same marker from being reused.


ADDING FUTURE LEGENDARIES
=========================

For a new legendary, usually add:

1. A condition file in summon/condition.
2. An aftermath file in summon/aftermath.
3. A new LegendarySummonDefinition in LegendarySummonRegistry.

Example future tags:

    pp_diancie_conditions
    pp_summon_diancie

    pp_hoopa_conditions
    pp_summon_hoopa

The main entity ticker should not need to change for every new Pokemon.


SUMMARY
=======

Current Rayquaza marker setup:

    pp_legendary_summon
        main area marker checked when player is nearby

    pp_rayquaza_conditions
        must hold emerald block

    pp_summon_rayquaza
        preferred Rayquaza spawn location

Core pipeline:

    EntityLegendarySummonTicker
        -> LegendarySummonService
        -> condition file
        -> LegendarySummonHelper
        -> aftermath file