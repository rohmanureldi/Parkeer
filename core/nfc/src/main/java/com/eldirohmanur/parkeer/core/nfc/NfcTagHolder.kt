package com.eldirohmanur.parkeer.core.nfc

import android.nfc.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcTagHolder @Inject constructor() {
    private val _tags = MutableSharedFlow<Tag>(extraBufferCapacity = 1)
    val tags: SharedFlow<Tag> = _tags

    /** Emitted when a tag tap is ignored because the system is busy processing. */
    private val _busyTaps = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val busyTaps: SharedFlow<Unit> = _busyTaps

    fun dispatch(tag: Tag) {
        _tags.tryEmit(tag)
    }

    fun notifyBusy() {
        _busyTaps.tryEmit(Unit)
    }
}
