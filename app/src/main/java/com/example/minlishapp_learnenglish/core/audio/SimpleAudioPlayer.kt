package com.example.minlishapp_learnenglish.core.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

class SimpleAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun play(url: String, onError: () -> Unit) {
        release()
        val player = MediaPlayer()
        mediaPlayer = player

        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setDataSource(url)
            player.setOnPreparedListener { it.start() }
            player.setOnCompletionListener { release() }
            player.setOnErrorListener { _, _, _ ->
                release()
                onError()
                true
            }
            player.prepareAsync()
        } catch (_: Exception) {
            release()
            onError()
        }
    }

    fun release() {
        val player = mediaPlayer
        mediaPlayer = null
        if (player != null) {
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching { player.release() }
        }
    }
}
