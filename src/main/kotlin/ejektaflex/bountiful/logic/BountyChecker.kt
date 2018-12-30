package ejektaflex.bountiful.logic

import ejektaflex.bountiful.ContentRegistry
import ejektaflex.bountiful.api.ext.sendMessage
import ejektaflex.bountiful.api.logic.pickable.PickedEntryEntity
import ejektaflex.bountiful.api.logic.pickable.PickedEntryStack
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

object BountyChecker {

    fun hasItems(player: EntityPlayer, inv: NonNullList<ItemStack>, data: BountyData): List<ItemStack>? {
        val stackPicked = data.toGet.items.mapNotNull { it as? PickedEntryStack }

        println(stackPicked)

        val prereqItems = inv.filter { invItem ->
            stackPicked.any { picked ->
                picked.itemStack?.isItemEqualIgnoreDurability(invItem) == true
            }
        }

        println("Prereq items: $prereqItems")

        // Check to see if bounty meets all prerequisites
        val hasAllItems = stackPicked.all { picked ->
            val stacksMatching = prereqItems.filter { it.isItemEqualIgnoreDurability(picked.itemStack!!) }
            val hasEnough = stacksMatching.sumBy { it.count } >= picked.amount
            if (!hasEnough) {
                player.sendMessage(TextComponentString("§cCannot fulfill bounty, you don't have everything needed!"))
            }
            hasEnough
        }

        return if (hasAllItems) {
            prereqItems
        } else {
            null
        }
    }

    fun takeItems(player: EntityPlayer, inv: NonNullList<ItemStack>, data: BountyData, matched: List<ItemStack>) {
        // If it does, reduce count of all relevant stacks
        data.toGet.items.mapNotNull { it as? PickedEntryStack }.forEach { picked ->
            val stacksToChange = matched.filter { it.isItemEqualIgnoreDurability(picked.itemStack!!) }
            for (stack in stacksToChange) {
                val amountToRemove = min(stack.count, picked.amount)
                stack.count -= amountToRemove
            }
        }
    }

    /**
     * Tries to tick all relevant entities on Bounty. Returns true if there are none left
     */
    fun tryTakeEntities(player: EntityPlayer, data: BountyData, bounty: ItemStack, entity: EntityLivingBase) {
        val toRemove = mutableListOf<PickedEntryEntity>()
        val bountyEntities = data.toGet.items.mapNotNull { it as? PickedEntryEntity }

        bountyEntities.forEach { picked ->
            //println("${picked.entityEntry?.name?.toLowerCase()}, ${entity.name.toLowerCase()}")
            if (picked.entityEntry?.name?.toLowerCase() == entity.name.toLowerCase()) {
                if (picked.amount > 0) {
                    picked.amount--
                }
                if (picked.amount == 0) {
                    toRemove.add(picked)
                }
            }
        }
        data.toGet.items.removeAll(toRemove)
        bounty.tagCompound = data.serializeNBT()
    }

    fun hasEntitiesFulfilled(data: BountyData): Boolean {
        val bountyEntities = data.toGet.items.mapNotNull { it as? PickedEntryEntity }
        return bountyEntities.isEmpty()
    }

    fun rewardItems(player: EntityPlayer, inv: NonNullList<ItemStack>, data: BountyData, bountyItem: ItemStack) {

        // Reward player with rewards
        data.rewards.items.forEach { reward ->
            var amountNeededToGive = reward.amount
            val stacksToGive = mutableListOf<ItemStack>()
            while (amountNeededToGive > 0) {
                val stackSize = min(amountNeededToGive, bountyItem.maxStackSize)
                val newStack = reward.itemStack!!.copy().apply { count = stackSize }
                stacksToGive.add(newStack)
                amountNeededToGive -= stackSize
            }
            stacksToGive.forEach { stack ->
                ItemHandlerHelper.giveItemToPlayer(player, stack)
            }
        }
    }


}