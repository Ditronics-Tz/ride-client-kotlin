package com.example.ridepassenger2.data.repository

import com.example.ridepassenger2.data.remote.PassengerPreferencesDto
import com.example.ridepassenger2.data.remote.RidaApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class CommunicationState(
    val preferences: PassengerPreferencesDto = PassengerPreferencesDto(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null
)

/** One acknowledged preference update at a time; failed saves never become selected settings. */
internal class CommunicationPreferences(private val api: RidaApi, private val scope: CoroutineScope) {
    private val mutable = MutableStateFlow(CommunicationState())
    val state = mutable.asStateFlow()
    fun refresh() {
        if (mutable.value.saving) return
        mutable.value = mutable.value.copy(loading = true, error = null)
        scope.launch {
            try {
                val response = api.getPreferences()
                currentCoroutineContext().ensureActive()
                mutable.value = CommunicationState(response, loading = false)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                mutable.value = mutable.value.copy(loading = false, error = "Could not load preferences. Retry before changing them.")
            }
        }
    }
    fun change(key: String, value: Boolean) {
        val previous = mutable.value
        if (previous.loading || previous.saving || previous.error != null) return
        mutable.value = previous.copy(saving = true)
        scope.launch {
            try {
                val response = api.updatePreferences(mapOf(key to value))
                currentCoroutineContext().ensureActive()
                mutable.value = CommunicationState(response, loading = false)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                mutable.value = previous.copy(error = "Preference was not saved. Reload and try again.")
            }
        }
    }
}
