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

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.playlist.Playlist

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class PlaylistCmd(private val bot: Bot) : Command() {
    init {
        this.category = bot.OWNER
        this.ownerCommand = true
        this.guildOnly = false
        this.name = "playlist"
        this.arguments = "<append|delete|make|setdefault>"
        this.help = "playlist management"
        this.children = arrayOf(ListCmd(), AppendlistCmd(), DeletelistCmd(), MakelistCmd(), DefaultlistCmd())
    }

    public override fun execute(event: CommandEvent) {
        val builder = StringBuilder(event.client.warning + " Playlist Management Commands:\n")
        for (cmd in this.children)
            builder.append("\n`").append(event.client.prefix).append(name).append(" ").append(cmd.name)
                    .append(" ").append(if (cmd.arguments == null) "" else cmd.arguments).append("` - ").append(cmd.help)
        event.reply(builder.toString())
    }

    internal inner class MakelistCmd : Command() {
        init {
            this.name = "make"
            this.aliases = arrayOf("create")
            this.help = "makes a new playlist"
            this.arguments = "<name>"
            this.category = bot.OWNER
            this.ownerCommand = true
            this.guildOnly = false
        }

        override fun execute(event: CommandEvent) {
            val pname = event.args.replace("\\s+".toRegex(), "_")
            if (Playlist.loadPlaylist(pname) == null) {
                try {
                    Files.createFile(Paths.get("Playlists" + File.separator + pname + ".txt"))
                    event.reply(event.client.success + " Successfully created playlist `" + pname + "`!")
                } catch (e: IOException) {
                    event.reply(event.client.error + " I was unable to create the playlist: " + e.localizedMessage)
                }

            } else
                event.reply(event.client.error + " Playlist `" + pname + "` already exists!")
        }
    }

    internal inner class DeletelistCmd : Command() {
        init {
            this.name = "delete"
            this.aliases = arrayOf("remove")
            this.help = "deletes an existing playlist"
            this.arguments = "<name>"
            this.guildOnly = false
            this.ownerCommand = true
            this.category = bot.OWNER
        }

        override fun execute(event: CommandEvent) {
            val pname = event.args.replace("\\s+".toRegex(), "_")
            if (Playlist.loadPlaylist(pname) == null)
                event.reply(event.client.error + " Playlist `" + pname + "` doesn't exist!")
            else {
                try {
                    Files.delete(Paths.get("Playlists" + File.separator + pname + ".txt"))
                    event.reply(event.client.success + " Successfully deleted playlist `" + pname + "`!")
                } catch (e: IOException) {
                    event.reply(event.client.error + " I was unable to delete the playlist: " + e.localizedMessage)
                }

            }
        }
    }

    internal inner class AppendlistCmd : Command() {
        init {
            this.name = "append"
            this.aliases = arrayOf("add")
            this.help = "appends songs to an existing playlist"
            this.arguments = "<name> <URL> | <URL> | ..."
            this.guildOnly = false
            this.ownerCommand = true
            this.category = bot.OWNER
        }

        override fun execute(event: CommandEvent) {
            val parts = event.args.split("\\s+".toRegex(), 2).toTypedArray()
            if (parts.size < 2) {
                event.reply(event.client.error + " Please include a playlist name and URLs to add!")
                return
            }
            val pname = parts[0]
            val playlist = Playlist.loadPlaylist(pname)
            if (playlist == null)
                event.reply(event.client.error + " Playlist `" + pname + "` doesn't exist!")
            else {
                val builder = StringBuilder()
                playlist.items.forEach { item -> builder.append("\r\n").append(item) }
                val urls = parts[1].split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (url in urls) {
                    var u = url.trim { it <= ' ' }
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length - 1)
                    builder.append("\r\n").append(u)
                }
                try {
                    Files.write(Paths.get("Playlists" + File.separator + pname + ".txt"), builder.toString().trim { it <= ' ' }.toByteArray())
                    event.reply(event.client.success + " Successfully added " + urls.size + " songs to playlist `" + pname + "`!")
                } catch (e: IOException) {
                    event.reply(event.client.error + " I was unable to append to the playlist: " + e.localizedMessage)
                }

            }
        }
    }

    internal inner class DefaultlistCmd : Command() {
        init {
            this.name = "setdefault"
            this.aliases = arrayOf("default")
            this.help = "sets the default playlist for the server"
            this.arguments = "<playlistname|NONE>"
            this.guildOnly = true
            this.ownerCommand = true
            this.category = bot.OWNER
        }

        override fun execute(event: CommandEvent) {
            if (event.args.isEmpty()) {
                event.reply(event.client.error + " Please include a playlist name or NONE")
            }
            if (event.args.equals("none", ignoreCase = true)) {
                bot.setDefaultPlaylist(event.guild, null)
                event.reply(event.client.success + " Cleared the default playlist for **" + event.guild.name + "**")
                return
            }
            val pname = event.args.replace("\\s+".toRegex(), "_")
            if (Playlist.loadPlaylist(pname) == null) {
                event.reply(event.client.error + " Could not find `" + pname + ".txt`!")
            } else {
                bot.setDefaultPlaylist(event.guild, pname)
                event.reply(event.client.success + " The default playlist for **" + event.guild.name + "** is now `" + pname + "`")
            }
        }
    }

    internal inner class ListCmd : Command() {
        init {
            this.name = "all"
            this.aliases = arrayOf("available", "list")
            this.help = "lists all available playlists"
            this.guildOnly = true
            this.ownerCommand = true
            this.category = bot.OWNER
        }

        override fun execute(event: CommandEvent) {
            if (!Playlist.folderExists())
                Playlist.createFolder()
            if (!Playlist.folderExists()) {
                event.reply(event.client.warning + " Playlists folder does not exist and could not be created!")
                return
            }
            val list = Playlist.playlists
            if (list == null)
                event.reply(event.client.error + " Failed to load available playlists!")
            else if (list.isEmpty())
                event.reply(event.client.warning + " There are no playlists in the Playlists folder!")
            else {
                val builder = StringBuilder(event.client.success + " Available playlists:\n")
                list.forEach { str -> builder.append("`").append(str).append("` ") }
                event.reply(builder.toString())
            }
        }
    }
}
