package cubex2.cs4.plugins.vanilla.block;

import com.google.common.collect.Lists;
import cubex2.cs4.CustomStuff4;
import cubex2.cs4.api.WrappedItemStack;
import cubex2.cs4.plugins.vanilla.*;
import cubex2.cs4.util.IntRange;
import cubex2.cs4.util.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class BlockMixin extends Block implements CSBlock<ContentBlockBase>
{
    public static final AxisAlignedBB DEFAULT_AABB_MARKER = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public BlockMixin(Material materialIn)
    {
        super(materialIn);
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getSubtype(state);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        Optional<BlockDrop[]> drops = getContent().drop.get(getSubtype(state));
        if (drops.isPresent())
        {
            List<ItemStack> result = Lists.newArrayList();

            for (BlockDrop drop : drops.get())
            {
                WrappedItemStack wrappedItemStack = drop.getItem();
                ItemStack droppedStack = wrappedItemStack.getItemStack();

                if (droppedStack != null)
                {
                    int amount = drop.getAmount();
                    if (amount > 0)
                    {
                        result.add(ItemHelper.copyStack(droppedStack, amount));
                    }
                }
            }

            return result;

        } else
        {
            return super.getDrops(world, pos, state, fortune);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return getContent().canSilkHarvest.get(getSubtype(state)).orElse(true);
    }

    @Override
    protected ItemStack createStackedBlock(IBlockState state)
    {
        Item item = Item.getItemFromBlock(this);
        int subtype = 0;

        if (item.getHasSubtypes())
        {
            subtype = getSubtype(state);
        }

        return new ItemStack(item, 1, subtype);
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state)
    {
        return getContent().harvestTool.get(getSubtype(state)).orElse(null);
    }

    @Override
    public int getHarvestLevel(IBlockState state)
    {
        return getContent().harvestLevel.get(getSubtype(state)).orElse(-1);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return getContent().isFullCube.get(getSubtype(state)).orElse(true);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        // Block is calling this in the constructor...
        if (getContent() == null)
            return true;

        return getContent().isOpaqueCube.get(getSubtype(state)).orElse(true);
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        BlockRenderLayer layer = getContent().renderLayer;
        return layer != null ? layer : super.getBlockLayer();
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return getContent().hardness.get(getSubtype(blockState)).orElse(1f);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
    {
        IBlockState blockState = world.getBlockState(pos);
        return getContent().resistance.get(getSubtype(blockState)).orElse(0f) / 5f;
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity)
    {
        return getContent().soundType.get(getSubtype(state)).orElse(SoundType.STONE);
    }

    @Override
    public int getLightOpacity(IBlockState state)
    {
        return getContent().opacity.get(getSubtype(state)).orElse(255);
    }

    @Override
    public int getLightValue(IBlockState state)
    {
        return getContent().light.get(getSubtype(state)).orElse(0);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().flammability.get(getSubtype(state)).orElse(0) * 3; // 300 is 100%
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().fireSpreadSpeed.get(getSubtype(state)).orElse(0);
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().isFireSource.get(getSubtype(state)).orElse(false);
    }

    @Override
    public boolean isWood(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().isWood.get(getSubtype(state)).orElse(false);
    }

    @Override
    public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return getContent().canSustainLeaves.get(getSubtype(state)).orElse(false);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable)
    {
        EnumPlantType type = plantable.getPlantType(world, pos.offset(direction));

        EnumPlantType[] sustainedPlants = getContent().sustainedPlants.get(getSubtype(state)).orElse(null);
        if (sustainedPlants != null)
        {
            return ArrayUtils.contains(sustainedPlants, type);
        } else
        {
            return super.canSustainPlant(state, world, pos, direction, plantable);
        }
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().isBeaconBase.get(getSubtype(state)).orElse(false);
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().enchantPowerBonus.get(getSubtype(state)).orElse(0f);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
    {
        return getContent().expDrop.get(getSubtype(state)).orElse(IntRange.ZERO).getRandomValue();
    }

    @Override
    public MapColor getMapColor(IBlockState state)
    {
        return getContent().mapColor.get(getSubtype(state)).orElse(getMaterial(state).getMaterialMapColor());
    }

    @Override
    public boolean isBurning(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        return getContent().isBurning.get(getSubtype(state)).orElse(false);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        if (side == EnumFacing.DOWN && !getContent().canPlaceOnCeiling)
            return false;
        if (side == EnumFacing.UP && !getContent().canPlaceOnFloor)
            return false;
        if (side.getAxis().isHorizontal() && !getContent().canPlaceOnSides)
            return false;

        return super.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        AxisAlignedBB bounds = getContent().bounds.get(getSubtype(state)).orElse(DEFAULT_AABB_MARKER);
        return bounds == DEFAULT_AABB_MARKER ? super.getBoundingBox(state, source, pos) : bounds;
    }

    @Nullable
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        AxisAlignedBB bounds = getContent().selectionBounds.get(getSubtype(state)).orElse(null);
        if (bounds == DEFAULT_AABB_MARKER)
            return super.getSelectedBoundingBox(state, worldIn, pos);
        else
            return bounds != null ? bounds.offset(pos) : null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        AxisAlignedBB bounds = getContent().collisionBounds.get(getSubtype(state)).orElse(null);
        if (bounds == DEFAULT_AABB_MARKER)
            return super.getCollisionBoundingBox(state, worldIn, pos);
        else
            return bounds;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
        String[] lines = getContent().information.get(stack.getMetadata()).orElse(new String[0]);
        tooltip.addAll(Arrays.asList(lines));
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.addAll(ItemHelper.createSubItems(itemIn, tab, getContent().creativeTab, getSubtypes()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        int subtype = getSubtype(state);
        return getContent().tileEntity.hasEntry(subtype)
               && getContent().tileEntity.get(subtype).isPresent();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        if (hasTileEntity(state))
        {
            Optional<ResourceLocation> optional = getContent().tileEntity.get(getSubtype(state));
            if (optional.isPresent())
            {
                return TileEntityRegistry.createTileEntity(optional.get());
            } else
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack stack, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (playerIn.isSneaking())
        {
            if (interactWithFluidItem(worldIn, pos, state, playerIn, hand, facing)) return true;
            if (openGui(worldIn, pos, state, playerIn)) return true;
        } else
        {
            if (openGui(worldIn, pos, state, playerIn)) return true;
            if (interactWithFluidItem(worldIn, pos, state, playerIn, hand, facing)) return true;
        }

        return false;
    }

    private boolean interactWithFluidItem(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing)
    {
        if (getContent().canInteractWithFluidItem.get(getSubtype(state)).orElse(true))
        {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing))
            {
                if (worldIn.isRemote)
                {
                    return true;
                }

                IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);

                if (FluidUtil.interactWithFluidHandler(playerIn.getHeldItem(hand), fluidHandler, playerIn))
                {
                    playerIn.inventoryContainer.detectAndSendChanges();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean openGui(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn)
    {
        Optional<ContentGuiBase> gui = getGui(state);

        if (gui.isPresent())
        {
            if (worldIn.isRemote)
            {
                return true;
            } else
            {
                playerIn.openGui(CustomStuff4.INSTANCE, gui.get().getGuiId(), worldIn, pos.getX(), pos.getY(), pos.getZ());

                return true;
            }
        }

        return false;
    }


    private Optional<ContentGuiBase> getGui(IBlockState state)
    {
        Optional<ResourceLocation> location = getContent().gui.get(getSubtype(state));
        if (location.isPresent())
        {
            ContentGuiBase gui = GuiRegistry.get(location.get());
            if (gui == null)
            {
                FMLLog.warning("Missing GUI %s", location.get());
            }
            return Optional.ofNullable(gui);
        }
        return Optional.empty();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ContentBlockBaseWithSubtypes.insertSubtype(getProperties()));
    }
}
