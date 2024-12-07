package moe.nea.firmament.features.texturepack

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.item.ItemModelManager
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.client.render.item.model.ItemModel
import net.minecraft.client.render.item.model.ItemModelTypes
import net.minecraft.client.render.model.ResolvableModel
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ModelTransformationMode

class PredicateModel {
	data class Baked(
		val fallback: ItemModel,
		val overrides: List<Override>
	) : ItemModel {
		data class Override(
			val model: ItemModel,
			val predicate: FirmamentModelPredicate,
		)

		override fun update(
			state: ItemRenderState,
			stack: ItemStack,
			resolver: ItemModelManager,
			transformationMode: ModelTransformationMode,
			world: ClientWorld?,
			user: LivingEntity?,
			seed: Int
		) {
			val model =
				overrides
					.find { it.predicate.test(stack) }
					?.model
					?: fallback
			model.update(state, stack, resolver, transformationMode, world, user, seed)
		}
	}

	data class Unbaked(
		val fallback: ItemModel.Unbaked,
		val overrides: List<Override>,
	) : ItemModel.Unbaked {
		companion object {
			val OVERRIDE_CODEC: Codec<Override> = RecordCodecBuilder.create {
				it.group(
					ItemModelTypes.CODEC.fieldOf("model").forGetter(Override::model),
					CustomModelOverrideParser.LEGACY_CODEC.fieldOf("predicate").forGetter(Override::predicate),
				).apply(it, Unbaked::Override)
			}
			val CODEC: MapCodec<Unbaked> =
				RecordCodecBuilder.mapCodec {
					it.group(
						ItemModelTypes.CODEC.fieldOf("fallback").forGetter(Unbaked::fallback),
						OVERRIDE_CODEC.listOf().fieldOf("overrides").forGetter(Unbaked::overrides),
					).apply(it, ::Unbaked)
				}
		}

		data class Override(
			val model: ItemModel.Unbaked,
			val predicate: FirmamentModelPredicate,
		)

		override fun resolve(resolver: ResolvableModel.Resolver) {
			fallback.resolve(resolver)
			overrides.forEach { it.model.resolve(resolver) }
		}

		override fun getCodec(): MapCodec<out Unbaked> {
			return CODEC
		}

		override fun bake(context: ItemModel.BakeContext): ItemModel {
			return Baked(
				fallback.bake(context),
				overrides.map { Baked.Override(it.model.bake(context), it.predicate) }
			)
		}
	}
}
