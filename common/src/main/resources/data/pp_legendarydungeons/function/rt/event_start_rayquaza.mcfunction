# Rayquaza event starter tick check
# Secret check runs first so a nether star does not also trigger the normal Rayquaza path.
execute as @e[type=minecraft:armor_stand,tag=event_starter] at @s if data entity @s HandItems[{id:"minecraft:nether_star"}] if entity @a[distance=..10,scores={scs_secrets=10},sort=nearest,limit=1] unless score @s scs_secrets matches 1 run function pp_legendarydungeons:rt/secrets/alt_rayquaza_spawn_check

# Normal Rayquaza check. Excludes nether star so the shiny-secret item does not also run the normal spawn.
execute as @e[type=minecraft:armor_stand,tag=event_starter] at @s unless data entity @s HandItems[{id:"minecraft:emerald_block"}] unless data entity @s HandItems[{id:"minecraft:nether_star"}] unless score @s event_triggered matches 1 unless score @s scs_secrets matches 1 if entity @a[distance=..10,sort=nearest,limit=1] run function pp_legendarydungeons:rt/spawnpokemon/rayquaza
