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

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SkipCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "skip"
        this.help = "votes to skip the current song"
        this.aliases = arrayOf("voteskip")
        this.beListening = true
        this.bePlaying = true
    }

    public override fun doCommand(event: CommandEvent) {
        val handler = event.guild.audioManager.sendingHandler as AudioHandler
        if (event.author.idLong == handler.requester) {
            event.reply(event.client.success + " Skipped **" + handler.player.playingTrack.info.title + "**")
            handler.player.stopTrack()
        } else {
            val listeners = event.selfMember.voiceState.channel.members.stream()
                    .filter { m -> !m.user.isBot && !m.voiceState.isDeafened }.count().toInt()
            var msg: String
            if (handler.votes.contains(event.author.id))
                msg = event.client.warning + " You already voted to skip this song `["
            else {
                msg = event.client.success + " You voted to skip the song `["
                handler.votes.add(event.author.id)
            }
            val skippers = event.selfMember.voiceState.channel.members.stream()
                    .filter { m -> handler.votes.contains(m.user.id) }.count().toInt()
            val required = Math.ceil(listeners * .55).toInt()
            msg += skippers.toString() + " votes, " + required + "/" + listeners + " needed]`"
            if (skippers >= required) {
                val u = event.jda.getUserById(handler.requester)
                msg += ("\n" + event.client.success + " Skipped **" + handler.player.playingTrack.info.title
                        + "**" + if (handler.requester == 0L) "" else " (requested by " + (if (u == null) "someone" else "**" + u.name + "**") + ")")
                handler.player.stopTrack()
            }
            event.reply(msg)
        }
    }

}
