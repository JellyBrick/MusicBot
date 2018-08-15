/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.audio.AudioHandler
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.User

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class RemoveCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "remove"
        this.help = "removes a song from the queue"
        this.arguments = "<position|ALL>"
        this.aliases = arrayOf("delete")
        this.beListening = true
        this.bePlaying = true
    }

    public override fun doCommand(event: CommandEvent) {
        val handler = event.guild.audioManager.sendingHandler as AudioHandler
        if (handler.queue.isEmpty) {
            event.replyError("There is nothing in the queue!")
            return
        }
        if (event.args.equals("all", ignoreCase = true)) {
            val count = handler.queue.removeAll(event.author.idLong)
            if (count == 0)
                event.replyWarning("You don't have any songs in the queue!")
            else
                event.replySuccess("Successfully removed your $count entries.")
            return
        }
        var pos: Int = try {
            Integer.parseInt(event.args)
        } catch (e: NumberFormatException) {
            0
        }

        if (pos < 1 || pos > handler.queue.size()) {
            event.replyError("Position must be a valid integer between 1 and " + handler.queue.size() + "!")
            return
        }
        var isDJ = event.member.hasPermission(Permission.MANAGE_SERVER)
        if (!isDJ)
            isDJ = event.member.roles.contains(event.guild.getRoleById(bot.getSettings(event.guild).roleId))
        val qt = handler.queue[pos - 1]
        if (qt.identifier == event.author.idLong) {
            handler.queue.remove(pos - 1)
            event.replySuccess("Removed **" + qt.track.info.title + "** from the queue")
        } else if (isDJ) {
            handler.queue.remove(pos - 1)
            var u: User? = try {
                event.jda.getUserById(qt.identifier)
            } catch (e: Exception) {
                null
            }

            event.replySuccess("Removed **" + qt.track.info.title
                    + "** from the queue (requested by " + (if (u == null) "someone" else "**" + u.name + "**") + ")")
        } else {
            event.replyError("You cannot remove **" + qt.track.info.title + "** because you didn't add it!")
        }
    }

}
