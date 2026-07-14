# Spawns a wandering trader at the location of the static_features / pp_reg_wtrader armor stand

# Finds regular wandering trader markers that are near a player.
# This is safe to schedule globally because it searches for the marker again.
# It processes each valid marker once, then the helper kills that marker.

execute as @e[type=minecraft:armor_stand,tag=pp_reg_wtrader] at @s if entity @a[distance=..80] run summon minecraft:wandering_trader ~ ~ ~

# Kills the marker after spawning the trader to prevent duplicate spawns.
execute as @e[type=minecraft:armor_stand,tag=pp_reg_wtrader] at @s if entity @a[distance=..80] run kill @s