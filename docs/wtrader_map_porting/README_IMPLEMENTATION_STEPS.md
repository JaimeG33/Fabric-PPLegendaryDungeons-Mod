# WTrader map profile release pack

Crystal Caves / Diancie is intentionally not placed in runtime folders because that dungeon is not expected for this release.
Disabled draft files are under `docs/wtrader_map_porting/disabled_crystal_caves_draft/`.

## Install

1. Copy the `src` folder into the root of your mod project.
2. This replaces `src/main/resources/data/pp_legendarydungeons/wtrader/selection_tables/default.json`.
3. Run `/reload` or restart the dev server.
4. Watch the log for `[WTrader JSON]` warnings.
5. Test exact profiles with these commands:

Sky Pillar:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/to_skypillar"',Invisible:1b,Marker:1b}

Ancient City:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/to_ancient_city"',Invisible:1b,Marker:1b}

Fishing Boat:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/to_fishing_boat"',Invisible:1b,Marker:1b}

Shipwreck Cove:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/to_shipwreck_cove"',Invisible:1b,Marker:1b}

Mega Site:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/mega_showdown/to_mega_site"',Invisible:1b,Marker:1b}

Wishing Weald:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/mega_showdown/to_wishing_weald"',Invisible:1b,Marker:1b}

## Notes

- The map offers point directly to the final map loot tables.
- The old placeholder-map load functions are not used for these trader offers.
- Mega Showdown profiles assume Mega Showdown items/structures exist in the pack.
