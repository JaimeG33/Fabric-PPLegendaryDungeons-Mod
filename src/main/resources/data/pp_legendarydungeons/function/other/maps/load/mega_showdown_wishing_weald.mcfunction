# Give selected player a generated map to a MegaShowdown Wishing Weald (3311107) (Need Mega Showdown Mod installed for it to work)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311107}}}] at @s run loot give @s loot pp_legendarydungeons:maps/mega_showdown/wishing_weald

# Delete the old map bought from the villager
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311107}}}] run function pp_legendarydungeons:other/maps/clear_oldmap