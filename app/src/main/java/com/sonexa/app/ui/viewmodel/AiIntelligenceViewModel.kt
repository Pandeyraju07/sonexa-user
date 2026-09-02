package com.sonexa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonexa.app.data.model.*
import com.sonexa.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiIntelligenceUiState(
    val isLoading: Boolean = false,
    val musicDna: MusicDnaResponseDto? = null,
    val insights: ListeningInsightsResponseDto? = null,
    val predictions: List<PredictionItemDto> = emptyList(),
    val currentWhyThisSong: WhyThisSongResponseDto? = null,
    val currentJourney: MusicJourneyResponseDto? = null,
    val lastVibeChange: ChangeVibeResponseDto? = null,
    val lastQueueFix: FixQueueResponseDto? = null,
    val nextDjDecision: NextTrackDecisionDto? = null,
    val voiceResult: VoiceSearchResponseDto? = null,
    val generatedPlaylistTracks: List<TrackDto> = emptyList(),
    val errorMessage: String? = null
)

class AiIntelligenceViewModel(
    private val repository: AiRepository = AiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiIntelligenceUiState())
    val uiState: StateFlow<AiIntelligenceUiState> = _uiState.asStateFlow()

    init {
        loadMusicDna()
        loadListeningInsights()
        loadPredictions()
    }

    fun loadMusicDna() {
        viewModelScope.launch {
            repository.getMusicDna().onSuccess { dna ->
                _uiState.value = _uiState.value.copy(musicDna = dna)
            }
        }
    }

    fun loadListeningInsights() {
        viewModelScope.launch {
            repository.getListeningInsights().onSuccess { ins ->
                _uiState.value = _uiState.value.copy(insights = ins)
            }
        }
    }

    fun loadPredictions() {
        viewModelScope.launch {
            repository.getPredictions().onSuccess { preds ->
                _uiState.value = _uiState.value.copy(predictions = preds)
            }
        }
    }

    fun requestWhyThisSong(trackId: String) {
        viewModelScope.launch {
            repository.getWhyThisSong(trackId).onSuccess { why ->
                _uiState.value = _uiState.value.copy(currentWhyThisSong = why)
            }
        }
    }

    fun createMusicJourney(theme: String, durationMinutes: Int, onComplete: (MusicJourneyResponseDto) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.createJourney(theme, durationMinutes).onSuccess { journey ->
                _uiState.value = _uiState.value.copy(isLoading = false, currentJourney = journey)
                onComplete(journey)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to build journey")
            }
        }
    }

    fun changeVibe(
        vibe: String,
        currentQueue: List<TrackDto>,
        currentTrack: TrackDto?,
        onQueueUpdated: (List<TrackDto>) -> Unit
    ) {
        viewModelScope.launch {
            repository.changeVibe(vibe, currentQueue, currentTrack).onSuccess { response ->
                _uiState.value = _uiState.value.copy(lastVibeChange = response)
                onQueueUpdated(response.reorderedQueue)
            }
        }
    }

    fun fixQueue(queue: List<TrackDto>, onQueueFixed: (List<TrackDto>, String) -> Unit) {
        viewModelScope.launch {
            repository.fixQueue(queue).onSuccess { response ->
                _uiState.value = _uiState.value.copy(lastQueueFix = response)
                onQueueFixed(response.balancedQueue, response.balanceSummary)
            }
        }
    }

    fun triggerDjNext(currentTrack: TrackDto?, onDecision: (TrackDto, String) -> Unit) {
        viewModelScope.launch {
            repository.djNext(currentTrack).onSuccess { decision ->
                _uiState.value = _uiState.value.copy(nextDjDecision = decision)
                if (decision.track != null) {
                    onDecision(decision.track, decision.reason)
                }
            }
        }
    }

    fun generateAiPlaylist(prompt: String, onGenerated: (List<TrackDto>) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.generateAiPlaylist(prompt).onSuccess { tracks ->
                _uiState.value = _uiState.value.copy(isLoading = false, generatedPlaylistTracks = tracks)
                onGenerated(tracks)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to generate AI playlist")
            }
        }
    }

    fun executeVoiceSearch(
        transcript: String,
        language: String = "en",
        onExecuteIntent: (VoiceSearchResponseDto) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.voiceSearch(transcript, language).onSuccess { response ->
                _uiState.value = _uiState.value.copy(isLoading = false, voiceResult = response)
                onExecuteIntent(response)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Voice search failed")
            }
        }
    }
}