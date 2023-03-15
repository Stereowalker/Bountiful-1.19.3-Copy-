package ejektaflex.bountiful.data.bounty

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ejektaflex.bountiful.data.bounty.enums.BountyType
import ejektaflex.bountiful.ext.hackyRandom
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.tags.ItemTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BountyEntryItemTag : AbstractBountyEntryStackLike(), IBountyObjective, IBountyReward {

    @Expose
    @SerializedName("type")
    override var bType: String = BountyType.ItemTag.id

    override val formattedName: MutableComponent
        get() = Component.literal(name ?: content)

    override fun validate() {
        if (bType == BountyType.ItemTag.id) {
            if (tagElements.isEmpty()) {
                throw EntryValidationException("Tag '$content' does not exist or was empty!")
            }
        }
        super.validate()
    }

    val tagElements: List<Item>
        get() = ItemTags.getCollection().get(ResourceLocation(content))?.allElements?.toList() ?: listOf()

    override val validStacks: List<ItemStack>
        get() {
            return tagElements.map { element ->
                ItemStack(element).apply {
                    count = amount
                    nbtTag?.let { tag = it }
                }
            }
        }

    override fun reward(player: Player) {
        var amountNeeded = amount
        val stacksToGive = mutableListOf<ItemStack>()

        while (amountNeeded > 0) {
            val randItem = validStacks.hackyRandom().copy()
            val stackSize = min(amountNeeded, randItem.maxStackSize)
            randItem.count = stackSize
            stacksToGive.add(randItem)
            amountNeeded -= stackSize
        }

        stacksToGive.forEach { stack ->
            ItemHandlerHelper.giveItemToPlayer(player, stack)
        }
    }

}
