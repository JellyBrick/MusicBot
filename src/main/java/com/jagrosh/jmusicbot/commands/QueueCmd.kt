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
import com.jagrosh.jdautilities.menu.Paginator
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.audio.AudioHandler
import com.jagrosh.jmusicbot.utils.FormatUtil
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.exceptions.PermissionException
import java.util.concurrent.TimeUnit

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class QueueCmd(bot: Bot) : MusicCommand(bot) {

    private val builder: Paginator.Builder

    init {
        this.name = "queue"
        this.help = "shows the current queue"
        this.arguments = "[pagenum]"
        this.aliases = arrayOf("list")
        this.bePlaying = true
        this.botPermissions = arrayOf(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)
        builder = Paginator.Builder()
                .setColumns(1)
                .setFinalAction { m ->
                    try {
                        m.clearReactions().queue()
                    } catch (ignored: PermissionException) {
                    }
                }
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .setEventWaiter(bot.waiter)
                .setTimeout(1, TimeUnit.MINUTES)
    }

    public override fun doCommand(event: CommandEvent) {
        var pagenum = 1
        try {
            pagenum = Integer.parseInt(event.args)
        } catch (ignored: NumberFormatException) {
        }

        val ah = event.guild.audioManager.sendingHandler as AudioHandler
        val list = ah.queue.list
        if (list.isEmpty()) {
            event.replyWarning("There is no music in the queue!" + if (!ah.isMusicPlaying) "" else " Now playing:\n\n**" + ah.player.playingTrack.info.title + "**\n" + FormatUtil.embedFormat(ah))
            return
        }
        val songs = arrayOfNulls<String>(list.size)
        var total: Long = 0
        for (i in list.indices) {
            total += list[i].track.duration
            songs[i] = list[i].toString()
        }
        val fintotal = total
        builder.setText { _, _ -> event.client.success + " " + getQueueTitle(ah, event.client.success, songs.size, fintotal, bot.getSettings(event.guild).repeatMode) }
                .setItems(*songs)
                .setUsers(event.author)
                .setColor(event.selfMember.color)
        builder.build().paginate(event.channel, pagenum)
    }

    private fun getQueueTitle(ah: AudioHandler, success: String, songslength: Int, total: Long, repeatmode: Boolean): String {
        val sb = StringBuilder()
        if (ah.player.playingTrack != null)
            sb.append("**").append(ah.player.playingTrack.info.title).append("**\n").append(FormatUtil.embedFormat(ah)).append("\n\n")
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtil.formatTime(total)).append("` ")
                .append(if (repeatmode) "| \uD83D\uDD01" else "").toString())
    }
}
