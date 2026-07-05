# If the player is holding a custom map, determine which explorer map to generate 

# Rayquaza's Sky Pillar (3311101) (pp_legendarydungeons)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311101}}}] run function pp_legendarydungeons:other/maps/load/skypillar_map


# Kyoger's Underwater Ruins (3311102) (pp_legendarydungeons)
# Not Implemented yet

# Groudon's Magma Cavern (3311103) (pp_legendarydungeons)
# Not Implemented yet

# Ancient City (3311104) (Minecraft Vanilla)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311104}}}] run function pp_legendarydungeons:other/maps/load/ancient_city_map

# Shipwreck Cove (3311105) (Cobblemon)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311105}}}] run function pp_legendarydungeons:other/maps/load/shipwreck_cove

# Wrecked Fishing Boat (3311106) (Cobblemon)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311106}}}] run function pp_legendarydungeons:other/maps/load/fishing_boat


# Mega Showdown Modded Structures (Need Mega Showdown Mod installed for them to work):
# Wishing Weald (3311107) (Mega Showdown)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311107}}}] run function pp_legendarydungeons:other/maps/load/mega_showdown_wishing_weald

# Mega Site (3311108) (Mega Showdown)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311108}}}] run function pp_legendarydungeons:other/maps/load/mega_showdown_mega_site


# Diancie's Crystal Caves (3311109) (pp_legendarydungeons)
execute as @a[nbt={SelectedItem:{id:"minecraft:filled_map", components:{"minecraft:rarity":"rare","minecraft:custom_model_data":3311109}}}] run function pp_legendarydungeons:other/maps/load/crystal_caves



# Not Implemented yet