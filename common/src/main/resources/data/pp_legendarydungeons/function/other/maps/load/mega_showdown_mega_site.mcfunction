# Give selected player a generated map to a MegaShowdown Mega Site underground (the one with the mega stone) (3311108) (Need Mega Showdown Mod installed for it to work)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311108}}}] at @s run loot give @s loot pp_legendarydungeons:maps/mega_showdown/mega_site

# Delete the old map bought from the villager
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311108}}}] run function pp_legendarydungeons:other/maps/clear_oldmap