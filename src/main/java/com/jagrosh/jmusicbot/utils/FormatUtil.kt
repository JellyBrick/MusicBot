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
package com.jagrosh.jmusicbot.utils

import com.jagrosh.jmusicbot.audio.AudioHandler
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
object FormatUtil {

    fun formatTime(duration: Long): String {
        if (duration == java.lang.Long.MAX_VALUE)
            return "LIVE"
        var seconds = Math.round(duration / 1000.0)
        val hours = seconds / (60 * 60)
        seconds %= (60 * 60).toLong()
        val minutes = seconds / 60
        seconds %= 60
        return (if (hours > 0) hours.toString() + ":" else "") + (if (minutes < 10) "0$minutes" else minutes) + ":" + if (seconds < 10) "0$seconds" else seconds
    }

    fun nowPlayingMessage(guild: Guild, successEmoji: String): Message {
        val mb = MessageBuilder()
        mb.append(successEmoji).append(" **Now Playing...**")
        val eb = EmbedBuilder()
        val ah = guild.audioManager.sendingHandler as AudioHandler
        eb.setColor(guild.selfMember.color)
        if (!ah.isMusicPlaying) {
            eb.setTitle("No music playing")
            eb.setDescription("\u23F9 " + FormatUtil.progressBar(-1.0) + " " + FormatUtil.volumeIcon(ah.player.volume))
        } else {
            if (ah.requester != 0L) {
                val u = guild.jda.getUserById(ah.requester)
                if (u == null)
                    eb.setAuthor("Unknown (ID:" + ah.requester + ")", null, null)
                else
                    eb.setAuthor(u.name + "#" + u.discriminator, null, u.effectiveAvatarUrl)
            }

            try {
                eb.setTitle(ah.player.playingTrack.info.title, ah.player.playingTrack.info.uri)
            } catch (e: Exception) {
                eb.setTitle(ah.player.playingTrack.info.title)
            }

            if (!AudioHandler.USE_NP_REFRESH && ah.player.playingTrack is YoutubeAudioTrack)
                eb.setThumbnail("https://img.youtube.com/vi/" + ah.player.playingTrack.identifier + "/mqdefault.jpg")

            eb.setDescription(FormatUtil.embedFormat(ah))
        }
        return mb.setEmbed(eb.build()).build()
    }

    fun topicFormat(handler: AudioHandler?): String {
        return if (handler == null)
            "No music playing\n\u23F9 " + progressBar(-1.0) + " " + volumeIcon(100)
        else if (!handler.isMusicPlaying)
            "No music playing\n\u23F9 " + progressBar(-1.0) + " " + volumeIcon(handler.player.volume)
        else {
            val userid = handler.requester
            val track = handler.player.playingTrack
            var title: String? = track.info.title
            if (title == null || title == "Unknown Title")
                title = track.info.uri
            ("**" + title + "** [" + (if (userid == 0L) "autoplay" else "<@$userid>") + "]"
                    + "\n" + (if (handler.player.isPaused) "\u23F8" else "\u25B6") + " "
                    + "[" + formatTime(track.duration) + "] "
                    + volumeIcon(handler.player.volume))
        }
    }

    fun embedFormat(handler: AudioHandler?): String {
        return if (handler == null)
            "No music playing\n\u23F9 " + progressBar(-1.0) + " " + volumeIcon(100)
        else if (!handler.isMusicPlaying)
            "No music playing\n\u23F9 " + progressBar(-1.0) + " " + volumeIcon(handler.player.volume)
        else {
            val track = handler.player.playingTrack
            val progress = track.position.toDouble() / track.duration
            ((if (handler.player.isPaused) "\u23F8" else "\u25B6")
                    + " " + progressBar(progress)
                    + " `[" + formatTime(track.position) + "/" + formatTime(track.duration) + "]` "
                    + volumeIcon(handler.player.volume))
        }
    }

    private fun progressBar(percent: Double): String {
        val str = StringBuilder()
        for (i in 0..11)
            if (i == (percent * 12).toInt())
                str.append("\uD83D\uDD18")
            else
                str.append("▬")
        return str.toString()
    }

    fun volumeIcon(volume: Int): String {
        if (volume == 0)
            return "\uD83D\uDD07"
        if (volume < 30)
            return "\uD83D\uDD08"
        return if (volume < 70) "\uD83D\uDD09" else "\uD83D\uDD0A"
    }

    fun listOfTChannels(list: List<TextChannel>, query: String): String {
        val out = StringBuilder(" Multiple text channels found matching \"$query\":")
        var i = 0
        while (i < 6 && i < list.size) {
            out.append("\n - ").append(list[i].name).append(" (<#").append(list[i].id).append(">)")
            i++
        }
        if (list.size > 6)
            out.append("\n**And ").append(list.size - 6).append(" more...**")
        return out.toString()
    }

    fun listOfVChannels(list: List<VoiceChannel>, query: String): String {
        val out = StringBuilder(" Multiple voice channels found matching \"$query\":")
        var i = 0
        while (i < 6 && i < list.size) {
            out.append("\n - ").append(list[i].name).append(" (ID:").append(list[i].id).append(")")
            i++
        }
        if (list.size > 6)
            out.append("\n**And ").append(list.size - 6).append(" more...**")
        return out.toString()
    }

    fun listOfRoles(list: List<Role>, query: String): String {
        val out = StringBuilder(" Multiple text channels found matching \"$query\":")
        var i = 0
        while (i < 6 && i < list.size) {
            out.append("\n - ").append(list[i].name).append(" (ID:").append(list[i].id).append(")")
            i++
        }
        if (list.size > 6)
            out.append("\n**And ").append(list.size - 6).append(" more...**")
        return out.toString()
    }

    fun filter(input: String): String {
        return input.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim { it <= ' ' }
    }
}
