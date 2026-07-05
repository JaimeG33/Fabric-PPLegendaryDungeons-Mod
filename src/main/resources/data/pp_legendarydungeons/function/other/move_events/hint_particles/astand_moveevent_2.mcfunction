# pp_legendarydungeons:other/move_events/hint_particles
# This function is executing as the nearby player, at the nearby player.
# Particles are shown only to that player with @s at the end of each particle command.
# This generic version only uses the pp_Move_Event armor stand, so it can work for any future move event.

# Strong gray dust cloud around the MoveEvent marker.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..12,sort=nearest,limit=1] run particle minecraft:dust{color:[0.35,0.35,0.35],scale:1.0} ~ ~1 ~ 0.4 0.2 0.4 0.01 5 force @s

# Smaller brown/earth dust mixed in.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..12,sort=nearest,limit=1] run particle minecraft:dust{color:[0.55,0.45,0.32],scale:0.9} ~ ~0.5 ~ 0.4 0.15 0.4 0.01 2 force @s

# Gravel block particles close to the ground.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..12,sort=nearest,limit=1] run particle minecraft:block{block_state:"minecraft:gravel"} ~ ~0.2 ~ 0.4 0.1 0.4 0.03 3 force @s