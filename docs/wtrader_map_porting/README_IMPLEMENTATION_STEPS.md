# WTrader Trial Chambers + themed gem map profile patch

This patch adds Trial Chambers as a map-based WTrader profile and revises existing specific map profiles so they use themed gem pools instead of the broad `type_gems_all` pool.

## Main changes

Added:
- `wtrader/map_offers/trial_chambers.json`
- `wtrader/profiles/wtraders_map/to_trial_chambers.json`
- `wtrader/trade_pools/minecraft/trial_chambers/trial_chambers_misc.json`
- `wtrader/trade_pools/minecraft/trial_chambers/trial_combat_tools.json`

Added themed gem pools:
- `gems_sky_pillar`: dragon/flying gems
- `gems_ancient_city`: dark gem
- `gems_ocean_maps`: water gem
- `gems_trial_chambers`: fighting gem
- `gems_mega_site`: normal gem
- `gems_wishing_weald`: poison gem

Revised:
- Sky Pillar profile now uses `gems_sky_pillar`
- Ancient City profile now uses `gems_ancient_city`
- Fishing Boat and Shipwreck Cove profiles now use `gems_ocean_maps`
- Mega Site profile now uses `gems_mega_site`
- Wishing Weald profile now uses `gems_wishing_weald`
- `selection_tables/default.json` now includes Trial Chambers in the `single_target` map group

Still disabled:
- Crystal Caves / Diancie files remain under `docs/wtrader_map_porting/disabled_crystal_caves_draft/`

## Install steps

1. Copy the `src` folder into your mod project.
2. This will replace the existing map profile JSONs and `wtrader/selection_tables/default.json`.
3. Run `/reload` or restart the dev server.
4. Watch the log for `[WTrader JSON]` warnings.
5. Test Trial Chambers directly with:

summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/to_trial_chambers"',Invisible:1b,Marker:1b}

## Notes

- The Trial Chambers map offer points to `pp_legendarydungeons:maps/find_trial_chambers`.
- Trial Chambers uses the `pp_legendarydungeons:trial_chamber` structure tag through your existing map loot table.
- `minecraft:ominous_trial_key`, `minecraft:trial_key`, `minecraft:breeze_rod`, and `minecraft:wind_charge` are used in the Trial Chambers misc pool.
