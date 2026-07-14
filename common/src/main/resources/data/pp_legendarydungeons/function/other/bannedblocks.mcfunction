
# If the player is close to a dungeon marker or tagged eguardian

# Ban the selected blocks

execute as @a at @s if entity @e[type=elder_guardian,tag=scs_eguardian,distance=..100] run function pp_legendarydungeons:other/noblocks/beds

execute as @a at @s if entity @e[type=elder_guardian,tag=scs_eguardian,distance=..120] run function pp_legendarydungeons:other/noblocks/pc

execute as @a at @s if entity @e[type=elder_guardian,tag=scs_eguardian,distance=..120] run function pp_legendarydungeons:other/noblocks/heal

