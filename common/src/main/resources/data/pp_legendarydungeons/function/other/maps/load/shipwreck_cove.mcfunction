# Give selected player a generated map to a Cobblemon Shipwreck Cove structure (3311105)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311105}}}] at @s run loot give @s loot pp_legendarydungeons:maps/cobblemon/shipwreck_coves

# Delete the old map bought from the villager
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311105}}}] run function pp_legendarydungeons:other/maps/clear_oldmap