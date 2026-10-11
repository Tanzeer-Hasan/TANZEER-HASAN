package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "spendwise_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to standard private preferences if EncryptedSharedPreferences has KeyStore issues on older emulators
            context.getSharedPreferences("spendwise_secure_fallback", Context.MODE_PRIVATE)
        }
    }

    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_PIN_HASH)
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun getBaselineIncome(): Double {
        return sharedPreferences.getFloat(KEY_BASELINE_INCOME, 0f).toDouble()
    }

    fun setBaselineIncome(amount: Double) {
        sharedPreferences.edit().putFloat(KEY_BASELINE_INCOME, amount.toFloat()).apply()
    }

    fun getBaselineFixedExpenses(): Double {
        return sharedPreferences.getFloat(KEY_BASELINE_FIXED_EXPENSES, 0f).toDouble()
    }

    fun setBaselineFixedExpenses(amount: Double) {
        sharedPreferences.edit().putFloat(KEY_BASELINE_FIXED_EXPENSES, amount.toFloat()).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun saveUserPin(pin: String): Boolean {
        if (pin.length < 4) return false

        val salt = generateSalt()
        val hash = hashPinWithSalt(pin, salt)

        sharedPreferences.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hash)
            .apply()

        return true
    }

    fun verifyUserPin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        val storedSaltBase64 = sharedPreferences.getString(KEY_PIN_SALT, null) ?: return false

        val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val computedHash = hashPinWithSalt(pin, salt)

        return storedHash == computedHash
    }

    fun isBiometricHardwareAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        digest.update(salt)
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    companion object {
        private const val KEY_PIN_HASH = "key_user_pin_hash"
        private const val KEY_PIN_SALT = "key_user_pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"
        private const val KEY_BASELINE_INCOME = "key_baseline_income"
        private const val KEY_BASELINE_FIXED_EXPENSES = "key_baseline_fixed_expenses"
    }
}
