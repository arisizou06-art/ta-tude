package com.tudecitrus.data.repository

import com.tudecitrus.data.local.dao.DiseaseInfoDao
import com.tudecitrus.data.model.DiseaseInfoEntity
import com.tudecitrus.data.seed.DiseaseSeedData
import kotlinx.coroutines.flow.Flow

class DiseaseRepository(
    private val diseaseInfoDao: DiseaseInfoDao
) {
    fun observeDiseases(): Flow<List<DiseaseInfoEntity>> = diseaseInfoDao.getAllDiseases()

    fun searchDiseases(query: String): Flow<List<DiseaseInfoEntity>> =
        diseaseInfoDao.searchDiseases(query)

    suspend fun getDiseaseById(id: Int): DiseaseInfoEntity? = diseaseInfoDao.getDiseaseById(id)

    suspend fun seedIfEmpty() {
        if (diseaseInfoDao.countDiseases() == 0) {
            diseaseInfoDao.insertAll(DiseaseSeedData.items)
        }
    }
}
