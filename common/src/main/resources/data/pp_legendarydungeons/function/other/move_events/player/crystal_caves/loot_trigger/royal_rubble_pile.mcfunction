# This function is running as the custom flint item.
# The execution position is the custom flint item.
# Spawn the actual loot table, then delete only this triggering flint item.

loot spawn ~ ~ ~ loot pp_legendarydungeons:dungeons/crystal_caves/royal_rubble_pile

particle minecraft:poof ~ ~0.25 ~ 0.35 0.2 0.35 0.03 20 force @a[distance=..10]
playsound minecraft:block.gravel.break block @a[distance=..10] ~ ~ ~ 1 0.8 1

kill @s