# This function is executed as the move_dig armor stand, at the move_dig armor stand.

# Debug confirmation. Remove later once working.
# tellraw @a[distance=..12] {"text":"Debug: break_blocks is running at the move_dig armor stand.","color":"aqua"}

# Local breaking sounds for nearby players only.
playsound minecraft:block.gravel.break block @a[distance=..12] ~ ~ ~ 1 0.7 1
playsound minecraft:block.stone.break block @a[distance=..12] ~ ~ ~ 0.8 0.8 1
playsound minecraft:item.totem.use master @a[distance=..12] ~ ~ ~ 0.65 1.25 1

# Simple particle effect.
particle minecraft:poof ~ ~1 ~ 1 0.6 1 0.05 60 force @a[distance=..12]

# Replace crying_obsidian within 6 blocks with air.
fill ~-6 ~-6 ~-6 ~6 ~6 ~6 minecraft:air replace minecraft:crying_obsidian

# Replace obsidian within 6 blocks with gravel.
fill ~-6 ~-6 ~-6 ~6 ~6 ~6 minecraft:gravel replace minecraft:obsidian


# Particle burst at each MoveEvent block display near the Dig marker.
# Mostly gray dust, with some brown dirt/gravel color mixed in.

execute at @e[type=minecraft:block_display,tag=MoveEvent,distance=..4] run particle minecraft:dust{color:[0.35,0.35,0.35],scale:1.2} ~ ~0.4 ~ 0.25 0.25 0.25 0.02 10 force @a[distance=..16]

execute at @e[type=minecraft:block_display,tag=MoveEvent,distance=..4] run particle minecraft:dust{color:[0.55,0.48,0.38],scale:1.0} ~ ~0.4 ~ 0.22 0.22 0.22 0.02 4 force @a[distance=..16]

execute at @e[type=minecraft:block_display,tag=MoveEvent,distance=..4] run particle minecraft:block{block_state:"minecraft:gravel"} ~ ~0.4 ~ 0.25 0.25 0.25 0.08 8 force @a[distance=..16]

execute at @e[type=minecraft:block_display,tag=MoveEvent,distance=..4] run particle minecraft:block{block_state:"minecraft:coarse_dirt"} ~ ~0.4 ~ 0.20 0.20 0.20 0.06 3 force @a[distance=..16]

execute at @e[type=minecraft:block_display,tag=MoveEvent,distance=..4] run particle minecraft:poof ~ ~0.5 ~ 0.15 0.15 0.15 0.02 3 force @a[distance=..16]


# Main rocky breaking sounds.
playsound minecraft:block.gravel.break block @a[distance=..12] ~ ~ ~ 1.2 0.7 1
playsound minecraft:block.stone.break block @a[distance=..12] ~ ~ ~ 1.0 0.75 1

# Extra digging texture.
playsound minecraft:block.gravel.place block @a[distance=..12] ~ ~ ~ 0.9 0.55 1
playsound minecraft:block.suspicious_gravel.break block @a[distance=..12] ~ ~ ~ 0.8 0.8 1

# Optional heavier impact. Remove if it feels too loud.
playsound minecraft:block.deepslate.break block @a[distance=..12] ~ ~ ~ 0.7 0.65 1


# Kill nearby block_display entities tagged MoveEvent.
kill @e[type=minecraft:block_display,tag=MoveEvent,distance=..8]

# Finish the event.
function pp_legendarydungeons:other/move_events/dig/cleanup