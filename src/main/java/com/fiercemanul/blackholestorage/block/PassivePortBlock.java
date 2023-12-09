package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenuProvider;
import com.fiercemanul.blackholestorage.gui.PassivePortMenuProvider;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class PassivePortBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;

    public PassivePortBlock() {
        super(Properties.of(Material.METAL)
                .strength(30.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .lightLevel(PassivePortBlock::getLightLevel)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .color(MaterialColor.COLOR_BLACK));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, Boolean.TRUE)
                .setValue(SOUTH, Boolean.TRUE)
                .setValue(WEST, Boolean.TRUE)
                .setValue(EAST, Boolean.TRUE)
                .setValue(UP, Boolean.TRUE)
                .setValue(DOWN, Boolean.TRUE)
                .setValue(WATERLOGGED, Boolean.FALSE)
        );
        this.shapesCache = this.getShapeForEachState(PassivePortBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelshape = Shapes.or(
                Block.box(0, 0, 0, 16, 3, 3),
                Block.box(0, 0, 13, 16, 3, 16),
                Block.box(0, 0, 3, 3, 3, 13),
                Block.box(13, 0, 3, 16, 3, 13),
                Block.box(0, 13, 0, 16, 16, 3),
                Block.box(0, 13, 13, 16, 16, 16),
                Block.box(0, 13, 3, 3, 16, 13),
                Block.box(13, 13, 3, 16, 16, 13),
                Block.box(0, 3, 0, 3, 13, 3),
                Block.box(13, 3, 0, 16, 13, 3),
                Block.box(13, 3, 13, 16, 13, 16),
                Block.box(0, 3, 13, 3, 13, 16)
        );
        if (!state.getValue(NORTH)) voxelshape = Shapes.or(voxelshape, Block.box(3, 3, 0, 13, 13, 1));
        if (!state.getValue(SOUTH)) voxelshape = Shapes.or(voxelshape, Block.box(3, 3, 14, 13, 13, 16));
        if (!state.getValue(WEST)) voxelshape = Shapes.or(voxelshape, Block.box(0, 3, 3, 1, 13, 13));
        if (!state.getValue(EAST)) voxelshape = Shapes.or(voxelshape, Block.box(14, 3, 3, 16, 13, 13));
        if (!state.getValue(DOWN)) voxelshape = Shapes.or(voxelshape, Block.box(3, 0, 3, 13, 1, 13));
        if (!state.getValue(UP)) voxelshape = Shapes.or(voxelshape, Block.box(3, 14, 3, 13, 16, 13));
        return voxelshape;
    }

    private static int getLightLevel(BlockState value) {
        if (value.getValue(NORTH)
                || value.getValue(SOUTH)
                || value.getValue(WEST)
                || value.getValue(EAST)
                || value.getValue(DOWN)
                || value.getValue(UP)) return 15;
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = shapesCache.get(pState);
        if (shape != null) return shape;
        return Shapes.block();
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        VoxelShape shape = shapesCache.get(pState);
        if (shape != null) return shape;
        return Shapes.block();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PassivePortBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlackHoleStorage.PASSIVE_PORT_BLOCK_ENTITY.get(), PassivePortBlockEntity::tick);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }


    //方块状态相关
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        BlockPos blockpos = placeContext.getClickedPos();
        FluidState fluidstate = placeContext.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    @NotNull
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        PassivePortBlockEntity blockEntity = (PassivePortBlockEntity) level.getBlockEntity(pos);
        if (blockEntity != null) blockEntity.onBlockStateChange();
    }

    //外观相关

    @Override
    public @NotNull RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }


    //互动


    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof ServerPlayer player && !pStack.getOrCreateTag().contains("BlockEntityTag")) {
            PassivePortBlockEntity blockEntity = (PassivePortBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) blockEntity.setOwner(player.getUUID());
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && !player.isSpectator()) {
            if (level.getBlockEntity(pos) instanceof PassivePortBlockEntity passivePort) {

                if (passivePort.hasUser()) {
                    ((ServerPlayer) player).sendMessage(new TranslatableComponent("blackholestorage.tip.using"), ChatType.SYSTEM, BlackHoleStorage.FAKE_PLAYER_UUID);
                    return InteractionResult.FAIL;
                }

                if (passivePort.getOwner() == null) {
                    passivePort.setOwner(player.getUUID());
                    passivePort.setLocked(false);
                }

                if (passivePort.getChannelInfo() == null) NetworkHooks.openGui((ServerPlayer) player, new ChannelSelectMenuProvider(passivePort), buf -> {
                });
                else {
                    NetworkHooks.openGui((ServerPlayer) player, new PassivePortMenuProvider(passivePort), buf -> {
                        buf.writeUUID(passivePort.getOwner());
                        buf.writeBoolean(passivePort.isLocked());
                        buf.writeBlockPos(pos);
                        buf.writeUUID(passivePort.getChannelInfo().owner());
                        buf.writeUtf(passivePort.getChannelName());
                    });
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pLevel.isClientSide) return;
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof PassivePortBlockEntity passivePort && pEntity instanceof ItemEntity itemEntity) {
            passivePort.inhaleItem(itemEntity);
        }
    }
}
