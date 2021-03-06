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
class ForceskipCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "forceskip"
        this.help = "skips the current song"
        this.aliases = arrayOf("modskip")
        this.bePlaying = true
        this.category = bot.DJ
    }

    public override fun doCommand(event: CommandEvent) {
        val handler = event.guild.audioManager.sendingHandler as AudioHandler
        val u = event.jda.getUserById(handler.requester)
        event.reply(event.client.success + " Skipped **" + handler.player.playingTrack.info.title
                + "** (requested by " + (if (u == null) "someone" else "**" + u.name + "**") + ")")
        handler.player.stopTrack()
    }

}
