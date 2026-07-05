# Secret Rayquaza sequence.
# This function is intended to be called AS and AT the local scs_secret_starter armor stand.
# It uses nearest local step markers instead of global limit=1 selectors.

# Step 1: Kyogre / blue orb stand.
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:lapis_block"}] unless score @s scs_secrets matches 1..3 run say hi, I have a blue orb in my hand
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:blueorb"}] unless score @s scs_secrets matches 1..3 run say hi, I have a blue orb in my hand
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:lapis_block"}] unless score @s scs_secrets matches 1..3 run playsound entity.wither.spawn master @a[distance=..30]
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:blueorb"}] unless score @s scs_secrets matches 1..3 run playsound entity.wither.spawn master @a[distance=..30]
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:lapis_block"}] unless score @s scs_secrets matches 1..3 run scoreboard players set @s scs_secrets 1
execute if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step1,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:blueorb"}] unless score @s scs_secrets matches 1..3 run scoreboard players set @s scs_secrets 1

# Step 2: Groudon / red orb stand.
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:redstone_block"}] run say hi, I have a red orb in my hand
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:redorb"}] run say hi, I have a red orb in my hand
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:redstone_block"}] run playsound entity.wither.spawn master @a[distance=..30]
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:redorb"}] run playsound entity.wither.spawn master @a[distance=..30]
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"minecraft:redstone_block"}] run scoreboard players set @s scs_secrets 2
execute if score @s scs_secrets matches 1 if data entity @e[type=minecraft:armor_stand,tag=scs_secret_step2,sort=nearest,limit=1,distance=..100] HandItems[{id:"mega_showdown:redorb"}] run scoreboard players set @s scs_secrets 2

# Step 3: Deoxys / meteorite item on this secret starter.
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"minecraft:nether_star"}] run say hi, I have a meteorite in my hand
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"mega_showdown:deoxys_meteorite"}] run say hi, I have a meteorite in my hand
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"minecraft:nether_star"}] run tellraw @a[distance=0..10] {"bold":true,"color":"dark_gray","hoverEvent":{"action":"show_text","value":[{"text":"You suddenly feel the need for a nether star","color":"dark_red","bold":true}]},"text":"Memories surface of a battle long forgotten..."}
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"mega_showdown:deoxys_meteorite"}] run tellraw @a[distance=0..10] {"bold":true,"color":"dark_gray","hoverEvent":{"action":"show_text","value":[{"text":"You suddenly feel the need for a nether star","color":"dark_red","bold":true}]},"text":"Memories surface of a battle long forgotten..."}
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"minecraft:nether_star"}] run scoreboard players set @s scs_secrets 3
execute if score @s scs_secrets matches 2 if data entity @s HandItems[{id:"mega_showdown:deoxys_meteorite"}] run scoreboard players set @s scs_secrets 3

# Once complete, give only the nearest local player the summon unlock.
execute if score @s scs_secrets matches 3 unless score @s event_triggered matches 1 if entity @a[distance=..20,sort=nearest,limit=1] run scoreboard players set @a[distance=..20,sort=nearest,limit=1] scs_secrets 10
execute if score @s scs_secrets matches 3 unless score @s event_triggered matches 1 run playsound entity.ender_dragon.ambient master @a[distance=..300]
execute if score @s scs_secrets matches 3 unless score @s event_triggered matches 1 if entity @a[distance=..20,sort=nearest,limit=1] run scoreboard players set @s event_triggered 1
