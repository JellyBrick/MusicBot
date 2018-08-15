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
package com.jagrosh.jmusicbot.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.jagrosh.jmusicbot.queue.Queueable
import com.jagrosh.jmusicbot.utils.FormatUtil

/**
 *
 * @author John Grosh <john.a.grosh></john.a.grosh>@gmail.com>
 */
class QueuedTrack(val track: AudioTrack, override val identifier: Long) : Queueable {

    override fun toString(): String {
        return "`[" + FormatUtil.formatTime(track.duration) + "]` **" + track.info.title + "** - <@" + identifier + ">"
    }

}
