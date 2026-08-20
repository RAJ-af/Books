package com.example.ui.audio

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.audio.LibrivoxAudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlayerState(
    val isVisible: Boolean = false,
    val bookTitle: String = "",
    val authorName: String = "",
    val coverUri: String = "",
    val tracks: List<LibrivoxAudioTrack> = emptyList(),
    val currentTrackIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isLoadingTrack: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val errorMessage: String? = null
)

class AudioPlayerManager(private val context: Context) {

    private val TAG = "AudioPlayerManager"
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()

    private var progressUpdateJob: Job? = null

    init {
        initExoPlayer()
    }

    private fun initExoPlayer() {
        if (exoPlayer != null) return
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        stopProgressUpdates()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _playerState.value = _playerState.value.copy(isLoadingTrack = true)
                        }
                        Player.STATE_READY -> {
                            val duration = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
                            _playerState.value = _playerState.value.copy(
                                isLoadingTrack = false,
                                durationMs = duration,
                                errorMessage = null
                            )
                        }
                        Player.STATE_ENDED -> {
                            _playerState.value = _playerState.value.copy(isLoadingTrack = false)
                            nextTrack()
                        }
                        Player.STATE_IDLE -> {
                            _playerState.value = _playerState.value.copy(isLoadingTrack = false)
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e(TAG, "ExoPlayer playback error", error)
                    _playerState.value = _playerState.value.copy(
                        isLoadingTrack = false,
                        isPlaying = false,
                        errorMessage = "Error playing audio: ${error.localizedMessage ?: "Network issue"}"
                    )
                }
            })
        }
    }

    fun playAudiobook(
        bookTitle: String,
        authorName: String,
        coverUri: String,
        tracks: List<LibrivoxAudioTrack>,
        initialTrackIndex: Int = 0
    ) {
        initExoPlayer()
        if (tracks.isEmpty()) return

        val validIndex = initialTrackIndex.coerceIn(0, tracks.lastIndex)
        _playerState.value = AudioPlayerState(
            isVisible = true,
            bookTitle = bookTitle,
            authorName = authorName,
            coverUri = coverUri,
            tracks = tracks,
            currentTrackIndex = validIndex,
            isLoadingTrack = true,
            playbackSpeed = _playerState.value.playbackSpeed
        )

        loadAndPlayTrack(validIndex)
    }

    private fun loadAndPlayTrack(index: Int) {
        val player = exoPlayer ?: return
        val tracks = _playerState.value.tracks
        if (index !in tracks.indices) return

        val track = tracks[index]
        _playerState.value = _playerState.value.copy(
            currentTrackIndex = index,
            isLoadingTrack = true,
            currentPositionMs = 0L,
            durationMs = track.playtimeSecs * 1000L,
            errorMessage = null
        )

        val mediaItem = MediaItem.fromUri(track.listenUrl)
        player.setMediaItem(mediaItem)
        player.playbackParameters = PlaybackParameters(_playerState.value.playbackSpeed)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun skipForward(seconds: Long = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition + (seconds * 1000L)).coerceAtMost(player.duration.coerceAtLeast(0L))
        player.seekTo(newPos)
        _playerState.value = _playerState.value.copy(currentPositionMs = newPos)
    }

    fun skipBackward(seconds: Long = 10) {
        val player = exoPlayer ?: return
        val newPos = (player.currentPosition - (seconds * 1000L)).coerceAtLeast(0L)
        player.seekTo(newPos)
        _playerState.value = _playerState.value.copy(currentPositionMs = newPos)
    }

    fun setSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun playTrackAtIndex(index: Int) {
        loadAndPlayTrack(index)
    }

    fun nextTrack() {
        val currentState = _playerState.value
        val nextIdx = currentState.currentTrackIndex + 1
        if (nextIdx in currentState.tracks.indices) {
            loadAndPlayTrack(nextIdx)
        }
    }

    fun previousTrack() {
        val currentState = _playerState.value
        val prevIdx = currentState.currentTrackIndex - 1
        if (prevIdx in currentState.tracks.indices) {
            loadAndPlayTrack(prevIdx)
        }
    }

    fun showPlayerSheet() {
        _playerState.value = _playerState.value.copy(isVisible = true)
    }

    fun hidePlayerSheet() {
        _playerState.value = _playerState.value.copy(isVisible = false)
    }

    fun stopAndRelease() {
        stopProgressUpdates()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        _playerState.value = AudioPlayerState()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = scope.launch {
            while (isActive) {
                val player = exoPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = pos,
                        durationMs = if (dur > 0) dur else _playerState.value.durationMs
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
}
