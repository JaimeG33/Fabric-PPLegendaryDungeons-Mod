
# Scan a 150 block radius to break desired block
# If the command ran successfully and broke a bed, it will store a score in the player's event_triggered scoreboard
execute as @a at @s if entity @e[type=armor_stand,tag=dungeon_restriction,distance=..150] store success score @s event_triggered run fill ~-10 ~-10 ~-10 ~10 ~10 ~10 air replace cobblemon:pc

# If the event_triggered score is 1, it means a bed was broken and we can notify the player
execute as @a[scores={event_triggered=1..}] run tellraw @s {"text":"There seems to be an ancient force interfering with this technology","color":"dark_purple","italic":true}

# Return the broken bed (just gray for now)
execute as @a[scores={event_triggered=1..}] run give @p cobblemon:pc

# Reset the event_triggered scoreboards for those affected
execute as @a[scores={event_triggered=1..}] run scoreboard players reset @a[scores={event_triggered=1..}] event_triggered
