# This function is executing as the nearby player, at the nearby player.
# Particles are shown only to that player with @s at the end of each particle command.

# Small hint particles at the general MoveEvent armor stand.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] run particle minecraft:dust{color:[0.35,0.35,0.35],scale:0.8} ~ ~1.2 ~ 0.15 0.25 0.15 0.005 2 force @s

# Slight brown/earth hint at the general MoveEvent stand.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] run particle minecraft:dust{color:[0.50,0.42,0.30],scale:0.7} ~ ~0.8 ~ 0.12 0.15 0.12 0.005 1 force @s

# Small hint particles at the nearby Dig armor stand.
# This searches for move_dig near the general pp_Move_Event stand, not just near the player.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] at @e[type=minecraft:armor_stand,tag=move_dig,distance=..8,sort=nearest,limit=1] run particle minecraft:dust{color:[0.35,0.35,0.35],scale:0.8} ~ ~1.2 ~ 0.15 0.25 0.15 0.005 2 force @s

execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] at @e[type=minecraft:armor_stand,tag=move_dig,distance=..8,sort=nearest,limit=1] run particle minecraft:dust{color:[0.50,0.42,0.30],scale:0.7} ~ ~0.8 ~ 0.12 0.15 0.12 0.005 1 force @s

# Very small particles at each nearby MoveEvent block_display around the Dig stand.
# Keep counts low because this runs repeatedly.
execute at @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1] at @e[type=minecraft:armor_stand,tag=move_dig,distance=..8,sort=nearest,limit=1] at @e[type=minecraft:block_display,tag=MoveEvent,distance=..5] run particle minecraft:dust{color:[0.35,0.35,0.35],scale:0.45} ~ ~0.4 ~ 0.08 0.08 0.08 0.002 1 force @s