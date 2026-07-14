# Summons an armor stand with the required pp_Move_Event tag to work with the move_event logic
# This is the part that shows the particles and scans for players
execute as @p at @p run summon armor_stand ~ ~ ~ {NoGravity:1b,Invulnerable:1b,Invisible:1b,Tags:["pp_Move_Event"]}
# Runs at the location of the player for debug and building purposes