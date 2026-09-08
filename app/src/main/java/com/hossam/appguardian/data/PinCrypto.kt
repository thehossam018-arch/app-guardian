package com.hossam.appguardian.data

import android.util.Base64
import com.hossam.appguardian.utils.Constants
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN storage: never plaintext. Each PIN gets a fresh random salt, run through PBKDF2-HMAC-SHA256
 * with a high iteration count. Only the salt + resulting hash are ever persisted (as Base64
 * strings in DataStore) — the PIN itself never touches disk, and nothing here ever touches the
 * network.
 */
object PinCrypto {

    data class HashedPin(val saltBase64: String, val hashBase64: String)

    fun hashPin(pin: String): HashedPin {
        val salt = ByteArray(Constants.SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(pin, salt)
        return HashedPin(
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        )
    }

    fun verifyPin(pin: String, stored: HashedPin): Boolean {
        val salt = Base64.decode(stored.saltBase64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(stored.hashBase64, Base64.NO_WRAP)
        val candidateHash = pbkdf2(pin, salt)
        return candidateHash.contentEquals(expectedHash)
    }

    private fun pbkdf2(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            Constants.PBKDF2_ITERATIONS,
            Constants.PBKDF2_KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
