package net.typho.beryllium.mixin.exploring;

import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.structure.DesertTempleGenerator;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.typho.beryllium.Beryllium;
import net.typho.beryllium.exploring.ContainerContentsProcessor;
import net.typho.beryllium.exploring.SusSandProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(DesertTempleGenerator.class)
public abstract class DesertTempleGeneratorMixin extends ShiftableStructurePiece {
    protected DesertTempleGeneratorMixin(StructurePieceType type, int x, int y, int z, int width, int height, int depth, Direction orientation) {
        super(type, x, y, z, width, height, depth, orientation);
    }

    protected DesertTempleGeneratorMixin(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
        super(structurePieceType, nbtCompound);
    }

    /**
     * @author The Typhothanian
     * @reason Custom desert temple
     */
    @Inject(
            method = "generate",
            at = @At("HEAD"),
            cancellable = true
    )
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        if (Beryllium.CONFIG.exploring.structures.desertPyramid) {
            if (adjustToAverageHeight(world, chunkBox, -15)) {
                Objects.requireNonNull(world.getServer())
                        .getStructureTemplateManager()
                        .getTemplate(Beryllium.EXPLORING.id("desert_pyramid"))
                        .orElseThrow()
                        .place(
                                world,
                                new BlockPos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ()),
                                pivot,
                                new StructurePlacementData()
                                        .setMirror(BlockMirror.NONE)
                                        .setRotation(BlockRotation.NONE)
                                        .addProcessor(new SusSandProcessor(LootTables.DESERT_PYRAMID_ARCHAEOLOGY))
                                        .addProcessor(new ContainerContentsProcessor(LootTables.DESERT_PYRAMID_CHEST, Registries.BLOCK.getKey(Blocks.CHEST).orElseThrow()))
                                        .setRandom(random),
                                random,
                                2
                        );
                ci.cancel();
            }
        }
    }
}
