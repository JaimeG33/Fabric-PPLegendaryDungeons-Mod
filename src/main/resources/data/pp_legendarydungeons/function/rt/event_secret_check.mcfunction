# Secret sequence tick check.
# Runs each secret starter locally instead of letting the secret function use global armor stand selectors.
execute as @e[type=minecraft:armor_stand,tag=scs_secret_starter] at @s unless score @s event_triggered matches 1 if entity @a[distance=..30,sort=nearest,limit=1] run function pp_legendarydungeons:rt/secrets/alt_rayquaza
