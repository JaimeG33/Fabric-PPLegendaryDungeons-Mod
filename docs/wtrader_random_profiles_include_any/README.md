# Random map WTrader profiles using include_any_of

These files replace the current placeholder random map profiles.

Runtime path used:
src/main/resources/data/pp_legendarydungeons/wtrader/profiles/wtraders_maps/random/

Included replacements:
- random_misc_map_trader.json
- random_research_outpost_map_trader.json
- random_dungeon_map_trader.json

Each profile now has:
- total_trades: 5
- 1 guaranteed generated map trade
- 4 random non-map trade slots

Important:
These require the Java patch that adds:
tag_filter_mode = "include_any_of"

Interpretation used in this pack:
- Multi-tag include slots use include_any_of, meaning OR behavior.
- Exclude slots still use tag_filter_mode = "exclude".
- Truly random slot uses empty categories and empty tags.

Notes:
- I used saplings_all for the random dungeon category slot. If your actual pool category is different, replace that string.
- If you specifically wanted cheap AND joke_trade instead of cheap OR joke_trade, change that slot's tag_filter_mode from include_any_of to include.
