package cubex2.cs4.plugins.vanilla;

import cubex2.cs4.api.ContentHelper;
import cubex2.cs4.api.InitPhase;
import cubex2.cs4.api.RecipeInput;
import cubex2.cs4.data.SimpleContent;
import cubex2.cs4.plugins.vanilla.crafting.MachineFuel;
import cubex2.cs4.plugins.vanilla.crafting.MachineManager;
import cubex2.cs4.util.CollectionHelper;
import cubex2.cs4.util.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.List;

class MachineFuelImpl extends SimpleContent implements MachineFuel
{
    List<RecipeInput> items;
    int burnTime;
    ResourceLocation fuelList;

    @Override
    public int getBurnTime()
    {
        return burnTime;
    }

    @Override
    public boolean matches(NonNullList<ItemStack> items)
    {
        return CollectionHelper.equalsWithoutOrder(items, this.items, (t, i) -> ItemHelper.stackMatchesRecipeInput(t, i, true));
    }

    @Override
    public List<RecipeInput> getFuelInput()
    {
        return items;
    }

    @Override
    protected void doInit(InitPhase phase, ContentHelper helper)
    {
        MachineManager.addFuel(fuelList, this);
    }

    @Override
    protected boolean isReady()
    {
        return items.stream().allMatch(input -> input.isOreClass() || (input.isItemStack() && input.getStack().isItemLoaded()));
    }
}
