# First scan to see if there is gravel nearby
# Running as the pp_rubble_loot block_display.
# Positioned at the rubble pile.

scoreboard players set @s event_triggered 0

execute if block ~-1 ~ ~-1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~-1 ~ ~ #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~-1 ~ ~1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1

execute if block ~ ~ ~-1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~ ~ ~ #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~ ~ ~1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1

execute if block ~1 ~ ~-1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~1 ~ ~ #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1
execute if block ~1 ~ ~1 #pp_legendarydungeons:rubble_source_blocks run scoreboard players set @s event_triggered 1

execute if score @s event_triggered matches 0 run function pp_legendarydungeons:other/move_events/player/crystal_caves/kill_rubble










# This function is running as the pp_rubble_loot block_display.
# The execution position is the rubble pile.
# It looks for the standard cache fragment item near this specific rubble pile.

execute as @e[type=minecraft:item,distance=..2,sort=nearest,limit=1,nbt={Item:{id:"minecraft:flint",components:{"minecraft:custom_model_data":1113301}}}] at @s run function pp_legendarydungeons:other/move_events/player/crystal_caves/standard_rubble_pile1_trigger

execute as @e[type=minecraft:item,distance=..2,sort=nearest,limit=1,nbt={Item:{id:"minecraft:raw_iron",components:{"minecraft:custom_model_data":1113302}}}] at @s run function pp_legendarydungeons:other/move_events/player/crystal_caves/loot_trigger/standard_rubble_pile2

execute as @e[type=minecraft:item,distance=..2,sort=nearest,limit=1,nbt={Item:{id:"minecraft:amethyst_cluster",components:{"minecraft:custom_model_data":1113303}}}] at @s run function pp_legendarydungeons:other/move_events/player/crystal_caves/loot_trigger/secret_rubble_pile

execute as @e[type=minecraft:item,distance=..2,sort=nearest,limit=1,nbt={Item:{id:"minecraft:emerald",components:{"minecraft:custom_model_data":1113304}}}] at @s run function pp_legendarydungeons:other/move_events/player/crystal_caves/loot_trigger/royal_rubble_pile
