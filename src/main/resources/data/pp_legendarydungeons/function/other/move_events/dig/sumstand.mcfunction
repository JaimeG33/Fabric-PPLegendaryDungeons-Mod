# Summons an armor stand with the required move_dig tag to work with the move_event logic
# This is the part that measures where to break the blocks
execute as @p at @p run summon armor_stand ~ ~ ~ {NoGravity:1b,Invulnerable:1b,Invisible:1b,Tags:["move_dig"]}
# Runs at the location of the player for debug and building purposes