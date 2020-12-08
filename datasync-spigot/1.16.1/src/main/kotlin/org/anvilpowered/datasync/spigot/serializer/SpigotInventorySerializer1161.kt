/*
 * DataSync - AnvilPowered
 *   Copyright (C) 2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package org.anvilpowered.datasync.spigot.serializer

import jetbrains.exodus.util.ByteArraySizedInputStream
import jetbrains.exodus.util.LightByteArrayOutputStream
import net.minecraft.server.v1_16_R1.NBTCompressedStreamTools
import net.minecraft.server.v1_16_R1.NBTTagCompound
import org.anvilpowered.datasync.api.model.snapshot.Snapshot
import org.anvilpowered.datasync.common.serializer.CommonInventorySerializer
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.DataInputStream
import java.io.DataOutput
import java.io.DataOutputStream
import java.util.Objects

class SpigotInventorySerializer1161 : CommonInventorySerializer<String, Player, Inventory, ItemStack>() {

    override fun serializeInventory(snapshot: Snapshot<*>, inventory: Inventory, maxSlots: Int): Boolean {
        var success = true
        try {
            val outputStream = LightByteArrayOutputStream()
            val dataOutput = DataOutputStream(outputStream)
            val contents: Array<out ItemStack?> = inventory.contents
            val root = NBTTagCompound()
            val slot = NBTTagCompound()
            root["slot"] = slot
            for (i in contents.indices) {
                if (contents[i] == null || contents[i]!!.type.isAir) {
                    continue
                }
                val itemNBT = NBTTagCompound()
                CraftItemStack.asNMSCopy(contents[i]).save(itemNBT)
                slot[i.toString()] = itemNBT
            }
            NBTCompressedStreamTools.a(root, dataOutput as DataOutput)
            snapshot.setInventory(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }
        return success
    }

    override fun serializeInventory(snapshot: Snapshot<*>, inventory: Inventory): Boolean {
        return serializeInventory(snapshot, inventory, INVENTORY_SLOTS)
    }

    override fun deserializeInventory(snapshot: Snapshot<*>, inventory: Inventory, fallbackItemStack: ItemStack): Boolean {
        inventory.clear()
        val root: NBTTagCompound
        try {
            root = NBTCompressedStreamTools.a(DataInputStream(ByteArraySizedInputStream(snapshot.inventory)))
            val slot = root["slot"] as NBTTagCompound
            for (i in 0 until INVENTORY_SLOTS) {
                if (slot[i.toString()] == null) {
                    continue
                }
                inventory.setItem(
                    i,
                    CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R1.ItemStack.a(slot[i.toString()] as NBTTagCompound))
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun deserializeInventory(snapshot: Snapshot<*>, itemStacks: Inventory): Boolean {
        return deserializeInventory(snapshot, itemStacks, defaultFallbackItemStackSnapshot)
    }

    override fun getDefaultFallbackItemStackSnapshot(): ItemStack {
        val itemStack = ItemStack(Material.BARRIER, 1)
        Objects.requireNonNull(itemStack.itemMeta)!!.setDisplayName("Not an actual slot")
        return itemStack
    }

    override fun getExitWithoutSavingItemStackSnapshot(): ItemStack {
        val itemStack = ItemStack(Material.GOLD_INGOT, 1)
        Objects.requireNonNull(itemStack.itemMeta)!!.setDisplayName("Exit without saving")
        return itemStack
    }

    override fun serialize(snapshot: Snapshot<*>, player: Player): Boolean {
        return serializeInventory(snapshot, player.inventory)
    }

    override fun deserialize(snapshot: Snapshot<*>, player: Player): Boolean {
        return deserializeInventory(snapshot, player.inventory)
    }
}