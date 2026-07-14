# Called AS and AT the local event_starter armor stand.

# Spawn shiny Rayquaza if the nearest player has unlocked the secret.
execute if data entity @s HandItems[{id:"minecraft:nether_star"}] if entity @a[distance=..10,scores={scs_secrets=10},sort=nearest,limit=1] unless score @s scs_secrets matches 1 run function pp_legendarydungeons:rt/spawnpokemon/alt_rayquaza_perfshiny

# Lock the local event starter so this secret spawn cannot repeat.
execute if data entity @s HandItems[{id:"minecraft:nether_star"}] if entity @a[distance=..10,scores={scs_secrets=10},sort=nearest,limit=1] unless score @s scs_secrets matches 1 run scoreboard players set @s scs_secrets 1

# Consume the nearest player's secret unlock.
execute if data entity @s HandItems[{id:"minecraft:nether_star"}] if entity @a[distance=..10,scores={scs_secrets=10},sort=nearest,limit=1] run scoreboard players set @a[distance=..10,scores={scs_secrets=10},sort=nearest,limit=1] scs_secrets 0

# Remove the nether star from the local event starter after the player's unlock has been consumed.
execute if data entity @s HandItems[{id:"minecraft:nether_star"}] if entity @a[distance=..10,scores={scs_secrets=0},sort=nearest,limit=1] run data modify entity @s HandItems[0] set value {id:"minecraft:air",Count:0b}
