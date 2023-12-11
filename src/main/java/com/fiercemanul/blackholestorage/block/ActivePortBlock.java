package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.gui.ActivePortMenuProvider;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenuProvider;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ActivePortBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;

    public ActivePortBlock() {
        super(Properties
                .of(Material.METAL)
                .strength(10.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .lightLevel(ActivePortBlock::getLightLevel)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .color(MaterialColor.COLOR_BLACK));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, Boolean.FALSE)
                .setValue(SOUTH, Boolean.FALSE)
                .setValue(WEST, Boolean.FALSE)
                .setValue(EAST, Boolean.FALSE)
                .setValue(UP, Boolean.FALSE)
                .setValue(DOWN, Boolean.FALSE)
                .setValue(WATERLOGGED, Boolean.FALSE)
        );
        this.shapesCache = this.getShapeForEachState(ActivePortBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelshape = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
        if (state.getValue(NORTH)) voxelshape = Shapes.or(voxelshape, Block.box(5, 5, 0, 11, 11, 2));
        if (state.getValue(SOUTH)) voxelshape = Shapes.or(voxelshape, Block.box(5, 5, 14, 11, 11, 16));
        if (state.getValue(WEST)) voxelshape = Shapes.or(voxelshape, Block.box(0, 5, 5, 2, 11, 11));
        if (state.getValue(EAST)) voxelshape = Shapes.or(voxelshape, Block.box(14, 5, 5, 16, 11, 11));
        if (state.getValue(DOWN)) voxelshape = Shapes.or(voxelshape, Block.box(5, 0, 5, 11, 2, 11));
        if (state.getValue(UP)) voxelshape = Shapes.or(voxelshape, Block.box(5, 14, 5, 11, 16, 11));
        return voxelshape;
    }

    private static int getLightLevel(BlockState value) {
        if (!value.getValue(NORTH)
                || !value.getValue(SOUTH)
                || !value.getValue(WEST)
                || !value.getValue(EAST)
                || !value.getValue(DOWN)
                || !value.getValue(UP)) return 9;
        return 0;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        VoxelShape shape = shapesCache.get(state);
        if (shape != null) return shape;
        return Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActivePortBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlackHoleStorage.ACTIVE_PORT_BLOCK_ENTITY.get(), ActivePortBlockEntity::tick);
    }


    //方块状态相关
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        BlockPos blockpos = placeContext.getClickedPos();
        FluidState fluidstate = placeContext.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
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
            ActivePortBlockEntity blockEntity = (ActivePortBlockEntity) pLevel.getBlockEntity(pPos);
            if (blockEntity != null) blockEntity.setOwner(player.getUUID());
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState pState, Level level, BlockPos pPos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (!level.isClientSide && !player.isSpectator()) {
            if (level.getBlockEntity(pPos) instanceof ActivePortBlockEntity activePort) {

                if (activePort.hasUser()) {
                    ((ServerPlayer) player).sendSystemMessage(Component.translatable("blackholestorage.tip.using"), true);
                    return InteractionResult.FAIL;
                }

                if (activePort.getOwner() == null) {
                    activePort.setOwner(player.getUUID());
                    activePort.setLocked(false);
                }

                if (activePort.getChannelInfo() == null) NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(activePort, ContainerLevelAccess.create(level, pPos)), buf -> {
                });
                else {
                    Vec3 vec3 = pHit.getLocation().subtract(pPos.getX(), pPos.getY(), pPos.getZ());
                    Direction direction;
                    if (vec3.z <= 0.125) direction = Direction.NORTH;
                    else if (vec3.z >= 0.875) direction = Direction.SOUTH;
                    else if (vec3.x <= 0.125) direction = Direction.WEST;
                    else if (vec3.x >= 0.875) direction = Direction.EAST;
                        //else if (vec3.y <= 0.125) direction = Direction.DOWN;
                    else if (vec3.y >= 0.875) direction = Direction.UP;
                    else direction = Direction.DOWN;
                    NetworkHooks.openScreen((ServerPlayer) player, new ActivePortMenuProvider(activePort, level, pPos), buf -> {
                        buf.writeUUID(activePort.getOwner());
                        buf.writeBoolean(activePort.isLocked());
                        buf.writeBlockPos(pPos);
                        buf.writeUUID(activePort.getChannelInfo().owner());
                        buf.writeUtf(activePort.getChannelName());
                        buf.writeNbt(activePort.northPort.toNbt());
                        buf.writeNbt(activePort.southPort.toNbt());
                        buf.writeNbt(activePort.westPort.toNbt());
                        buf.writeNbt(activePort.eastPort.toNbt());
                        buf.writeNbt(activePort.downPort.toNbt());
                        buf.writeNbt(activePort.upPort.toNbt());
                        buf.writeInt(activePort.rate);
                        buf.writeInt(direction.get3DDataValue());
                    });
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
