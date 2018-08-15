/*
 * Copyright 2018 John Grosh (jagrosh).
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
package com.jagrosh.jmusicbot.playlist

import com.jagrosh.jmusicbot.audio.AudioHandler
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
class Playlist private constructor(val name: String, val items: List<String>, private val shuffle: Boolean) {
    var tracks: MutableList<AudioTrack>? = null
    var errors: MutableList<PlaylistLoadError>? = null

    fun loadTracks(manager: AudioPlayerManager, consumer: (Any) -> Unit, callback: () -> Unit) {
        if (tracks == null) {
            tracks = LinkedList()
            errors = LinkedList()
            for (i in items.indices) {
                val last = i + 1 == items.size
                manager.loadItemOrdered(name, items[i], object : AudioLoadResultHandler {
                    override fun trackLoaded(at: AudioTrack) {
                        if (AudioHandler.isTooLong(at))
                            errors!!.add(PlaylistLoadError(i, items[i], "This track is longer than the allowed maximum"))
                        else {
                            tracks!!.add(at)
                            consumer.accept(at)
                        }
                        if (last) {
                            callback.run(consumer)
                        }
                    }

                    override fun playlistLoaded(ap: AudioPlaylist) {
                        if (ap.isSearchResult) {
                            if (AudioHandler.isTooLong(ap.tracks[0]))
                                errors!!.add(PlaylistLoadError(i, items[i], "This track is longer than the allowed maximum"))
                            else {
                                tracks!!.add(ap.tracks[0])
                                consumer.accept(ap.tracks[0])
                            }
                        } else if (ap.selectedTrack != null) {
                            if (AudioHandler.isTooLong(ap.selectedTrack))
                                errors!!.add(PlaylistLoadError(i, items[i], "This track is longer than the allowed maximum"))
                            else {
                                tracks!!.add(ap.selectedTrack)
                                consumer.accept(ap.selectedTrack)
                            }
                        } else {
                            val loaded = ArrayList(ap.tracks)
                            if (shuffle)
                                for (first in loaded.indices) {
                                    val second = (Math.random() * loaded.size).toInt()
                                    val tmp = loaded[first]
                                    loaded[first] = loaded[second]
                                    loaded[second] = tmp
                                }
                            loaded.removeIf { AudioHandler.isTooLong(it) }
                            tracks!!.addAll(loaded)
                            loaded.forEach(consumer)
                        }
                        if (last) {
                            callback.run(consumer)
                        }
                    }

                    override fun noMatches() {
                        errors!!.add(PlaylistLoadError(i, items[i], "No matches found."))
                        if (last) {
                            callback.run(consumer)
                        }
                    }

                    override fun loadFailed(fe: FriendlyException) {
                        errors!!.add(PlaylistLoadError(i, items[i], "Failed to load track: " + fe.localizedMessage))
                        if (last) {
                            callback.run(consumer)
                        }
                    }
                })
            }
        }
    }

    fun shuffleTracks() {
        if (tracks != null) {
            for (first in tracks!!.indices) {
                val second = (Math.random() * tracks!!.size).toInt()
                val tmp = tracks!![first]
                tracks!![first] = tracks!![second]
                tracks!![second] = tmp
            }
        }
    }

    inner class PlaylistLoadError (val index: Int, val item: String, val reason: String)

    companion object {

        fun createFolder() {
            try {
                Files.createDirectory(Paths.get("Playlists"))
            } catch (ignored: IOException) {
            }

        }

        fun folderExists(): Boolean {
            return Files.exists(Paths.get("Playlists"))
        }

        val playlists: List<String>?
            get() {
                return if (folderExists()) {
                    val folder = File("Playlists")
                    Arrays.stream(Objects.requireNonNull(folder.listFiles { pathname -> pathname.name.endsWith(".txt") })).map<String> { f -> f.name.substring(0, f.name.length - 4) }.collect(Collectors.toList())
                } else {
                    createFolder()
                    null
                }
            }

        fun loadPlaylist(name: String): Playlist? {
            try {
                if (folderExists()) {
                    val shuffle = booleanArrayOf(false)
                    val list = ArrayList<String>()
                    Files.readAllLines(Paths.get("Playlists" + File.separator + name + ".txt")).forEach Files@{ str ->
                        var s = str.trim { it <= ' ' }
                        if (s.isEmpty())
                            return@Files
                        if (s.startsWith("#") || s.startsWith("//")) {
                            s = s.replace("\\s+".toRegex(), "")
                            if (s.equals("#shuffle", ignoreCase = true) || s.equals("//shuffle", ignoreCase = true))
                                shuffle[0] = true
                        } else
                            list.add(s)
                    }
                    if (shuffle[0]) {
                        for (first in list.indices) {
                            val second = (Math.random() * list.size).toInt()
                            val tmp = list[first]
                            list[first] = list[second]
                            list[second] = tmp
                        }
                    }
                    return Playlist(name, list, shuffle[0])
                } else {
                    createFolder()
                    return null
                }
            } catch (e: IOException) {
                return null
            }

        }
    }
}

private fun <P1, R> ((P1) -> R).accept(at: R) {

}
