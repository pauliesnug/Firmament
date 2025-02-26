package moe.nea.firmament.compat.jade

import snownee.jade.api.ui.IElement
import snownee.jade.api.ui.IElementHelper
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.setSkyBlockId


fun String.jadeId(): Identifier = Identifier.of("firmament", this)

// This drill icon should work for CIT resource packs
val drillItem: ItemStack = Items.PRISMARINE_SHARD.defaultStack.setSkyBlockId(SkyblockId("TITANIUM_DRILL_1"))
val drillIcon: IElement = IElementHelper.get().item(drillItem, 0.5f).message(null)
