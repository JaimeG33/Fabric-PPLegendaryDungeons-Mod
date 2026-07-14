# Check to see if a player is within 10 blocks of an event location.
# Event location = armor stand with tag pp_Move_Event.
# Runs the move-selection function as that player.

# old
# execute as @a at @s if entity @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] run function pp_legendarydungeons:other/move_events/which_move

# Check each player individually.
# If they are near a pp_Move_Event stand, move execution position to that specific stand.
# @s remains the player.

execute as @a at @s at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] run function pp_legendarydungeons:other/move_events/which_move

# Debug only. Disable later because this will spam.
# execute as @a at @s if entity @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] run say "player is within 10 blocks"
# should only run at the location of the player next to the armor stand, one time per player at the stand (repeated by tick function)