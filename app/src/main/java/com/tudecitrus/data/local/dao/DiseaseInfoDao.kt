package com.tudecitrus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tudecitrus.data.model.DiseaseInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseInfoDao {
    @Query("SELECT * FROM disease_info ORDER BY disease_name ASC")
    fun getAllDiseases(): Flow<List<DiseaseInfoEntity>>

    @Query("SELECT * FROM disease_info WHERE id = :id LIMIT 1")
    suspend fun getDiseaseById(id: Int): DiseaseInfoEntity?

    @Query(
        """
        SELECT * FROM disease_info
        WHERE disease_name LIKE '%' || :query || '%'
           OR disease_name_id LIKE '%' || :query || '%'
        ORDER BY disease_name ASC
        """
    )
    fun searchDiseases(query: String): Flow<List<DiseaseInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DiseaseInfoEntity>)

    @Query("SELECT COUNT(*) FROM disease_info")
    suspend fun countDiseases(): Int
}
