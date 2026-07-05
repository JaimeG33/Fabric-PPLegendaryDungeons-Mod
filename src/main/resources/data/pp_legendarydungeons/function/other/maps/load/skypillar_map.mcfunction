# Give the selected player a generated map to Rayquaza's Sky Pillar dungeon 
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311101}}}] at @s run loot give @s loot pp_legendarydungeons:maps/find_skypillar

# Delete the old map bought from the villager
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311101}}}] run function pp_legendarydungeons:other/maps/clear_oldmap