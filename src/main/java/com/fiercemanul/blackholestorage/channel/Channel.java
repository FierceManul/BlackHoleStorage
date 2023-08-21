package com.fiercemanul.blackholestorage.channel;

import com.fiercemanul.blackholestorage.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class Channel implements IItemHandler, IFluidHandler, IEnergyStorage {

    private String channelName = "UnName";
    public final HashMap<String, Long> storageItems = new HashMap<>();
    public final HashMap<String, Long> storageFluids = new HashMap<>();
    public final HashMap<String, Long> storageEnergies = new HashMap<>();
    public int maxStorageSize = Config.MAX_SIZE_PRE_CHANNEL.get();

    public Channel() {}

    public abstract void onItemChanged(String itemId, boolean listChanged);

    public abstract void onFluidChanged(String fluidId, boolean listChanged);

    public abstract void onEnergyChanged(String energyId, boolean listChanged);

    public int getChannelSize() {
        return storageItems.size() + storageFluids.size() + storageEnergies.size();
    }

    public long getStorageAmount(ItemStack itemStack) {
        return storageItems.getOrDefault(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), 0L);
    }

    public long getStorageAmount(FluidStack fluidStack) {
        return storageFluids.getOrDefault(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString(), 0L);
    }

    public int canStoredAmount(ItemStack itemStack) {
        long a = storageItems.getOrDefault(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString(), 0L);
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public int canStoredAmount(FluidStack fluidStack) {
        long a = storageFluids.getOrDefault(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString(), 0L);
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public int canStoredFluid(String fluidId) {
        long a = storageFluids.getOrDefault(fluidId, 0L);
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public long getStorageEnergy() {
        return storageEnergies.getOrDefault("blackholestorage:forge_energy", 0L);
    }

    public long getStorageEnergy(String energyId) {
        return storageEnergies.getOrDefault(energyId, 0L);
    }

    public int canStoredEnergy() {
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE -  storageEnergies.getOrDefault("blackholestorage:forge_energy", 0L));
    }

    public void addItem(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.isEmpty()) return;
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemId, storageCount + itemStack.getCount());
                itemStack.setCount(0);
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
            }
            onItemChanged(itemId, false);
        } else {
            if (getChannelSize() >= maxStorageSize) return;
            storageItems.put(itemId, (long) itemStack.getCount());
            itemStack.setCount(0);
            onItemChanged(itemId, true);
        }
    }

    public void addFluid(FluidStack fluidStack) {
        if (fluidStack.hasTag() || fluidStack.isEmpty()) return;
        String fluidId = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString();
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= fluidStack.getAmount()) {
                storageFluids.replace(fluidId, storageAmount + fluidStack.getAmount());
                fluidStack.setAmount(0);
            } else {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                fluidStack.setAmount(fluidStack.getAmount() - (int) remainingSpaces);
            }
            onFluidChanged(fluidId, false);
        } else {
            if (getChannelSize() >= maxStorageSize) return;
            storageFluids.put(fluidId, (long) fluidStack.getAmount());
            fluidStack.setAmount(0);
            onFluidChanged(fluidId, true);
        }
    }

    public long addItem(String itemId, long count) {
        if (itemId.equals("minecraft:air") || count == 0) return 0;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                storageItems.replace(itemId, storageCount + count);
                onItemChanged(itemId, false);
                return 0;
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                onItemChanged(itemId, false);
                return count - remainingSpaces;
            }
        } else {
            storageItems.put(itemId, count);
            onItemChanged(itemId, true);
            return 0L;
        }
    }

    public long addFluid(String fluidId, long count) {
        if (fluidId.equals("minecraft:air") || count == 0) return 0;
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageFluids.replace(fluidId, storageAmount + count);
                onFluidChanged(fluidId, false);
                return 0;
            } else {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                onFluidChanged(fluidId, false);
                return count - remainingSpaces;
            }
        } else {
            storageFluids.put(fluidId, count);
            onFluidChanged(fluidId, true);
            return 0L;
        }
    }

    public long addEnergy(long count) {
        if (storageEnergies.containsKey("blackholestorage:forge_energy")) {
            long storageAmount = storageEnergies.get("blackholestorage:forge_energy");
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageEnergies.replace("blackholestorage:forge_energy", storageAmount + count);
                onEnergyChanged("blackholestorage:forge_energy", false);
                return 0;
            } else {
                storageEnergies.replace("blackholestorage:forge_energy", Long.MAX_VALUE);
                onEnergyChanged("blackholestorage:forge_energy", false);
                return count - remainingSpaces;
            }
        } else {
            storageEnergies.put("blackholestorage:forge_energy", count);
            onEnergyChanged("blackholestorage:forge_energy", true);
            return 0;
        }
    }

    public long addEnergy(String energyId, long count) {
        if (storageEnergies.containsKey(energyId)) {
            long storageAmount = storageEnergies.get(energyId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageEnergies.replace(energyId, storageAmount + count);
                onEnergyChanged(energyId, false);
                return 0;
            } else {
                storageEnergies.replace(energyId, Long.MAX_VALUE);
                onEnergyChanged(energyId, false);
                return count - remainingSpaces;
            }
        } else {
            storageEnergies.put(energyId, count);
            onEnergyChanged(energyId, true);
            return 0;
        }
    }

    /**
     * 填充物品叠堆，不限制数量。
     *
     * @param itemStack 要填充的物品
     * @param count     要填充的数量，负数为扣除。
     */
    public void fillItemStack(ItemStack itemStack, int count) {
        if (itemStack.isEmpty() || count == 0 || itemStack.hasTag()) return;
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemId);
                itemStack.setCount(itemStack.getCount() + (int) storageCount);
                onItemChanged(itemId, true);
            } else if (remainingSpaces < -count) {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemId, false);
            } else {
                storageItems.replace(itemId, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, false);
            }
        } else {
            if (count < 0) {
                if (getChannelSize() >= maxStorageSize) return;
                storageItems.put(itemId, (long) -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, true);
            }
        }
    }

    public void fillFluidStack(FluidStack fluidStack, int count) {
        if (fluidStack.isEmpty() || count == 0 || fluidStack.hasTag()) return;
        String fluidId = ForgeRegistries.FLUIDS.getKey(fluidStack.getRawFluid()).toString();
        if (storageFluids.containsKey(fluidId)) {
            long storageCount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageFluids.remove(fluidId);
                fluidStack.setAmount(fluidStack.getAmount() + (int) storageCount);
                onFluidChanged(fluidId, true);
            } else if (remainingSpaces < -count) {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                fluidStack.setAmount(fluidStack.getAmount() - (int) remainingSpaces);
                onFluidChanged(fluidId, false);
            } else {
                storageFluids.replace(fluidId, storageCount - count);
                fluidStack.setAmount(fluidStack.getAmount() + count);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (count < 0) {
                if (getChannelSize() >= maxStorageSize) return;
                storageFluids.put(fluidId, (long) -count);
                fluidStack.setAmount(fluidStack.getAmount() + count);
                onFluidChanged(fluidId, true);
            }
        }
    }

    /**
     * 从频道获取物品，但不限制数量。
     */
    public ItemStack takeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), count);
    }

    public FluidStack takeFluid(String fluidId, int count) {
        if (!storageFluids.containsKey(fluidId) || fluidId.equals("minecraft:air") || count == 0) return FluidStack.EMPTY;
        long storageAmount = storageFluids.get(fluidId);
        if (count < storageAmount) {
            storageFluids.replace(fluidId, storageAmount - count);
            onFluidChanged(fluidId, false);
        } else {
            storageFluids.remove(fluidId);
            count = (int) storageAmount;
            onFluidChanged(fluidId, true);
        }
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)), count);
    }

    /**
     * 从频道获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), 1);
        count = Integer.min(count, itemStack.getMaxStackSize());
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public ItemStack saveTakeItem(String itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), 1);
        int count = half ? (itemStack.getMaxStackSize() + 1) / 2 : itemStack.getMaxStackSize();
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public void removeItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        String itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        long storageCount = storageItems.get(itemId);
        if (itemStack.getCount() < storageCount) {
            storageItems.replace(itemId, storageCount - itemStack.getCount());
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            onItemChanged(itemId, true);
        }
    }

    public void removeEnergy(Long amount) {
        if (!storageEnergies.containsKey("blackholestorage:forge_energy")) return;
        long storageCount = storageEnergies.get("blackholestorage:forge_energy");
        if (amount < storageCount) {
            storageEnergies.replace("blackholestorage:forge_energy", storageCount - amount);
            onEnergyChanged("blackholestorage:forge_energy", false);
        } else {
            storageEnergies.remove("blackholestorage:forge_energy");
            onEnergyChanged("blackholestorage:forge_energy", true);
        }
    }

    public void removeEnergy(String energyId, Long amount) {
        if (!storageEnergies.containsKey(energyId)) return;
        long storageCount = storageEnergies.get(energyId);
        if (amount < storageCount) {
            storageEnergies.replace(energyId, storageCount - amount);
            onEnergyChanged(energyId, false);
        } else {
            storageEnergies.remove(energyId);
            onEnergyChanged(energyId, true);
        }
    }

    public boolean isEmpty() {
        return storageItems.isEmpty() && storageFluids.isEmpty() && storageEnergies.isEmpty();
    }

    public abstract boolean isRemoved();

    public String getName() {
        return channelName;
    }

    public void setName(String channelName) {
        this.channelName = channelName.substring(0, Math.min(channelName.length(), 64));
    }


    @Override
    public int getSlots() {
        return storageItems.size() + 9;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot >= storageItems.size()) return ItemStack.EMPTY;
        String itemName = storageItems.keySet().toArray(new String[]{})[slot];
        long count = Math.min(Integer.MAX_VALUE, storageItems.get(itemName));
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)), (int) count);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.hasTag()) return stack;
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= stack.getCount()) {
                if (!simulate) storageItems.replace(itemId, storageCount + stack.getCount());
            } else {
                if (!simulate) storageItems.replace(itemId, Long.MAX_VALUE);
                remainingStack = stack.copy();
                remainingStack.setCount(stack.getCount() - (int) remainingSpaces);
            }
            if (!simulate) onItemChanged(itemId, false);
        } else {
            if (getChannelSize() >= maxStorageSize) return stack;
            if (!simulate) {
                storageItems.put(itemId, (long) stack.getCount());
                onItemChanged(itemId, true);
            }
        }
        return remainingStack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= storageItems.size()) return ItemStack.EMPTY;
        String itemId = storageItems.keySet().toArray(new String[]{})[slot];
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), 1);
        int count = Math.min(itemStack.getMaxStackSize(), amount);
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            if (!simulate) {
                storageItems.replace(itemId, storageCount - count);
                onItemChanged(itemId, false);
            }
        } else {
            if (!simulate) {
                storageItems.remove(itemId);
                onItemChanged(itemId, true);
            }
            count = (int) storageCount;
        }
        itemStack.setCount(count);
        return itemStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }


    @Override
    public int getTanks() {
        return storageFluids.size() + 9;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank >= storageFluids.size()) return FluidStack.EMPTY;
        String fluidName = storageFluids.keySet().toArray(new String[]{})[tank];
        long count = Math.min(Integer.MAX_VALUE, storageFluids.get(fluidName));
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)), (int) count);
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.hasTag()) return 0;
        String fluidId = ForgeRegistries.FLUIDS.getKey(resource.getFluid()).toString();
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= resource.getAmount()) {
                if (action == FluidAction.EXECUTE) {
                    storageFluids.replace(fluidId, storageAmount + resource.getAmount());
                    onFluidChanged(fluidId, false);
                }
                return resource.getAmount();
            } else {
                if (action == FluidAction.EXECUTE) {
                    storageFluids.replace(fluidId, Long.MAX_VALUE);
                    onFluidChanged(fluidId, false);
                }
                return (int) remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxStorageSize) return 0;
            if (action == FluidAction.EXECUTE) {
                storageFluids.put(fluidId, (long) resource.getAmount());
                onFluidChanged(fluidId, true);
            }
            return resource.getAmount();
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        String fluidId = ForgeRegistries.FLUIDS.getKey(resource.getFluid()).toString();
        if (!storageFluids.containsKey(fluidId) || resource.getAmount() <= 0) return FluidStack.EMPTY;
        long storageAmount = storageFluids.get(fluidId);
        int count = resource.getAmount();
        if (count < storageAmount) {
            if (action == FluidAction.EXECUTE) {
                storageFluids.replace(fluidId, storageAmount - count);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (action == FluidAction.EXECUTE) {
                storageFluids.remove(fluidId);
                onFluidChanged(fluidId, true);
            }
            count = (int) storageAmount;
        }
        return new FluidStack(resource, count);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (storageFluids.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        String fluidId = storageFluids.keySet().toArray(new String[]{})[0];
        long storageAmount = storageFluids.get(fluidId);
        if (maxDrain < storageAmount) {
            if (action == FluidAction.EXECUTE) {
                storageFluids.replace(fluidId, storageAmount - maxDrain);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (action == FluidAction.EXECUTE) {
                storageFluids.remove(fluidId);
                onFluidChanged(fluidId, true);
            }
            maxDrain = (int) storageAmount;
        }
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)), maxDrain);
    }



    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (storageEnergies.containsKey("blackholestorage:forge_energy")) {
            long storageAmount = storageEnergies.get("blackholestorage:forge_energy");
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= maxReceive) {
                if (!simulate) {
                    storageEnergies.replace("blackholestorage:forge_energy", storageAmount + maxReceive);
                    onEnergyChanged("blackholestorage:forge_energy", false);
                }
                return maxReceive;
            } else {
                if (!simulate) {
                    storageEnergies.replace("blackholestorage:forge_energy", Long.MAX_VALUE);
                    onEnergyChanged("blackholestorage:forge_energy", false);
                }
                return (int) remainingSpaces;
            }
        } else {
            if (!simulate) {
                storageEnergies.put("blackholestorage:forge_energy", (long) maxReceive);
                onEnergyChanged("blackholestorage:forge_energy", true);
            }
            return maxReceive;
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!storageEnergies.containsKey("blackholestorage:forge_energy")) return 0;
        long storageCount = storageEnergies.get("blackholestorage:forge_energy");
        if (maxExtract < storageCount) {
            if (!simulate) {
                storageEnergies.replace("blackholestorage:forge_energy", storageCount - maxExtract);
                onEnergyChanged("blackholestorage:forge_energy", false);
            }
            return maxExtract;
        } else {
            if (!simulate) {
                storageEnergies.remove("blackholestorage:forge_energy");
                onEnergyChanged("blackholestorage:forge_energy", true);
            }
            return (int) storageCount;
        }
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, storageEnergies.getOrDefault("blackholestorage:forge_energy", 0L));
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return storageEnergies.containsKey("blackholestorage:forge_energy");
    }

    @Override
    public boolean canReceive() {
        return storageEnergies.getOrDefault("blackholestorage:forge_energy", 0L) < Long.MAX_VALUE;
    }
}
