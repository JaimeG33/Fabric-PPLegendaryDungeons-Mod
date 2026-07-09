# WTrader villager porting starter pack

Runtime files can be copied into `src/main/resources`. Draft map profiles are under `docs/wtrader_porting/draft_map_profiles` and should be reviewed before moving into runtime because they require matching `map_offers`.

## Step-by-step

1. Confirm the Java patch for `loot_table` trade-pool entries is installed.
2. Copy the `src` folder into your mod project.
3. Run `/reload` or start the dev server and watch for `[WTrader JSON]` warnings.
4. Add these no-map profiles to `selection_tables/default.json` under `custom_no_map_profiles`:
   - `pp_legendarydungeons:wtraders_nomap/chud_miner`
   - `pp_legendarydungeons:wtraders_nomap/chud_farmer`
   - `pp_legendarydungeons:wtraders_nomap/master_ball_collector`
5. Test exact profile spawn:
   `summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_no_map"],CustomName:'"pp_legendarydungeons:wtraders_nomap/chud_farmer"',Invisible:1b,Marker:1b}`
6. Do not move draft map profiles into runtime until map offer JSONs and map loot tables exist.
7. Once map offers are ready, move reviewed draft profiles into `src/main/resources/data/pp_legendarydungeons/wtrader/profiles/wtraders_map/` and add them to `custom_map_profile_groups`.

## Assumptions

These existing pools are assumed to already exist from your earlier batches:
- `type_gems_all`
- `apricorn_seed_all`
- `saplings_all` or a similar sapling pool
- `apple_leftovers`
- `waterbucket_fishbucket`
- `cobblestone_rocks`

`mega_showdown_specials` requires Mega Showdown to be loaded.
