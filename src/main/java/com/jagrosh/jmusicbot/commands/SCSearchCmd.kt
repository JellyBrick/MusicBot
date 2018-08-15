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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.concurrent.TimeUnit
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.menu.OrderedMenu
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.audio.AudioHandler
import com.jagrosh.jmusicbot.utils.FormatUtil
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SCSearchCmd(bot: Bot, private val searchingEmoji: String) : MusicCommand(bot) {

    private val builder: OrderedMenu.Builder

    init {
        this.name = "scsearch"
        this.arguments = "<query>"
        this.help = "searches Soundcloud for a provided query"
        this.beListening = true
        this.bePlaying = false
        this.botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS)
        builder = OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.waiter)
                .setTimeout(1, TimeUnit.MINUTES)
    }

    public override fun doCommand(event: CommandEvent) {
        if (event.args.isEmpty()) {
            event.reply(event.client.error + " Please include a query.")
            return
        }
        event.reply(searchingEmoji + " Searching... `[" + event.args + "]`") { m -> bot.audioManager.loadItemOrdered(event.guild, "scsearch:" + event.args, ResultHandler(m, event)) }
    }

    private inner class ResultHandler (internal val m: Message, internal val event: CommandEvent) : AudioLoadResultHandler {

        override fun trackLoaded(track: AudioTrack) {
            if (AudioHandler.isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.client.warning + " This track (**" + track.info.title + "**) is longer than the allowed maximum: `"
                        + FormatUtil.formatTime(track.duration) + "` > `" + FormatUtil.formatTime(AudioHandler.MAX_SECONDS * 1000) + "`")).queue()
                return
            }
            val pos = bot.queueTrack(event, track) + 1
            m.editMessage(FormatUtil.filter(event.client.success + " Added **" + track.info.title
                    + "** (`" + FormatUtil.formatTime(track.duration) + "`) " + if (pos == 0)
                "to begin playing"
            else
                " to the queue at position $pos")).queue()
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            builder.setColor(event.selfMember.color)
                    .setText(FormatUtil.filter(event.client.success + " Search results for `" + event.args + "`:"))
                    .setChoices()
                    .setSelection { _, i ->
                        val track = playlist.tracks[i!! - 1]
                        if (AudioHandler.isTooLong(track)) {
                            event.replyWarning("This track (**" + track.info.title + "**) is longer than the allowed maximum: `"
                                    + FormatUtil.formatTime(track.duration) + "` > `" + FormatUtil.formatTime(AudioHandler.MAX_SECONDS * 1000) + "`")
                            return@setSelection
                        }
                        val pos = bot.queueTrack(event, track) + 1
                        event.replySuccess("Added **" + track.info.title
                                + "** (`" + FormatUtil.formatTime(track.duration) + "`) " + if (pos == 0)
                            "to begin playing"
                        else
                            " to the queue at position $pos")
                    }
                    .setCancel {}
                    .setUsers(event.author)
            var i = 0
            while (i < 4 && i < playlist.tracks.size) {
                val track = playlist.tracks[i]
                builder.addChoices("`[" + FormatUtil.formatTime(track.duration) + "]` [**"
                        + track.info.title + "**](" + track.info.uri + ")")
                i++
            }
            builder.build().display(m)
        }

        override fun noMatches() {
            m.editMessage(FormatUtil.filter(event.client.warning + " No results found for `" + event.args + "`.")).queue()
        }

        override fun loadFailed(throwable: FriendlyException) {
            if (throwable.severity == Severity.COMMON)
                m.editMessage(event.client.error + " Error loading: " + throwable.message).queue()
            else
                m.editMessage(event.client.error + " Error loading track.").queue()
        }
    }
}
