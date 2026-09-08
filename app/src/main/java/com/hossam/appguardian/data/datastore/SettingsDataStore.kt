package com.hossam.appguardian.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hossam.appguardian.data.PinCrypto
import com.hossam.appguardian.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.settingsDataStoreImpl by preferencesDataStore(
    name = Constants.SETTINGS_DATASTORE_NAME
)

// File-scoped so both the instance methods below and the companion's blocking early-read can
// share the exact same key constants.
private val KEY_BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
private val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
private val KEY_LANGUAGE_TAG = stringPreferencesKey("language_tag")

data class AppSettingsState(
    val isBlockingEnabled: Boolean = true,
    val isPinSet: Boolean = false,
    val languageTag: String? = null // null = follow system
)

/**
 * This is the one DataStore file excluded from Android backup/device-transfer (see
 * data_extraction_rules.xml / backup_rules.xml) because it holds the PIN hash+salt.
 */
class SettingsDataStore(private val context: Context) {

    val settingsFlow: Flow<AppSettingsState> = context.settingsDataStoreImpl.data.map { prefs ->
        AppSettingsState(
            isBlockingEnabled = prefs[KEY_BLOCKING_ENABLED] ?: true,
            isPinSet = prefs[KEY_PIN_HASH] != null,
            languageTag = prefs[KEY_LANGUAGE_TAG]
        )
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.settingsDataStoreImpl.edit { it[KEY_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setLanguageTag(tag: String?) {
        context.settingsDataStoreImpl.edit { prefs ->
            if (tag == null) prefs.remove(KEY_LANGUAGE_TAG) else prefs[KEY_LANGUAGE_TAG] = tag
        }
    }

    suspend fun setPin(pin: String) {
        val hashed = PinCrypto.hashPin(pin)
        context.settingsDataStoreImpl.edit { prefs ->
            prefs[KEY_PIN_SALT] = hashed.saltBase64
            prefs[KEY_PIN_HASH] = hashed.hashBase64
        }
    }

    suspend fun clearPin() {
        context.settingsDataStoreImpl.edit { prefs ->
            prefs.remove(KEY_PIN_SALT)
            prefs.remove(KEY_PIN_HASH)
        }
    }

    suspend fun verifyPin(candidate: String): Boolean {
        val prefs = context.settingsDataStoreImpl.data.first()
        val salt = prefs[KEY_PIN_SALT] ?: return false
        val hash = prefs[KEY_PIN_HASH] ?: return false
        return PinCrypto.verifyPin(candidate, PinCrypto.HashedPin(salt, hash))
    }

    companion object {
        /**
         * A synchronous read, used ONLY from Application/Activity attachBaseContext() -- which
         * Android calls before onCreate() and before any coroutine scope exists -- to decide
         * which locale to wrap the base Context in. DataStore's underlying file read is small and
         * fast, so this narrow, early, one-shot blocking read is the standard accepted pattern for
         * manual per-app language switching without pulling in an extra library.
         */
        fun readLanguageTagBlocking(context: Context): String? = runBlocking {
            context.settingsDataStoreImpl.data.map { it[KEY_LANGUAGE_TAG] }.first()
        }
    }
}
