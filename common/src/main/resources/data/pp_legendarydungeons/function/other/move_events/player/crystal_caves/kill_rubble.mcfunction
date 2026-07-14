# Spawn the main reward loot. (Turned off for now)
# loot spawn ~ ~ ~ loot pp_legendarydungeons:dungeons/crystal_caves/standard_rubble_pile1

# Spawn extra broken-rubble themed loot.
loot spawn ~ ~ ~ loot pp_legendarydungeons:dungeons/crystal_caves/broken_rubble_pile1


# Stone / crystal breaking effects for nearby players.
playsound minecraft:block.amethyst_block.break block @a[distance=..12] ~ ~ ~ 1 0.8 1
playsound minecraft:block.deepslate.break block @a[distance=..12] ~ ~ ~ 1 0.7 1
playsound minecraft:block.gravel.break block @a[distance=..12] ~ ~ ~ 0.8 0.9 1
playsound minecraft:block.glass.break block @a[distance=..12] ~ ~ ~ 0.5 1.2 1

particle minecraft:block{block_state:{Name:"minecraft:amethyst_block"}} ~ ~0.4 ~ 0.5 0.35 0.5 0.08 35 force @a[distance=..12]
particle minecraft:block{block_state:{Name:"minecraft:deepslate"}} ~ ~0.3 ~ 0.6 0.3 0.6 0.06 45 force @a[distance=..12]
particle minecraft:poof ~ ~0.4 ~ 0.5 0.3 0.5 0.04 25 force @a[distance=..12]
playsound minecraft:block.gravel.break block @a[distance=..10] ~ ~ ~ 1 0.8 1


# Remove the rubble display entities and nearby event armor stand.
kill @e[type=minecraft:block_display,distance=..2]
kill @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..2]