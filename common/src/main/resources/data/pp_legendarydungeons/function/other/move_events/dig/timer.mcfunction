# This function only runs after being scheduled by poke_check.
# It then schedules itself while at least one Dig event is actively counting down.

# Debug actionbar. Remove later if you want.
execute as @e[type=minecraft:armor_stand,tag=move_dig,scores={event_triggered=1..30}] at @s run title @a[distance=..12] actionbar [{"text":"Dig timer: ","color":"yellow"},{"score":{"name":"@s","objective":"event_triggered"},"color":"gold"}]

# If a move_dig armor stand has reached 1, run break_blocks as/at that armor stand.
execute as @e[type=minecraft:armor_stand,tag=move_dig,scores={event_triggered=1}] at @s run function pp_legendarydungeons:other/move_events/dig/break_blocks

# If the armor stand still exists after break_blocks, mark it completed.
scoreboard players set @e[type=minecraft:armor_stand,tag=move_dig,scores={event_triggered=1}] event_triggered -1

# Count down active timers.
scoreboard players remove @e[type=minecraft:armor_stand,tag=move_dig,scores={event_triggered=2..}] event_triggered 1

# If any Dig event is still active, schedule this timer again for the next tick.
execute if entity @e[type=minecraft:armor_stand,tag=move_dig,scores={event_triggered=1..30}] run schedule function pp_legendarydungeons:other/move_events/dig/timer 1t replace