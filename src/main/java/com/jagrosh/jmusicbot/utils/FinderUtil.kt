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

import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import java.util.*

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
object FinderUtil {

    private const val DISCORD_ID = "\\d{17,20}"

    fun findTextChannel(query: String, guild: Guild): List<TextChannel> {
        val id: String
        if (query.matches("<#\\d+>".toRegex())) {
            id = query.replace("<#(\\d+)>".toRegex(), "$1")
            val tc = guild.jda.getTextChannelById(id)
            if (tc != null && tc.guild == guild)
                return listOf<TextChannel>(tc)
        } else if (query.matches(DISCORD_ID.toRegex())) {
            id = query
            val tc = guild.jda.getTextChannelById(id)
            if (tc != null && tc.guild == guild)
                return listOf<TextChannel>(tc)
        }
        val exact = ArrayList<TextChannel>()
        val wrongcase = ArrayList<TextChannel>()
        val startswith = ArrayList<TextChannel>()
        val contains = ArrayList<TextChannel>()
        val lowerquery = query.toLowerCase()
        guild.textChannels.forEach { tc ->
            if (tc.name == lowerquery)
                exact.add(tc)
            else if (tc.name.equals(lowerquery, ignoreCase = true) && exact.isEmpty())
                wrongcase.add(tc)
            else if (tc.name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(tc)
            else if (tc.name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(tc)
        }
        if (!exact.isEmpty())
            return exact
        if (!wrongcase.isEmpty())
            return wrongcase
        return if (!startswith.isEmpty()) startswith else contains
    }

    fun findVoiceChannel(query: String, guild: Guild): List<VoiceChannel> {
        val id: String
        if (query.matches("<#\\d+>".toRegex())) {
            id = query.replace("<#(\\d+)>".toRegex(), "$1")
            val vc = guild.jda.getVoiceChannelById(id)
            if (vc != null && vc.guild == guild)
                return listOf<VoiceChannel>(vc)
        } else if (query.matches(DISCORD_ID.toRegex())) {
            id = query
            val vc = guild.jda.getVoiceChannelById(id)
            if (vc != null && vc.guild == guild)
                return listOf<VoiceChannel>(vc)
        }
        val exact = ArrayList<VoiceChannel>()
        val wrongcase = ArrayList<VoiceChannel>()
        val startswith = ArrayList<VoiceChannel>()
        val contains = ArrayList<VoiceChannel>()
        val lowerquery = query.toLowerCase()
        guild.voiceChannels.forEach { vc ->
            if (vc.name == lowerquery)
                exact.add(vc)
            else if (vc.name.equals(lowerquery, ignoreCase = true) && exact.isEmpty())
                wrongcase.add(vc)
            else if (vc.name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(vc)
            else if (vc.name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(vc)
        }
        if (!exact.isEmpty())
            return exact
        if (!wrongcase.isEmpty())
            return wrongcase
        return if (!startswith.isEmpty()) startswith else contains
    }

    fun findRole(query: String, guild: Guild): List<Role> {
        val id: String
        if (query.matches("<@&\\d+>".toRegex())) {
            id = query.replace("<@&(\\d+)>".toRegex(), "$1")
            val r = guild.getRoleById(id)
            if (r != null)
                return listOf<Role>(r)
        } else if (query.matches(DISCORD_ID.toRegex())) {
            id = query
            val r = guild.getRoleById(id)
            if (r != null)
                return listOf<Role>(r)
        }
        val exact = ArrayList<Role>()
        val wrongcase = ArrayList<Role>()
        val startswith = ArrayList<Role>()
        val contains = ArrayList<Role>()
        val lowerQuery = query.toLowerCase()
        guild.roles.forEach { role ->
            if (role.name == query)
                exact.add(role)
            else if (role.name.equals(query, ignoreCase = true) && exact.isEmpty())
                wrongcase.add(role)
            else if (role.name.toLowerCase().startsWith(lowerQuery) && wrongcase.isEmpty())
                startswith.add(role)
            else if (role.name.toLowerCase().contains(lowerQuery) && startswith.isEmpty())
                contains.add(role)
        }
        if (!exact.isEmpty())
            return exact
        if (!wrongcase.isEmpty())
            return wrongcase
        return if (!startswith.isEmpty()) startswith else contains
    }
}
