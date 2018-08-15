/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class SettingsCmd(private val bot: Bot) : Command() {
    init {
        this.name = "settings"
        this.help = "shows the bots settings"
        this.aliases = arrayOf("status")
        this.guildOnly = true
    }

    override fun execute(event: CommandEvent) {
        val s = bot.getSettings(event.guild)
        val builder = MessageBuilder()
                .append("\uD83C\uDFA7 **")
                .append(event.selfUser.name)
                .append("** settings:")
        val tchan = event.guild.getTextChannelById(s.textId)
        val vchan = event.guild.getVoiceChannelById(s.voiceId)
        val role = event.guild.getRoleById(s.roleId)
        val ebuilder = EmbedBuilder()
                .setColor(event.selfMember.color)
                .setDescription("Text Channel: " + (if (tchan == null) "Any" else "**#" + tchan.name + "**")
                        + "\nVoice Channel: " + (if (vchan == null) "Any" else "**" + vchan.name + "**")
                        + "\nDJ Role: " + (if (role == null) "None" else "**" + role.name + "**")
                        + "\nRepeat Mode: **" + (if (s.repeatMode) "On" else "Off") + "**"
                        + "\nDefault Playlist: " + if (s.defaultPlaylist == null) "None" else "**" + s.defaultPlaylist + "**"
                )
                .setFooter(event.jda.guilds.size.toString() + " servers | "
                        + event.jda.guilds.stream().filter { g -> g.selfMember.voiceState.inVoiceChannel() }.count()
                        + " audio connections", null)
        event.channel.sendMessage(builder.setEmbed(ebuilder.build()).build()).queue()
    }

}
