package moe.nea.firmament.features.inventory

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.recipes.SBCraftingRecipe
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.util.MC

object CraftingOverlay : FirmamentFeature {

    private var screen: GenericContainerScreen? = null
    private var recipe: SBCraftingRecipe? = null
    private val craftingOverlayIndices = listOf(
        10, 11, 12,
        19, 20, 21,
        28, 29, 30,
    )


    fun setOverlay(screen: GenericContainerScreen, recipe: SBCraftingRecipe) {
        this.screen = screen
        this.recipe = recipe
    }

    override val name: String
        get() = "Crafting Overlay"
    override val identifier: String
        get() = "crafting-overlay"

    override fun onLoad() {
        SlotRenderEvents.After.subscribe { event ->
            val slot = event.slot
            val recipe = this.recipe ?: return@subscribe
            if (slot.inventory != screen?.screenHandler?.inventory) return@subscribe
            val recipeIndex = craftingOverlayIndices.indexOf(slot.index)
            if (recipeIndex < 0) return@subscribe
            val expectedItem = recipe.neuRecipe.inputs[recipeIndex]
            val actualStack = slot.stack ?: ItemStack.EMPTY!!
            val actualEntry = SBItemEntryDefinition.getEntry(actualStack).value
            if ((actualEntry.skyblockId.neuItem != expectedItem.itemId || actualEntry.stackSize < expectedItem.amount) && expectedItem.amount.toInt() != 0) {
                event.context.fill(
                    event.slot.x,
                    event.slot.y,
                    event.slot.x + 16,
                    event.slot.y + 16,
                    0x80FF0000.toInt()
                )
            }
            if (!slot.hasStack()) {
                val itemStack = SBItemEntryDefinition.getEntry(expectedItem).asItemEntry().value
                event.context.drawItem(itemStack, event.slot.x, event.slot.y)
                event.context.drawItemInSlot(
                    MC.font,
                    itemStack,
                    event.slot.x,
                    event.slot.y,
                    "${Formatting.RED}${expectedItem.amount.toInt()}"
                )
            }
        }
    }
}
