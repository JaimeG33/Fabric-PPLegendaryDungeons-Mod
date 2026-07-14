# Normal Rayquaza spawn.
# This function is intended to be called AS and AT the local event_starter armor stand.
# The Pokemon is spawned at the nearest local scs_pokespawn_main marker with spawnpokemonat.

# Spawn Rayquaza at the nearest local spawn marker.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run spawnpokemonat ~ ~ ~ rayquaza level=70

# Announce from this local event starter.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 run tellraw @a[distance=0..500,sort=nearest,limit=1] {"bold":true,"color":"dark_green","text":"Rayquaza has spawned!"}
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 run playsound entity.ender_dragon.ambient master @a[distance=..300]

# Tag the nearest Cobblemon Pokemon at the local spawn marker.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run tag @e[type=cobblemon:pokemon,sort=nearest,limit=1,distance=..8] add rayquaza_pokemon

# Give it the related effects.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:strength infinite 0 true
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:resistance infinite 3 true
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:wind_charged infinite 2 true

# Ensure it does not despawn.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run data merge entity @e[type=cobblemon:pokemon,tag=rayquaza_pokemon,sort=nearest,limit=1,distance=..8] {PersistenceRequired:1b}

# Lock only the local spawn marker and the local event starter.
execute unless score @s event_triggered matches 1 unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] spawn_once matches 1 as @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run scoreboard players set @s spawn_once 1
execute if entity @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] unless score @s event_triggered matches 1 run scoreboard players set @s event_triggered 1
