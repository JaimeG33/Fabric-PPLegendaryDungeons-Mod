# Crystal Heart active render loop.
# This file is now scoped for:
# data/pp_legendarydungeons/function/dungeons/crystal_caves/crystal_heart/loop.mcfunction
#
# It should NOT be called directly from load.
# It gets activated by pp_legendarydungeons:which_static_features.
#
# Multiplayer-safe pattern:
# - Executes once per crystal_heart marker, not once per player.
# - Particles are sent to nearby players with @a[distance=..64].
# - The loop only keeps itself alive while at least one player is within 128 blocks
#   of at least one crystal_heart marker.
#
# Frequency control:
# Change the final 4t in the schedule line below.
# 2t = smoother / heavier
# 4t = recommended
# 6t = lighter
# 10t = very light

# Render every crystal whose marker has a player close enough to see it.
execute as @e[type=minecraft:armor_stand,tag=crystal_heart] at @s if entity @a[distance=..64] run function pp_legendarydungeons:dungeons/crystal_caves/crystal_heart/render

# Keep this feature loop alive only while at least one player is near a crystal_heart marker.
# schedule ... replace prevents a buildup of duplicate scheduled loops.
execute as @e[type=minecraft:armor_stand,tag=crystal_heart] at @s if entity @a[distance=..128] run schedule function pp_legendarydungeons:dungeons/crystal_caves/crystal_heart/loop 4t replace
