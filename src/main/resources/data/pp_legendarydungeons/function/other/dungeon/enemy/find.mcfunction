# Function to determine which enemy to spawn where
# Tag must =scs_dungeon_enemy 
# nbt={CustomName:"\"name_of_enemy\""}"}


# Bogged Ranged Sentry (Netherite gear + Max Bow)
execute as @a at @s if entity @e[type=minecraft:armor_stand,tag=scs_dungeon_enemy,distance=..80,nbt={CustomName:"\"bogged_sentry\""}] run function pp_legendarydungeons:other/dungeon/enemy/bogged_sentry
