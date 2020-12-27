/*
 *   DataSync - AnvilPowered
 *   Copyright (C) 2020 Cableguy20
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.anvilpowered.datasync.sponge.command

import org.anvilpowered.datasync.api.registry.DataSyncKeys
import org.anvilpowered.datasync.common.command.CommonSyncCommandNode
import org.anvilpowered.datasync.common.command.CommonSyncReloadCommand
import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.Optional

class SpongeSyncReloadCommand : CommonSyncReloadCommand<Text, CommandSource>(), CommandCallable {

    companion object {
        val DESCRIPTION: Optional<Text> = Optional.of(Text.of(CommonSyncCommandNode.RELOAD_DESCRIPTION))
        val USAGE: Text = Text.of(CommonSyncCommandNode.RELOAD_USAGE)
    }

    override fun process(source: CommandSource, arguments: String): CommandResult {
        execute(source)
        return CommandResult.success()
    }

    override fun getSuggestions(source: CommandSource, arguments: String, targetPosition: Location<World>?): List<String> =
        listOf()

    override fun testPermission(source: CommandSource): Boolean =
        source.hasPermission(registry.getOrDefault(DataSyncKeys.RELOAD_COMMAND_PERMISSION))

    override fun getShortDescription(source: CommandSource): Optional<Text> = DESCRIPTION
    override fun getHelp(source: CommandSource): Optional<Text> = DESCRIPTION
    override fun getUsage(source: CommandSource): Text = USAGE
}
