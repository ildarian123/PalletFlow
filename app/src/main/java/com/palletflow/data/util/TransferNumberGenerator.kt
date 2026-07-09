package com.palletflow.data.util

import com.palletflow.domain.usecase.transfer.ValidateTransferNumberUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TransferNumberGenerator {
    
    suspend fun generateUniqueTransferNumber(
        validateUseCase: ValidateTransferNumberUseCase
    ): String {
        var attempts = 0
        val maxAttempts = 10
        
        while (attempts < maxAttempts) {
            val number = generate()
            if (validateUseCase(number)) {
                return number
            }
            attempts++
        }
        
        // Fallback: add timestamp
        return "TRANSFER-${System.currentTimeMillis()}"
    }
    
    private fun generate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "TRANSFER-$date-$random"
    }
}
