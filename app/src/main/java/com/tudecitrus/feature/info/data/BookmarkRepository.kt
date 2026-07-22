package com.tudecitrus.feature.info.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository untuk mengelola bookmark penyakit.
 * Disimpan di SharedPreferences agar persistent tanpa perlu migrasi Room.
 */
interface BookmarkRepository {
    val bookmarkedIds: StateFlow<Set<Int>>
    fun isBookmarked(diseaseId: Int): Boolean
    fun toggleBookmark(diseaseId: Int)
}

class SharedPrefsBookmarkRepository(
    context: Context
) : BookmarkRepository {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _bookmarkedIds = MutableStateFlow(loadFromPrefs())
    override val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

    override fun isBookmarked(diseaseId: Int): Boolean {
        return _bookmarkedIds.value.contains(diseaseId)
    }

    override fun toggleBookmark(diseaseId: Int) {
        val current = _bookmarkedIds.value.toMutableSet()
        if (!current.add(diseaseId)) {
            current.remove(diseaseId)
        }
        _bookmarkedIds.value = current
        saveToPrefs(current)
    }

    private fun loadFromPrefs(): Set<Int> {
        val raw = prefs.getStringSet(KEY_BOOKMARKED_IDS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveToPrefs(ids: Set<Int>) {
        prefs.edit()
            .putStringSet(KEY_BOOKMARKED_IDS, ids.map { it.toString() }.toSet())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "info_bookmark_prefs"
        private const val KEY_BOOKMARKED_IDS = "bookmarked_disease_ids"
    }
}
