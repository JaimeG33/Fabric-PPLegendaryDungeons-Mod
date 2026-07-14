# This function is executing as the selected player.

# Show subtle hint particles only to players near the event.
execute as @a at @s if entity @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..12,sort=nearest,limit=1] run function pp_legendarydungeons:other/move_events/hint_particles/astand_moveevent_2

# Give the nearest move_dig armor stand a score if it does not already have one.
scoreboard players add @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1] event_triggered 0

# Show the suspicious-ground message once.
execute at @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=0}] run tellraw @s {"text":"This ground seems suspicious... maybe a Pokémon can use Dig here?","color":"yellow"}

# After showing the hint, set the armor stand to -2.
# -2 = waiting for a valid Pokémon.
execute as @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=0}] run scoreboard players set @s event_triggered -2

# If a player-owned Pokémon with Dig is within 4 blocks of the Dig marker, announce the move.
execute at @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=-2}] if entity @e[type=cobblemon:pokemon,distance=..4,nbt={Pokemon:{PokemonOriginalTrainerType:"PLAYER",MoveSet:[{MoveName:"dig"}]}}] run tellraw @s {"text":"Pokémon, use Dig!","color":"gold"}

# Local pre-dig sound for the selected player only.
execute at @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=-2}] if entity @e[type=cobblemon:pokemon,distance=..4,nbt={Pokemon:{PokemonOriginalTrainerType:"PLAYER",MoveSet:[{MoveName:"dig"}]}}] run playsound minecraft:block.gravel.break block @s ~ ~ ~ 1 0.6 1

# Start the timer function.
# This is scheduled BEFORE setting the score to 30 because the selector still needs event_triggered=-2.
execute at @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=-2}] if entity @e[type=cobblemon:pokemon,distance=..4,nbt={Pokemon:{PokemonOriginalTrainerType:"PLAYER",MoveSet:[{MoveName:"dig"}]}}] run schedule function pp_legendarydungeons:other/move_events/dig/timer 1t replace

# Start the 1.5 second countdown on that specific move_dig armor stand.
execute as @e[type=minecraft:armor_stand,tag=move_dig,distance=..5,sort=nearest,limit=1,scores={event_triggered=-2}] at @s if entity @e[type=cobblemon:pokemon,distance=..4,nbt={Pokemon:{PokemonOriginalTrainerType:"PLAYER",MoveSet:[{MoveName:"dig"}]}}] run scoreboard players set @s event_triggered 30
