# WTrader random map odds fix

This restores the intended custom-map branch odds:
- 45% single-target map trader
- 30% random misc / strange structure map trader
- 20% random abandoned research outpost map trader
- 5% random dungeon map trader

The top-level 20/40/40 roll remains unchanged.

## Additions
- wtrader/map_offers/random_misc.json
- wtrader/map_offers/random_research_outpost.json
- wtrader/map_offers/random_dungeon.json
- wtrader/profiles/wtraders_map/random_misc_map_trader.json
- wtrader/profiles/wtraders_map/random_research_outpost_map_trader.json
- wtrader/profiles/wtraders_map/random_dungeon_map_trader.json

## Replacement
- wtrader/selection_tables/default.json

## Test commands

Normal 20/40/40:
summon armor_stand ~ ~ ~ {Tags:["pp_wtraders"],Invisible:1b,Marker:1b}

Map-only roll:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_profiletype","custom_map"],Invisible:1b,Marker:1b}

Exact random misc:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/random_misc_map_trader"',Invisible:1b,Marker:1b}

Exact random research:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/random_research_outpost_map_trader"',Invisible:1b,Marker:1b}

Exact random dungeon:
summon armor_stand ~ ~ ~ {Tags:["pp_wtrader_specificprofile","custom_map"],CustomName:'"pp_legendarydungeons:wtraders_map/random_dungeon_map_trader"',Invisible:1b,Marker:1b}

## Note
These three new profiles are placeholders with only the map trade. Later, increase total_trades and add random_slots without changing the group odds.
