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

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.Settings
import com.jagrosh.jmusicbot.audio.AudioHandler
import net.dv8tion.jda.core.entities.GuildVoiceState
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.exceptions.PermissionException

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
abstract class MusicCommand(val bot: Bot) : Command() {
    var bePlaying: Boolean = false
    var beListening: Boolean = false

    init {
        this.guildOnly = true
        this.category = bot.MUSIC
    }

    override fun execute(event: CommandEvent) {
        val settings = bot.getSettings(event.guild)
        val tchannel = event.guild.getTextChannelById(settings.textId)
        if (tchannel != null && event.textChannel != tchannel) {
            try {
                event.message.delete().queue()
            } catch (ignored: PermissionException) {
            }

            event.replyInDm(event.client.error + " You can only use that command in <#" + settings.textId + ">!")
            return
        }
        if (bePlaying && (event.guild.audioManager.sendingHandler == null || !(event.guild.audioManager.sendingHandler as AudioHandler).isMusicPlaying)) {
            event.reply(event.client.error + " There must be music playing to use that!")
            return
        }
        if (beListening) {
            var current: VoiceChannel? = event.guild.selfMember.voiceState.channel
            if (current == null)
                current = event.guild.getVoiceChannelById(settings.voiceId)
            val userState = event.member.voiceState
            if (!userState.inVoiceChannel() || userState.isDeafened || current != null && userState.channel != current) {
                event.reply(event.client.error
                        + " You must be listening in " + (if (current == null) "a voice channel" else "**" + current.name + "**")
                        + " to use that!")
                return
            }
            if (!event.guild.selfMember.voiceState.inVoiceChannel())
                try {
                    event.guild.audioManager.openAudioConnection(userState.channel)
                } catch (ex: PermissionException) {
                    event.reply(event.client.error + " I am unable to connect to **" + userState.channel.name + "**!")
                    return
                }

        }
        doCommand(event)
    }

    protected abstract fun doCommand(event: CommandEvent)
}
