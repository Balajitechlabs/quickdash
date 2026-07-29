package com.balajitechlabs.quickdash.core.data

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

object HistorySerializer : Serializer<HistoryPreferences> {
    override val defaultValue: HistoryPreferences = HistoryPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): HistoryPreferences {
        try {
            return HistoryPreferences.parseFrom(input)
        } catch (exception: androidx.datastore.core.CorruptionException) {
            throw androidx.datastore.core.CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: HistoryPreferences,
        output: OutputStream
    ) {
        t.writeTo(output)
    }
}
