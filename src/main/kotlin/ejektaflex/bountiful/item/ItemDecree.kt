package ejektaflex.bountiful.item

import ejektaflex.bountiful.api.ext.hackyRandom
import ejektaflex.bountiful.content.ModContent
import ejektaflex.bountiful.data.Decree
import ejektaflex.bountiful.registry.DecreeRegistry
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraft.util.text.TranslationTextComponent


class ItemDecree() : Item(
        Item.Properties().maxStackSize(1).group(ModContent.BountifulGroup)
) {


    /**
     * Thrown when bounty NBT data could not be created
     */
    class DecreeCreationException(err: String = "Decree could not be created!") : Exception(err)

    override fun getTranslationKey() = "bountiful.decree"

    override fun getDisplayName(stack: ItemStack): ITextComponent {
        return StringTextComponent("§5").appendSibling(
            TranslationTextComponent(getTranslationKey())
        )
    }

    @OnlyIn(Dist.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {

        val id = stack.tag?.getString("id")

        if (id != null) {
            val decree = DecreeRegistry.getDecree(id)
            val tip = if (decree != null) {
                StringTextComponent("§5").appendSibling(
                        StringTextComponent("§6")
                ).appendSibling(
                        StringTextComponent(decree.decreeTitle)
                )
            } else {
                TranslationTextComponent("bountiful.decree.invalid").appendSibling(
                        StringTextComponent(" ($id)")
                )
            }
            tooltip.add(tip)
        } else {
            tooltip.add(TranslationTextComponent(getTranslationKey()))
        }

        // TODO Add debug tool when holding sneak, showing which pools are being used
        //tooltip.add(StringTextComponent("Replace Me!"))
    }

    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {

        val held = playerIn.getHeldItem(handIn)
        ensureDecree(held)

        return super.onItemRightClick(worldIn, playerIn, handIn)
    }

    private fun ensureDecree(stack: ItemStack) {
        val data = try {
            DecreeRegistry.content.hackyRandom()
        } catch (e: DecreeCreationException) {
            return
        }
        if (stack.item is ItemDecree) {
            if (!stack.hasTag()) {
                stack.tag = data.serializeNBT()
            }
        } else {
            throw Exception("${stack.displayName} is not an ItemDecree, so you cannot generate decree data for it!")
        }
    }

}