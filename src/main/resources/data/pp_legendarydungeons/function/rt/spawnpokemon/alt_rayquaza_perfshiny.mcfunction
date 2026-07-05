# Secret shiny Rayquaza spawn.
# This function is intended to be called AS and AT the local event_starter armor stand.
# The Pokemon is spawned at the nearest local scs_pokespawn_main marker with spawnpokemonat.

# Spawn shiny perfect-IV Rayquaza.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run spawnpokemonat ~ ~ ~ rayquaza level=70 shiny attack_iv=31 defence_iv=31 hp_iv=31 special_attack_iv=31 special_defence_iv=31 speed_iv=31

# Assign its tag.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run tag @e[type=cobblemon:pokemon,sort=nearest,limit=1,distance=..8] add secret_rayquaza_pokemon

# Give it its effects.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=secret_rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:strength infinite 1 true
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=secret_rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:resistance infinite 4 true
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run effect give @e[type=cobblemon:pokemon,tag=secret_rayquaza_pokemon,sort=nearest,limit=1,distance=..8] minecraft:wind_charged infinite 4 true

# Ensure it does not despawn.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 at @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run data merge entity @e[type=cobblemon:pokemon,tag=secret_rayquaza_pokemon,sort=nearest,limit=1,distance=..8] {PersistenceRequired:1b}

# Announce from the local event starter.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 run tellraw @a[distance=0..500,sort=nearest,limit=1] {"bold":true,"color":"dark_red","text":"SHINY Rayquaza has spawned!!!"}
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 run playsound block.end_portal.spawn master @a[distance=..300]

# Prevent re-use on only the local spawn marker.
execute unless score @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] scs_secrets matches 1 as @e[type=minecraft:armor_stand,tag=scs_pokespawn_main,sort=nearest,limit=1,distance=..100] run scoreboard players set @s scs_secrets 1
