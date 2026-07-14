# This function is executing as the selected player, at the selected player.
# Check which move-specific armor stand is near this MoveEvent area.
# Also includes checks for rubble piles and alternate loot spawns focused on the player.


# Pokemon Specific Checks:
execute if entity @e[type=minecraft:armor_stand,tag=move_dig,distance=..8,sort=nearest,limit=1] run function pp_legendarydungeons:other/move_events/dig/poke_check








# Player Focused Checks:
execute if entity @e[type=minecraft:block_display,tag=pp_rubble_loot,distance=..7,sort=nearest,limit=1] run function pp_legendarydungeons:other/move_events/player/crystal_caves/rubble_pile_loot_check1