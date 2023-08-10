package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.BlackHoleStorage;
import com.fiercemanul.blackholestorage.channel.ChannelInfo;
import com.fiercemanul.blackholestorage.channel.ServerChannelManager;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenu;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenuProvider;
import com.fiercemanul.blackholestorage.gui.PassivePortMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PassivePortBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PassivePortBlock() {
        super(Properties.of(Material.METAL)
                .strength(30.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
                .lightLevel(value -> 15)
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
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN, WATERLOGGED);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PassivePortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlackHoleStorage.PASSIVE_PORT_BLOCK_ENTITY.get(), PassivePortBlockEntity::tick);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
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
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        PassivePortBlockEntity blockEntity = (PassivePortBlockEntity) level.getBlockEntity(pos);
        if (blockEntity != null) blockEntity.onBlockStateChange();
    }

    //外观相关

    @Override
    public boolean skipRendering(BlockState state, BlockState state1, Direction direction) {
        return state1.is(this) || super.skipRendering(state, state1, direction);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && !player.isSpectator()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PassivePortBlockEntity passivePort) {

                if (passivePort.hasUser()) {
                    ((ServerPlayer) player).sendSystemMessage(Component.translatable("blackholestorage.tip.using"), true);
                    return InteractionResult.FAIL;
                }

                if (passivePort.getOwner() == null) {
                    passivePort.setOwner(player.getUUID());
                    passivePort.setLocked(false);
                }

                if (passivePort.getChannelInfo() == null)
                    NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(passivePort), buf -> {
                    });
                else {
                    NetworkHooks.openScreen((ServerPlayer) player, new PassivePortMenuProvider(passivePort), buf -> {
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
}
