package porker.pp_legendarydungeons.features.custom_mobs.mob_registry;

/**
 * Bogged Ranged Sentry.
 *
 * Ported from:
 * src/main/resources/data/pp_legendarydungeons/function/other/dungeon/enemy/bogged_sentry.mcfunction
 */
public final class BoggedSentryDefinition {
    private BoggedSentryDefinition() {
    }

    public static CustomMobDefinition create() {
        return new CustomMobDefinition(
                "bogged_sentry",
                """
                summon minecraft:bogged ~ ~ ~ {PersistenceRequired:1b,HandItems:[{id:"minecraft:bow",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:flame":1,"minecraft:power":5,"minecraft:punch":2,"minecraft:unbreaking":3,"minecraft:vanishing_curse":1}}}},{}],ArmorItems:[{id:"minecraft:netherite_boots",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:feather_falling":4,"minecraft:fire_protection":4,"minecraft:frost_walker":2,"minecraft:soul_speed":3,"minecraft:binding_curse":1,"minecraft:vanishing_curse":1}},"minecraft:trim":{material:"minecraft:iron",pattern:"minecraft:rib"}}},{id:"minecraft:netherite_leggings",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:blast_protection":4,"minecraft:swift_sneak":3,"minecraft:binding_curse":1,"minecraft:vanishing_curse":1}},"minecraft:trim":{material:"minecraft:quartz",pattern:"minecraft:silence"}}},{id:"minecraft:netherite_chestplate",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:projectile_protection":4,"minecraft:binding_curse":1,"minecraft:vanishing_curse":1}},"minecraft:trim":{material:"minecraft:quartz",pattern:"minecraft:silence"}}},{id:"minecraft:iron_helmet",count:1,components:{"minecraft:unbreakable":{},"minecraft:enchantments":{levels:{"minecraft:protection":1,"minecraft:thorns":1,"minecraft:binding_curse":1,"minecraft:vanishing_curse":1}},"minecraft:trim":{material:"minecraft:redstone",pattern:"minecraft:flow"}}}],ArmorDropChances:[0.000F,0.000F,0.000F,0.000F],attributes:[{id:"minecraft:generic.follow_range",base:40}]}
                """.trim()
        );
    }
}
