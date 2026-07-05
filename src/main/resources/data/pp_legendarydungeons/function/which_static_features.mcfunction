# Determines which static feature is near the static_features_check marker.
# This function is called as/at a static_features_check armor stand.

# Crystal Heart
# Activates the faster crystal_heart loop only when a player is close enough to actually see it.
# schedule ... replace prevents duplicate loop buildup.
execute as @e[type=minecraft:armor_stand,tag=crystal_heart,distance=..64] at @s if entity @a[distance=..64] run schedule function pp_legendarydungeons:dungeons/crystal_caves/crystal_heart/loop 1t replace



# Wandering Trader Spawn: (executes as armor stand only once)
execute if entity @e[type=minecraft:armor_stand,tag=pp_reg_wtrader,distance=..80] if entity @a[distance=..80] run schedule function pp_legendarydungeons:other/wtraders/blank_trader 1t replace


# Future examples:
# execute as @e[type=minecraft:armor_stand,tag=altar_glow,distance=..64] at @s if entity @a[distance=..64] run schedule function pp_legendarydungeons:dungeons/crystal_caves/altar_glow/loop 1t replace
# execute as @e[type=minecraft:armor_stand,tag=portal_aura,distance=..64] at @s if entity @a[distance=..64] run schedule function pp_legendarydungeons:dungeons/crystal_caves/portal_aura/loop 1t replace