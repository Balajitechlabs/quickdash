package com.balajitechlabs.quickdash.repository

import com.balajitechlabs.quickdash.core.data.HistoryPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HistoryRepositoryTest {

    @Test
    fun `default HistoryPreferences has empty histories`() {
        val defaultPrefs = HistoryPreferences.getDefaultInstance()
        assertThat(defaultPrefs.searchHistoryList).isEmpty()
        assertThat(defaultPrefs.notificationHistory).isEmpty()
    }

    @Test
    fun `adding search item builds correct list ordering`() {
        val builder = HistoryPreferences.newBuilder()
        val list = mutableListOf("query1", "query2")
        list.remove("query1")
        list.add(0, "query1")
        
        builder.clearSearchHistory().addAllSearchHistory(list)
        val result = builder.build()

        assertThat(result.searchHistoryList).containsExactly("query1", "query2").inOrder()
    }
}
