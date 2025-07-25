package net.typho.beryllium.exploring;

import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BiomeParticleConfig;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;
import net.typho.beryllium.Beryllium;
import net.typho.beryllium.Module;

public class Exploring extends Module {
    public final LootFunctionType<ExplorationCompassLootFunction> EXPLORATION_COMPASS = Registry.register(Registries.LOOT_FUNCTION_TYPE, id("exploration_compass"), new LootFunctionType<>(ExplorationCompassLootFunction.CODEC));

    public final ComponentType<DyeColor> COMPASS_NEEDLE_COMPONENT = Registry.register(Registries.DATA_COMPONENT_TYPE, id("needle_color"), ComponentType.<DyeColor>builder().codec(DyeColor.CODEC).build());

    public final StructureProcessorType<StoneBrickVariantProcessor> STONE_BRICK_VARIANT_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR, id("stone_brick_variants"), () -> StoneBrickVariantProcessor.CODEC);
    public final StructureProcessorType<SusSandProcessor> SUS_SAND_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR, id("sus_sand"), () -> SusSandProcessor.CODEC);
    public final StructureProcessorType<ContainerContentsProcessor> CONTAINER_CONTENTS_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR, id("container_contents"), () -> ContainerContentsProcessor.CODEC);

    public final SimpleParticleType FIREFLY_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("firefly"), FabricParticleTypes.simple(false));
    public final SimpleParticleType SPRUCE_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("spruce_leaves"), FabricParticleTypes.simple(false));
    public final SimpleParticleType BIRCH_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("birch_leaves"), FabricParticleTypes.simple(false));

    public final TagKey<Structure> SPAWN_KEY = TagKey.of(RegistryKeys.STRUCTURE, id("spawn"));
    public final TagKey<Biome> HAS_FIREFLIES = TagKey.of(RegistryKeys.BIOME, id("has_fireflies"));
    public final TagKey<Biome> BIRCH_TAG = TagKey.of(RegistryKeys.BIOME, id("birch"));
    public final TagKey<Biome> SPRUCE_TAG = TagKey.of(RegistryKeys.BIOME, id("spruce"));
    public final TagKey<Biome> OAK_TAG = TagKey.of(RegistryKeys.BIOME, id("oak"));

    public final Block FIREFLY_BOTTLE =
            blockWithItem(
                    "firefly_bottle",
                    new FireflyBottleBlock(AbstractBlock.Settings.create()
                            .strength(0f)
                            .pistonBehavior(PistonBehavior.DESTROY)
                            .emissiveLighting((state, world, pos) -> true)
                            .luminance(state -> 3)
                            .breakInstantly()
                            .noBlockBreakParticles()
                            .nonOpaque()
                            .sounds(BlockSoundGroup.GLASS)
                            .suffocates(Blocks::never)
                            .blockVision(Blocks::never)),
            new Item.Settings()
    );
    public final Block DAFFODILS = blockWithItem("daffodils", new FlowerbedBlock(AbstractBlock.Settings.copy(Blocks.PINK_PETALS)), new Item.Settings());
    public final Block SCILLA = blockWithItem("scilla", new FlowerbedBlock(AbstractBlock.Settings.copy(Blocks.PINK_PETALS)), new Item.Settings());
    public final Block GERANIUMS = blockWithItem("geraniums", new FlowerbedBlock(AbstractBlock.Settings.copy(Blocks.PINK_PETALS)), new Item.Settings());
    public final Block ALGAE_BLOCK = block("algae", new AlgaeBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.DARK_GREEN)
            .replaceable()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.GLOW_LICHEN)
            .nonOpaque()
            .burnable()
            .pistonBehavior(PistonBehavior.DESTROY)));

    public final Item METAL_DETECTOR_ITEM = item("metal_detector", new MetalDetectorItem(new Item.Settings()));
    public final Item ALGAE_ITEM = item("algae", new AlgaeItem(ALGAE_BLOCK, new Item.Settings()));
    public final Item EXODINE_INGOT = item("exodine_ingot", new Item(new Item.Settings()));

    public final RiverAlgaeFeature RIVER_ALGAE_FEATURE = Registry.register(Registries.FEATURE, id("river_algae"), new RiverAlgaeFeature());

    public final RegistryKey<ConfiguredFeature<?, ?>> SWAMP_ALGAE_CONFIGURED = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("swamp_algae"));
    public final RegistryKey<ConfiguredFeature<?, ?>> RIVER_ALGAE_CONFIGURED = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("river_algae"));
    public final RegistryKey<ConfiguredFeature<?, ?>> DAFFODILS_CONFIGURED = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("daffodils"));
    public final RegistryKey<ConfiguredFeature<?, ?>> SCILLA_CONFIGURED = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("scilla"));
    public final RegistryKey<ConfiguredFeature<?, ?>> GERANIUMS_CONFIGURED = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id("geraniums"));

    public final RegistryKey<PlacedFeature> SWAMP_ALGAE_PLACED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("swamp_algae"));
    public final RegistryKey<PlacedFeature> RIVER_ALGAE_PLACED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("river_algae"));
    public final RegistryKey<PlacedFeature> DAFFODILS_PLACED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("daffodils"));
    public final RegistryKey<PlacedFeature> SCILLA_PLACED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("scilla"));
    public final RegistryKey<PlacedFeature> GERANIUMS_PLACED = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("geraniums"));

    public Exploring(String name) {
        super(name);
    }

    @Override
    public void onInitialize() {
        FlammableBlockRegistry.getDefaultInstance().add(DAFFODILS, 60, 100);
        FlammableBlockRegistry.getDefaultInstance().add(SCILLA, 60, 100);
        FlammableBlockRegistry.getDefaultInstance().add(GERANIUMS, 60, 100);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> {
                    entries.addAfter(Items.COMPASS, METAL_DETECTOR_ITEM);
                });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register(entries -> {
                    entries.addAfter(Items.PINK_PETALS, DAFFODILS, SCILLA, GERANIUMS);
                });
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.COMPASS, builder -> builder.add(COMPASS_NEEDLE_COMPONENT, DyeColor.RED)));
        Registry.register(Registries.RECIPE_TYPE, id("compass_dye"), new RecipeType<>() {
            @Override
            public String toString() {
                return "exploring/compass_dye";
            }
        });
        Registry.register(Registries.RECIPE_SERIALIZER, id("compass_dye"), CompassDyeRecipe.SERIALIZER);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.SWAMP),
                GenerationStep.Feature.VEGETAL_DECORATION,
                SWAMP_ALGAE_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(BiomeKeys.RIVER),
                GenerationStep.Feature.VEGETAL_DECORATION,
                RIVER_ALGAE_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BIRCH_TAG),
                GenerationStep.Feature.VEGETAL_DECORATION,
                DAFFODILS_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(SPRUCE_TAG),
                GenerationStep.Feature.VEGETAL_DECORATION,
                SCILLA_PLACED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(OAK_TAG),
                GenerationStep.Feature.VEGETAL_DECORATION,
                GERANIUMS_PLACED
        );
        BiomeModifications.create(Beryllium.EXPLORING.id("fireflies"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(Beryllium.EXPLORING.HAS_FIREFLIES), context -> {
                    context.getEffects().setParticleConfig(new BiomeParticleConfig(Beryllium.EXPLORING.FIREFLY_PARTICLE, 0.008f));
                });
        BiomeModifications.create(Beryllium.EXPLORING.id("swamp_water"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(BiomeKeys.SWAMP), context -> {
                    context.getEffects().setWaterColor(0x6D6D5C);
                    context.getEffects().setWaterFogColor(0x6D6D5C);
                });
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            switch (key.getValue().toString()) {
                case "minecraft:chests/village/village_armorer": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.IRON_CHESTPLATE))
                            .with(ItemEntry.builder(Items.IRON_LEGGINGS))
                            .with(ItemEntry.builder(Items.IRON_BOOTS))
                            .with(ItemEntry.builder(Items.SHIELD))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_butcher", "minecraft:chests/village/village_shepherd", "minecraft:chests/village/village_tannery", "minecraft:chests/village/village_temple": {
                    builder.modifyPools(pool -> pool.bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_cartographer": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Beryllium.EXPLORING.FIREFLY_BOTTLE))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_desert_house": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.APPLE).weight(5).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3)))));
                    break;
                }
                case "minecraft:chests/village/village_fisher": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.FISHING_ROD).weight(4))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_fletcher": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.IRON_INGOT).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                            .with(ItemEntry.builder(Items.GRAVEL).weight(4).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 8))))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_mason": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.BRICK).weight(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                            .with(ItemEntry.builder(Items.CRACKED_STONE_BRICKS).weight(2))
                            .with(ItemEntry.builder(Items.CHISELED_STONE_BRICKS).weight(2))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_plains_house": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.WHEAT_SEEDS).weight(3).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 5))))
                            .with(ItemEntry.builder(Beryllium.EXPLORING.GERANIUMS)));
                    break;
                }
                case "minecraft:chests/village/village_savanna_house": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.APPLE).weight(7).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                            .with(ItemEntry.builder(Items.WATER_BUCKET)));
                    break;
                }
                case "minecraft:chests/village/village_snowy_house": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.ICE).weight(5)));
                    break;
                }
                case "minecraft:chests/village/village_taiga_house": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Beryllium.EXPLORING.SCILLA)));
                    break;
                }
                case "minecraft:chests/village/village_toolsmith": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.IRON_AXE).weight(5))
                            .with(ItemEntry.builder(Items.IRON_HOE).weight(5))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/village/village_weaponsmith": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.IRON_LEGGINGS).weight(5))
                            .with(ItemEntry.builder(Items.OBSIDIAN).weight(5).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 7))))
                            .bonusRolls(new ConstantLootNumberProvider(3)));
                    break;
                }
                case "minecraft:chests/jungle_temple": {
                    builder.pool(LootPool.builder()
                            .rolls(new ConstantLootNumberProvider(1))
                            .with(ItemEntry.builder(Items.GOLDEN_HELMET))
                            .with(ItemEntry.builder(Items.GOLDEN_CHESTPLATE))
                            .with(ItemEntry.builder(Items.GOLDEN_LEGGINGS))
                            .with(ItemEntry.builder(Items.GOLDEN_BOOTS))
                    );
                    builder.pool(LootPool.builder()
                            .rolls(new ConstantLootNumberProvider(2))
                            .with(ItemEntry.builder(Items.DIAMOND).weight(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3))))
                            .with(ItemEntry.builder(Items.EMERALD).weight(4).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 4))))
                            .with(ItemEntry.builder(Items.GOLD_INGOT).weight(6).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 4))))
                            .with(ItemEntry.builder(Items.IRON_INGOT).weight(8).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 5))))
                    );
                    break;
                }
                case "minecraft:gameplay/piglin_bartering": {
                    builder.modifyPools(pool -> pool.with(ItemEntry.builder(Items.COMPASS).weight(40)
                                    .apply(new ExplorationCompassLootFunction.Builder()
                                            .withDestination(TagKey.of(RegistryKeys.STRUCTURE, Beryllium.EXPLORING.id("on_bastion_maps")))
                                            .searchRadius(100)
                                            .withSkipExistingChunks(false)
                                    )
                                    .apply(SetNameLootFunction.builder(Text.translatable("item.beryllium.exploring.bastion_compass"), SetNameLootFunction.Target.ITEM_NAME))
                            )
                            .with(ItemEntry.builder(Items.COMPASS).weight(40)
                                    .apply(new ExplorationCompassLootFunction.Builder()
                                            .withDestination(TagKey.of(RegistryKeys.STRUCTURE, Beryllium.EXPLORING.id("on_fortress_maps")))
                                            .searchRadius(100)
                                            .withSkipExistingChunks(false)
                                    )
                                    .apply(SetNameLootFunction.builder(Text.translatable("item.beryllium.exploring.fortress_compass"), SetNameLootFunction.Target.ITEM_NAME))
                            ));

                    break;
                }
            }
        });
    }

    public static class Config extends ConfigSection {
        public MetalDetector metalDetector = new MetalDetector();

        public static class MetalDetector extends ConfigSection {
            public int tooltipRadius = 16, needleX = 16, needleY = 2;
        }

        public Structures structures = new Structures();

        public static class Structures extends ConfigSection {
            public boolean junglePyramid = true;
            public boolean desertPyramid = true;
        }

        public boolean spawnInVillage = true;
    }
}
