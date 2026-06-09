package com.altf4.ourfinance.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE)
    private val KEY_LOGGED_IN_USER_EMAIL = "logged_in_user_email"
    private val KEY_LOGGED_IN_USER_NAME = "logged_in_user_name"
    private val KEY_LOGGED_IN_USER_PIC = "logged_in_user_pic"

    // Google Apps Script Web App Deployment URL
    private val BASE_URL = "https://script.google.com/macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec"

    private val _authenticatedUser = MutableStateFlow<GoogleUser?>(null)
    val authenticatedUser: StateFlow<GoogleUser?> = _authenticatedUser.asStateFlow()

    init {
        val savedEmail = sharedPrefs.getString(KEY_LOGGED_IN_USER_EMAIL, null)
        if (savedEmail != null) {
            val user = GoogleUser(
                displayName = sharedPrefs.getString(KEY_LOGGED_IN_USER_NAME, ""),
                email = savedEmail,
                profilePictureUrl = sharedPrefs.getString(KEY_LOGGED_IN_USER_PIC, null),
                apiParamName = getApiParamName(savedEmail)
            )
            _authenticatedUser.value = user
            UserManager.updateUser(user)
        }
    }

    fun setAuthenticatedUser(user: GoogleUser?) {
        _authenticatedUser.value = user
        if (user != null) {
            sharedPrefs.edit().apply {
                putString(KEY_LOGGED_IN_USER_EMAIL, user.email)
                putString(KEY_LOGGED_IN_USER_NAME, user.displayName)
                putString(KEY_LOGGED_IN_USER_PIC, user.profilePictureUrl)
                apply()
            }
            UserManager.updateUser(user)
        } else {
            sharedPrefs.edit().clear().apply()
        }
    }

    /**
     * Specialized logic for Google OAuth entry.
     * Fetches current Google profile picture and forces an overwrite to backend storage.
     */
    fun handleGoogleLogin(
        email: String,
        displayName: String?,
        photoUrl: String?,
        context: Context,
        onComplete: () -> Unit = {}
    ) {
        val emailClean = email.trim().lowercase()
        if (!allowedEmails.contains(emailClean)) {
            Log.w("AuthViewModel", "Access Denied: Email not whitelisted for app access.")
            return
        }

        val user = GoogleUser(
            displayName = displayName ?: emailClean.split("@").firstOrNull() ?: "User",
            email = emailClean,
            profilePictureUrl = photoUrl, // Temporarily use Google URL
            apiParamName = getApiParamName(emailClean)
        )
        setAuthenticatedUser(user)

        // If Google provides a photo, fetch it and upload to our backend storage (Google Drive)
        if (!photoUrl.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(photoUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    
                    if (bitmap != null) {
                        uploadBitmapToBackend(bitmap, user.apiParamName) { success, driveUrl ->
                            if (success && driveUrl != null) {
                                val finalUser = user.copy(profilePictureUrl = driveUrl)
                                viewModelScope.launch(Dispatchers.Main) {
                                    setAuthenticatedUser(finalUser)
                                    onComplete()
                                }
                            } else {
                                viewModelScope.launch(Dispatchers.Main) { onComplete() }
                            }
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) { onComplete() }
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to sync Google photo to backend", e)
                    viewModelScope.launch(Dispatchers.Main) { onComplete() }
                }
            }
        } else {
            onComplete()
        }
    }

    private fun uploadBitmapToBackend(
        bitmap: Bitmap,
        username: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            // Scale and compress
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64EncodedPayload = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            val jsonRequest = JSONObject().apply {
                put("action", "updateProfileImage")
                put("username", username)
                put("imageBytes", base64EncodedPayload)
                put("mimeType", "image/jpeg")
            }

            val url = URL(BASE_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(jsonRequest.toString())
            wr.flush()
            wr.close()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val resText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(resText)
                if (jsonResponse.optString("status") == "success") {
                    val driveUrl = jsonResponse.getString("url")
                    onResult(true, driveUrl)
                } else {
                    onResult(false, null)
                }
            } else {
                onResult(false, null)
            }
            scaledBitmap.recycle()
        } catch (e: Exception) {
            onResult(false, null)
        }
    }

    fun uploadProfileImage(imageUri: Uri, context: Context, onResult: (Boolean, String?) -> Unit) {
        val currentUser = _authenticatedUser.value ?: return onResult(false, "No authenticated user initialized")
        val username = currentUser.apiParamName

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Downsample and compress to eliminate payload memory pressure
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                    withContext(Dispatchers.Main) { onResult(false, "Failed to resolve image data") }
                    return@launch
                }

                // 1:1 Aspect Ratio Cropping (Center-Crop)
                val width = originalBitmap.width
                val height = originalBitmap.height
                val newDimension = if (width > height) height else width
                val left = (width - newDimension) / 2
                val top = (height - newDimension) / 2
                val croppedBitmap = Bitmap.createBitmap(originalBitmap, left, top, newDimension, newDimension)

                // Scale down smartly (e.g., max 300x300 pixels for a perfect avatar size)
                val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val imageBytes = outputStream.toByteArray()
                val base64EncodedPayload = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                // Assemble clean JSON execution command packet
                val jsonRequest = JSONObject().apply {
                    put("action", "updateProfileImage")
                    put("username", username)
                    put("imageBytes", base64EncodedPayload)
                    put("mimeType", "image/jpeg")
                }

                val url = URL(BASE_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val wr = OutputStreamWriter(conn.outputStream)
                wr.write(jsonRequest.toString())
                wr.flush()
                wr.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val resText = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(resText)
                    
                    if (jsonResponse.optString("status") == "success") {
                        val generatedDriveUrl = jsonResponse.getString("url")
                        
                        // Parse profile maps returned directly from cloud transactional logs
                        if (jsonResponse.has("userProfiles")) {
                            val profilesObj = jsonResponse.getJSONObject("userProfiles")
                            val freshProfilesMap = mutableMapOf<String, String>()
                            profilesObj.keys().forEach { key ->
                                freshProfilesMap[key] = profilesObj.getString(key)
                            }
                            UserManager.syncUserProfiles(freshProfilesMap)
                        }

                        // Update current state locally
                        val updatedUser = currentUser.copy(profilePictureUrl = generatedDriveUrl)
                        withContext(Dispatchers.Main) {
                            setAuthenticatedUser(updatedUser)
                            onResult(true, generatedDriveUrl)
                        }
                    } else {
                        withContext(Dispatchers.Main) { onResult(false, jsonResponse.optString("message", "Upload rejected")) }
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, "Server network error: $responseCode") }
                }
                
                // Cleanup bitmaps
                originalBitmap.recycle()
                if (croppedBitmap != originalBitmap) {
                    croppedBitmap.recycle()
                }
                scaledBitmap.recycle()
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Secure Drive image distribution failed", e)
                withContext(Dispatchers.Main) { onResult(false, e.localizedMessage) }
            }
        }
    }

    /**
     * Updates the user's name in the backend spreadsheet database.
     * Note: Profile picture updates are now handled separately via uploadProfileImage.
     */
    suspend fun updateUserProfile(name: String): Boolean {
        val currentUser = _authenticatedUser.value ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val encodedEmail = URLEncoder.encode(currentUser.email, "UTF-8")
                val encodedName = URLEncoder.encode(name, "UTF-8")

                // We only update the name here, as the photoUrl (if it's a local path) 
                // is handled by AccessibilityScreen calling uploadProfileImage if needed.
                val urlString = "$BASE_URL?action=updateProfile&email=$encodedEmail&fullName=$encodedName"
                Log.d("AuthViewModel", "Sending name update request to URL")

                val response = executeGetRequest(urlString)
                if (response != null) {
                    val updatedUser = currentUser.copy(displayName = name)
                    withContext(Dispatchers.Main) {
                        setAuthenticatedUser(updatedUser)
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error occurred during name update", e)
                false
            }
        }
    }

    private suspend fun executeGetRequest(urlString: String): String? = withContext(Dispatchers.IO) {
        try {
            var url = URL(urlString)
            var connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = false

            var responseCode = connection.responseCode
            var redirectCount = 0
            val maxRedirects = 5

            while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        responseCode == 307) && redirectCount < maxRedirects) {

                val newUrlString = connection.getHeaderField("Location")
                if (newUrlString == null) break

                connection.disconnect()
                url = URL(newUrlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = false

                responseCode = connection.responseCode
                redirectCount++
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                reader.use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginWithPassword(email: String, password: String): String? {
        val emailClean = email.trim().lowercase()
        if (!allowedEmails.contains(emailClean)) {
            return "Access Denied: Email not whitelisted."
        }

        return withContext(Dispatchers.IO) {
            try {
                val encodedEmail = URLEncoder.encode(emailClean, "UTF-8")
                val encodedPassword = URLEncoder.encode(password, "UTF-8")
                val urlString = "$BASE_URL?action=loginWithPassword&email=$encodedEmail&password=$encodedPassword"

                val response = executeGetRequest(urlString) ?: return@withContext "Network connection failed. Try again."
                val json = JSONObject(response)

                if (json.optString("status") == "success") {
                    val user = GoogleUser(
                        displayName = json.optString("fullName", "User"),
                        email = emailClean,
                        profilePictureUrl = json.optString("profilePictureUrl", ""),
                        apiParamName = getApiParamName(emailClean)
                    )
                    withContext(Dispatchers.Main) {
                        setAuthenticatedUser(user)
                    }
                    null
                } else {
                    json.optString("message", "Incorrect password or credentials.")
                }
            } catch (e: Exception) {
                "An unexpected error occurred."
            }
        }
    }

    suspend fun updatePasswordOnly(password: String): Boolean {
        val currentUser = _authenticatedUser.value ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val encodedEmail = URLEncoder.encode(currentUser.email, "UTF-8")
                val encodedPassword = URLEncoder.encode(password, "UTF-8")
                val urlString = "$BASE_URL?action=updatePasswordOnly&email=$encodedEmail&password=$encodedPassword"

                val response = executeGetRequest(urlString)
                if (response != null) {
                    val json = JSONObject(response)
                    json.optString("status") == "success"
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    val allowedEmails = setOf(
        "arnab.banik299@gmail.com",
        "sadmanhossainwork@gmail.com",
        "sabbirtonmoy911@gmail.com"
    )

    fun getApiParamName(email: String): String {
        return when (email.lowercase().trim()) {
            "sadmanhossainwork@gmail.com" -> "Sadman"
            "arnab.banik299@gmail.com" -> "Arnab"
            "sabbirtonmoy911@gmail.com" -> "Sabbir"
            else -> "Guest"
        }
    }

    fun logout() {
        _authenticatedUser.value = null
        sharedPrefs.edit().clear().apply()
    }
}
