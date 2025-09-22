package com.example.quitesmoking.urge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MindfulnessViewModel(
    private val repo: MindfulnessRepoFs = MindfulnessRepoFs()
) : ViewModel() {

    private val _videos = MutableStateFlow<List<VideoDoc>>(emptyList())
    val videos: StateFlow<List<VideoDoc>> = _videos

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            _videos.value = repo.fetchAll()
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load"
        } finally {
            _loading.value = false
        }
    }
}
