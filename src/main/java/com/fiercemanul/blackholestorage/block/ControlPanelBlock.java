package com.fiercemanul.blackholestorage.block;

import com.fiercemanul.blackholestorage.channel.ClientChannelManager;
import com.fiercemanul.blackholestorage.gui.ChannelSelectMenuProvider;
import com.fiercemanul.blackholestorage.gui.ControlPanelMenuProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ControlPanelBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ControlPanelBlock() {
        super(Properties
                .of(Material.METAL)
                .strength(20.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .lightLevel(value -> 4)
                .isValidSpawn((state, getter, pos, entityType) -> false)
                .isSuffocating((state, getter, pos) -> false)
                .color(MaterialColor.COLOR_BLACK));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControlPanelBlockEntity(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        if (Minecraft.getInstance().player == null) return;
        if (!pStack.hasTag()) return;
        if (pStack.getTag().contains("BlockEntityTag")) {
            CompoundTag nbt = pStack.getTag().getCompound("BlockEntityTag");
            if (nbt.contains("owner")) {
                UUID selfUUID = Minecraft.getInstance().player.getUUID();
                UUID ownerUUID = nbt.getUUID("owner");
                String ownerName = ClientChannelManager.getInstance().getUserName(nbt.getUUID("owner"));
                boolean lock = nbt.getBoolean("locked");
                if (selfUUID.equals(ownerUUID)) pTooltip.add(Component.translatable("bhs.GUI.owner", "§a" + ownerName));
                else if (lock) pTooltip.add(Component.translatable("bhs.GUI.owner", "§c" + ownerName));
                else pTooltip.add(Component.translatable("bhs.GUI.owner", ownerName));
            }
        }
    }



    //方块状态相关
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        FluidState fluidstate = placeContext.getLevel().getFluidState(placeContext.getClickedPos());
        boolean isHorizontal = placeContext.getClickedFace().getAxis().isHorizontal();
        return this.defaultBlockState()
                .setValue(FACING, isHorizontal ? placeContext.getClickedFace().getOpposite() : placeContext.getHorizontalDirection())
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH -> {
                return Block.box(3.0D, 1.0D, 0.0D, 13.0D, 15.0D, 2.0D);
            }
            case SOUTH -> {
                return Block.box(3.0D, 1.0D, 14.0D, 13.0D, 15.0D, 16.0D);
            }
            case WEST -> {
                return Block.box(0.0D, 1.0D, 3.0D, 2.0D, 15.0D, 13.0D);
            }
            case EAST -> {
                return Block.box(14.0D, 1.0D, 3.0D, 16.0D, 15.0D, 13.0D);
            }
        }
        return Shapes.block();
    }

    @Override
    @NotNull
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }


    //互交

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof ServerPlayer && !pStack.getOrCreateTag().contains("BlockEntityTag")) {
            ControlPanelBlockEntity panelBlock = (ControlPanelBlockEntity) pLevel.getBlockEntity(pPos);
            panelBlock.setOwner(pPlacer.getUUID());
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && !player.isSpectator()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ControlPanelBlockEntity controlPanelBlockEntity) {
                if (controlPanelBlockEntity.getChannelInfo() != null) {
                    UUID owner;
                    if (controlPanelBlockEntity.getOwner() == null) {
                        owner = player.getUUID();
                        controlPanelBlockEntity.setOwner(owner);
                        controlPanelBlockEntity.setLocked(false);
                    } else owner = controlPanelBlockEntity.getOwner();
                    NetworkHooks.openScreen((ServerPlayer) player, new ControlPanelMenuProvider(controlPanelBlockEntity), buf -> {
                        buf.writeBlockPos(pos);
                        buf.writeInt(-2);
                        buf.writeUUID(owner);
                        buf.writeBoolean(controlPanelBlockEntity.isLocked());
                        buf.writeBoolean(controlPanelBlockEntity.getCraftingMode());
                        buf.writeUtf(controlPanelBlockEntity.getFilter(), 64);
                        buf.writeByte(controlPanelBlockEntity.getSortType());
                        buf.writeByte(controlPanelBlockEntity.getViewType());
                        buf.writeUUID(controlPanelBlockEntity.getChannelOwner());
                        buf.writeInt(controlPanelBlockEntity.getChannelID());
                    });
                } else {
                    NetworkHooks.openScreen((ServerPlayer) player, new ChannelSelectMenuProvider(controlPanelBlockEntity), buf -> {});
                }
            }
        }
        return InteractionResult.SUCCESS;
    }


}
