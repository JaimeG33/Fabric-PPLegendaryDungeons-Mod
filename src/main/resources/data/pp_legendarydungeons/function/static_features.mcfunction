# Central static feature scanner.
# Runs every 30 ticks.
# Multiplayer-friendly: checks from feature markers, not from players.

execute as @e[type=minecraft:armor_stand,tag=static_features_check] at @s if entity @a[distance=..128] run function pp_legendarydungeons:which_static_features

schedule function pp_legendarydungeons:static_features 30t replace