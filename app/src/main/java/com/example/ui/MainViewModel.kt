package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.database.AppDatabase
import com.example.database.PrescriptionEntity
import com.example.database.PrescriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState()
    class OtpSent(val phoneNumber: String) : AuthState()
    class Authenticated(val phoneNumber: String) : AuthState()
}

class MainViewModel(private val repository: PrescriptionRepository) : ViewModel() {

    // Dark Mode Theme settings
    private val _isDarkMode = MutableStateFlow(true) // Default to elegant Dark Mode as requested
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Authentication stage
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Scanned items history from Room DB
    val prescriptionsHistory: StateFlow<List<PrescriptionEntity>> = repository.allPrescriptions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Scan State
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _activeScanResult = MutableStateFlow<String?>(null)
    val activeScanResult: StateFlow<String?> = _activeScanResult.asStateFlow()

    private val _activeScannedTitle = MutableStateFlow<String>("")
    val activeScannedTitle: StateFlow<String> = _activeScannedTitle.asStateFlow()

    // For viewing a historical item
    private val _focusedPrescription = MutableStateFlow<PrescriptionEntity?>(null)
    val focusedPrescription: StateFlow<PrescriptionEntity?> = _focusedPrescription.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // SIMULATED AUTHENTICATION ACTIONS
    fun sendOtp(phone: String, onSent: () -> Unit) {
        viewModelScope.launch {
            _isScanning.value = true
            // Simulate network delay
            kotlinx.coroutines.delay(1200)
            _isScanning.value = false
            _authState.value = AuthState.OtpSent(phone)
            onSent()
        }
    }

    fun verifyOtp(phone: String, otp: String, onVerified: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            _isScanning.value = true
            kotlinx.coroutines.delay(1000)
            _isScanning.value = false
            if (otp == "123456" || otp.length >= 4) { // accept 123456 or any 4-6 digits for standard prototype testing as required
                _authState.value = AuthState.Authenticated(phone)
                onVerified()
            } else {
                onError()
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
        _selectedBitmap.value = null
        _activeScanResult.value = null
        _focusedPrescription.value = null
    }

    // SCAN & OCR ACTIONS
    fun selectImage(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        _activeScanResult.value = null
    }

    fun clearActiveScan() {
        _selectedBitmap.value = null
        _activeScanResult.value = null
        _activeScannedTitle.value = ""
    }

    fun runDetections(title: String, isTest: Boolean, callback: () -> Unit) {
        val bitmap = _selectedBitmap.value
        if (bitmap == null && !GeminiService.isApiKeyPlaceholder()) {
            return
        }

        viewModelScope.launch {
            _isScanning.value = true
            val prompt = if (isTest) {
                "الرجاء تحليل صورة هذا التحليل الطبي '$title' ومقارنة النسب والأرقام وعرض العناوين بوضوح."
            } else {
                "الرجاء قراءة تفاصيل هذه الراجيتة/الوصفة الطبية '$title' بدقة وعرض أسماء الأدوية العلمية والتجارية والشركة والجرعات والتعليمات."
            }

            // Call model safely using GeminiService
            val ocrTextResult = GeminiService.scanPrescriptionOrAnalysis(bitmap, prompt)
            
            _activeScanResult.value = ocrTextResult
            _activeScannedTitle.value = title.ifBlank { if (isTest) "تحليل طبي" else "راجيتة طبية" }
            _isScanning.value = false

            // Automatically save to local database history for premium experience
            val finalTitle = title.ifBlank { if (isTest) "تحليل طبي" else "راجيتة طبية" }
            val entity = PrescriptionEntity(
                title = finalTitle,
                resultText = ocrTextResult,
                imageUrl = null, // can be evolved to file path if needed, we keep null for light database size
                type = if (isTest) "TEST" else "PRESCRIPTION"
            )
            repository.insert(entity)
            callback()
        }
    }

    fun deleteHistoryId(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_focusedPrescription.value?.id == id) {
                _focusedPrescription.value = null
            }
        }
    }

    fun viewPrescriptionDetails(prescription: PrescriptionEntity) {
        _focusedPrescription.value = prescription
    }

    fun closePrescriptionDetails() {
        _focusedPrescription.value = null
    }
}

// factory configuration to instantiate Room db safely
class ViewModelFactory(private val repository: PrescriptionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
