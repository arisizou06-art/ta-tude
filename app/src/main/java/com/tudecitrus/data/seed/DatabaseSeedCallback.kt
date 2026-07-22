package com.tudecitrus.data.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseSeedCallback {
    fun create(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDiseases(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Jaring pengaman: kalau jumlah baris tidak sesuai (mis. tabel kosong
                // setelah destructive migration yang TIDAK memicu onCreate, atau DB lama
                // yang cuma punya 4 entri), isi ulang data penyakit. Idempoten & murah:
                // di-skip otomatis kalau jumlahnya sudah benar.
                val expected = DiseaseSeedData.items.size
                val count = db.query("SELECT COUNT(*) FROM disease_info").use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) else 0
                }
                if (count != expected) {
                    seedDiseases(db)
                }
            }
        }
    }

    private fun seedDiseases(db: SupportSQLiteDatabase) {
        DiseaseSeedData.items.forEach { item ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO disease_info (
                    id,
                    disease_name,
                    disease_name_id,
                    description,
                    symptoms,
                    causes,
                    treatment,
                    prevention,
                    severity_level
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(
                    item.id,
                    item.diseaseName,
                    item.diseaseNameId,
                    item.description,
                    item.symptoms,
                    item.causes,
                    item.treatment,
                    item.prevention,
                    item.severityLevel
                )
            )
        }
    }
}
