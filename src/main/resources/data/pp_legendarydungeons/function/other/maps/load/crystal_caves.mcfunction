# Give selected player a generated map to a vanilla Ancient City  (3311104)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311109}}}] at @s run loot give @s loot pp_legendarydungeons:maps/find_crystal_caves

# Delete the old map bought from the villager
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311109}}}] run function pp_legendarydungeons:other/maps/clear_oldmap