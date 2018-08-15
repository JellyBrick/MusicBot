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
class SkiptoCmd(bot: Bot) : MusicCommand(bot) {

    init {
        this.name = "skipto"
        this.help = "skips to the specified song"
        this.arguments = "<position>"
        this.aliases = arrayOf("jumpto")
        this.bePlaying = true
        this.category = bot.DJ
    }

    public override fun doCommand(event: CommandEvent) {
        val index: Int
        try {
            index = Integer.parseInt(event.args)
        } catch (e: NumberFormatException) {
            event.reply(event.client.error + " `" + event.args + "` is not a valid integer!")
            return
        }

        val handler = event.guild.audioManager.sendingHandler as AudioHandler
        if (index < 1 || index > handler.queue.size()) {
            event.reply(event.client.error + " Position must be a valid integer between 1 and " + handler.queue.size() + "!")
            return
        }
        handler.queue.skip(index - 1)
        event.reply(event.client.success + " Skipped to **" + handler.queue[0].track.info.title + "**")
        handler.player.stopTrack()
    }

}
