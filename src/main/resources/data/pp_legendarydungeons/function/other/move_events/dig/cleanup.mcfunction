# This function is running as/at the move_dig armor stand.

# Completion message.
tellraw @a[distance=..12] {"text":"Barrier destroyed successfully.","color":"green"}

# Final particle effect.
particle minecraft:poof ~ ~1 ~ 0.8 0.8 0.8 0.05 40 force @a[distance=..12]

# Kill the nearby general MoveEvent marker armor stand.
kill @e[type=minecraft:armor_stand,tag=pp_Move_Event,distance=..10,sort=nearest,limit=1]

# Kill this move_dig armor stand.
kill @s