package com.example.database

import kotlinx.coroutines.flow.Flow

class PrescriptionRepository(private val prescriptionDao: PrescriptionDao) {
    val allPrescriptions: Flow<List<PrescriptionEntity>> = prescriptionDao.getAllPrescriptions()

    suspend fun insert(prescription: PrescriptionEntity): Long {
        return prescriptionDao.insertPrescription(prescription)
    }

    suspend fun deleteById(id: Int) {
        prescriptionDao.deletePrescriptionById(id)
    }

    suspend fun clearAll() {
        prescriptionDao.clearAll()
    }
}
