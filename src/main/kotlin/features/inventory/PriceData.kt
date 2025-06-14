package moe.nea.firmament.features.inventory

import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.util.FirmFormatters.formatCommas
import moe.nea.firmament.util.bold
import moe.nea.firmament.util.gold
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.yellow

object PriceData : FirmamentFeature {
	override val identifier: String
		get() = "price-data"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val tooltipEnabled by toggle("enable-always") { true }
		val enableKeybinding by keyBindingWithDefaultUnbound("enable-keybind")
	}

	override val config get() = TConfig

	fun formatPrice(label: Text, price: Double): Text {
		return Text.literal("")
			.yellow()
			.bold()
			.append(label)
			.append(": ")
			.append(
				Text.literal(formatCommas(price, fractionalDigits = 1))
					.append(if(price != 1.0) " coins" else " coin")
					.gold()
					.bold()
			)
	}

	@Subscribe
	fun onItemTooltip(it: ItemTooltipEvent) {
		if (!TConfig.tooltipEnabled && !TConfig.enableKeybinding.isPressed()) {
			return
		}
		val sbId = it.stack.skyBlockId
		val bazaarData = HypixelStaticData.bazaarData[sbId]
		val lowestBin = HypixelStaticData.lowestBin[sbId]
		if (bazaarData != null) {
			it.lines.add(Text.literal(""))
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.bazaar.sell-order", "Bazaar Sell Order"),
					bazaarData.quickStatus.sellPrice
				)
			)
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.bazaar.buy-order", "Bazaar Buy Order"),
					bazaarData.quickStatus.buyPrice
				)
			)
		} else if (lowestBin != null) {
			it.lines.add(Text.literal(""))
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.ah.lowestbin", "Lowest BIN"),
					lowestBin
				)
			)
		}
	}
}
