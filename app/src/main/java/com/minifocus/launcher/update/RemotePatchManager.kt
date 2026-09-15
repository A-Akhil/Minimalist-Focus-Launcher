/*
 * Minimalist Focus Launcher
 * Copyright (C) 2025 A-Akhil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.minifocus.launcher.update

import android.content.Context
import android.util.Base64
import android.util.Log
import com.minifocus.launcher.BuildConfig
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Remote "data patches": a small signed JSON file hosted on GitHub that can change
 * values the app already knows how to read (feature flags, thresholds, lists, text).
 *
 * It deliberately never downloads code. Google Play's Device and Network Abuse policy
 * forbids Play-distributed apps from loading executable code (dex/jar/so) from anywhere
 * other than Play, so real code changes must still ship as a Play update.
 *
 * Envelope format (what is hosted):
 * {
 *   "payload":   "<base64 of the payload JSON bytes>",
 *   "signature": "<base64 DER ECDSA P-256/SHA-256 signature over the payload bytes>"
 * }
 *
 * Payload format:
 * {
 *   "patchVersion": 3,              // must increase; older/equal patches are ignored
 *   "minVersionCode": 21,           // optional, inclusive
 *   "maxVersionCode": 30,           // optional, inclusive
 *   "values": { "some.flag": true, "some.limit": 5, "some.text": "hi" }
 * }
 */
class RemotePatchManager(
    private val context: Context,
    private val appVersionCode: Long
) {

    private val patchFile = File(context.filesDir, PATCH_FILE_NAME)

    // The periodic worker and the manual "Check for updates" button can overlap.
    private val checkMutex = Mutex()

    private val _values = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val values: StateFlow<Map<String, Any?>> = _values.asStateFlow()

    private val _patchVersion = MutableStateFlow(0L)
    val patchVersion: StateFlow<Long> = _patchVersion.asStateFlow()

    val isConfigured: Boolean
        get() = PUBLIC_KEY_BASE64.isNotBlank()

    /** Loads the last applied patch from disk. Safe to call on every app start. */
    suspend fun loadApplied() = withContext(Dispatchers.IO) {
        if (!patchFile.exists()) return@withContext
        try {
            val patch = parsePayload(patchFile.readText())
            if (patch != null && isCompatible(patch)) {
                publish(patch)
                Log.i(TAG, "Loaded stored patch v${patch.version}")
            } else {
                // The app was updated past this patch's range; drop it.
                patchFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Discarding unreadable stored patch", e)
            patchFile.delete()
        }
    }

    /**
     * Fetches, verifies and applies the hosted patch.
     * Returns true when a newer patch was applied.
     * Throws [IOException] on network errors so the caller can retry.
     */
    suspend fun checkForPatch(): Boolean = checkMutex.withLock { fetchAndApply() }

    private suspend fun fetchAndApply(): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false

        Log.i(TAG, "Checking ${BuildConfig.PATCH_URL} (current patch v${_patchVersion.value})")
        // No patch file published yet: nothing to apply, and nothing a retry would fix.
        val body = download(BuildConfig.PATCH_URL) ?: run {
            Log.i(TAG, "No patch published (HTTP 404)")
            return@withContext false
        }
        val envelope = JSONObject(body)
        val payloadBytes = Base64.decode(envelope.getString("payload"), Base64.DEFAULT)
        val signature = Base64.decode(envelope.getString("signature"), Base64.DEFAULT)

        if (!verify(payloadBytes, signature)) {
            Log.w(TAG, "Rejected patch with invalid signature")
            return@withContext false
        }

        val payloadJson = String(payloadBytes, Charsets.UTF_8)
        val patch = parsePayload(payloadJson) ?: run {
            Log.w(TAG, "Signed patch has no valid patchVersion")
            return@withContext false
        }
        if (patch.version <= _patchVersion.value) {
            Log.i(TAG, "Up to date: hosted v${patch.version}, applied v${_patchVersion.value}")
            return@withContext false
        }
        if (!isCompatible(patch)) {
            Log.i(TAG, "Patch v${patch.version} not for versionCode $appVersionCode")
            return@withContext false
        }

        // Write atomically so a crash mid-write never leaves a half-written patch.
        val tmp = File(context.filesDir, "$PATCH_FILE_NAME.tmp")
        tmp.writeText(payloadJson)
        if (!tmp.renameTo(patchFile)) {
            tmp.delete()
            throw IOException("Could not store patch")
        }
        publish(patch)
        Log.i(TAG, "Applied remote patch v${patch.version}: ${patch.values}")
        true
    }

    fun getBoolean(key: String, default: Boolean): Boolean =
        _values.value[key] as? Boolean ?: default

    fun getInt(key: String, default: Int): Int =
        (_values.value[key] as? Number)?.toInt() ?: default

    fun getLong(key: String, default: Long): Long =
        (_values.value[key] as? Number)?.toLong() ?: default

    fun getString(key: String, default: String): String =
        _values.value[key] as? String ?: default

    private fun publish(patch: Patch) {
        // loadApplied() and checkForPatch() can race at startup; never go backwards.
        if (patch.version < _patchVersion.value) return
        _values.value = patch.values
        _patchVersion.value = patch.version
    }

    private fun isCompatible(patch: Patch): Boolean {
        if (patch.minVersionCode != null && appVersionCode < patch.minVersionCode) return false
        if (patch.maxVersionCode != null && appVersionCode > patch.maxVersionCode) return false
        return true
    }

    private fun verify(payload: ByteArray, signature: ByteArray): Boolean = try {
        val keyBytes = Base64.decode(PUBLIC_KEY_BASE64, Base64.DEFAULT)
        val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(keyBytes))
        Signature.getInstance("SHA256withECDSA").run {
            initVerify(publicKey)
            update(payload)
            verify(signature)
        }
    } catch (e: Exception) {
        Log.w(TAG, "Signature verification failed", e)
        false
    }

    private fun parsePayload(json: String): Patch? {
        val obj = JSONObject(json)
        val version = obj.optLong("patchVersion", -1L)
        if (version <= 0L) return null
        val valuesObj = obj.optJSONObject("values") ?: JSONObject()
        val values = buildMap {
            valuesObj.keys().forEach { key ->
                val raw = valuesObj.get(key)
                put(key, if (raw == JSONObject.NULL) null else raw)
            }
        }
        return Patch(
            version = version,
            minVersionCode = if (obj.has("minVersionCode")) obj.getLong("minVersionCode") else null,
            maxVersionCode = if (obj.has("maxVersionCode")) obj.getLong("maxVersionCode") else null,
            values = values
        )
    }

    /** Returns null when the file does not exist (HTTP 404). */
    private fun download(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return null
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.use { input ->
                val buffer = ByteArray(8 * 1024)
                val out = java.io.ByteArrayOutputStream()
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    if (out.size() > MAX_PATCH_BYTES) throw IOException("Patch too large")
                }
                return out.toString(Charsets.UTF_8.name())
            }
        } finally {
            connection.disconnect()
        }
    }

    private data class Patch(
        val version: Long,
        val minVersionCode: Long?,
        val maxVersionCode: Long?,
        val values: Map<String, Any?>
    )

    companion object {
        private const val TAG = "RemotePatch"
        private const val PATCH_FILE_NAME = "remote_patch.json"
        private const val MAX_PATCH_BYTES = 2 * 1024 * 1024

        // Base64 X.509 (SubjectPublicKeyInfo) EC P-256 public key from scripts/remote-patch/keygen.sh.
        // While blank, patch checks are disabled.
        private const val PUBLIC_KEY_BASE64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+38sMNe2TOAREJYaZR1JBtqYZF4e7ievCXxBzu0mfF57Ot2U34dqzMF4jQft/nnaGO+OEUYpMfXGLbUyOQMM6w=="
    }
}
