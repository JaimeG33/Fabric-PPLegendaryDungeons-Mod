# This function is running as the player.
# The execution position should be the nearby pp_Move_Event armor stand.
# It checks nearby rubble pile block displays and runs logic from the display's location.

execute as @e[type=minecraft:block_display,tag=pp_rubble_loot,distance=..4] at @s run function pp_legendarydungeons:other/move_events/player/crystal_caves/rubble_node_check1