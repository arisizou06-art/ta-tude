package com.tudecitrus.data.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseSeedCallback {
    fun create(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDiseases(db)
                seedAppSettings(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Jaring pengaman: kalau jumlah baris tidak sesuai (mis. tabel kosong
                // setelah destructive migration yang TIDAK memicu onCreate, atau DB lama
                // yang cuma punya 4 entri), isi ulang data penyakit. Idempoten & murah:
                // di-skip otomatis kalau jumlahnya sudah benar.
                val expected = DiseaseSeedData.items.size
                val count = countRows(db, "disease_info")
                if (count != expected) {
                    seedDiseases(db)
                }

                // Jaring pengaman yang sama untuk konfigurasi aplikasi, agar tabel
                // app_settings ikut terisi pada pemasangan yang sudah ada (onCreate
                // hanya berjalan saat database pertama kali dibuat).
                if (countRows(db, "app_settings") != AppSettingsSeedData.items.size) {
                    seedAppSettings(db)
                }
            }
        }
    }

    private fun countRows(db: SupportSQLiteDatabase, table: String): Int {
        return db.query("SELECT COUNT(*) FROM $table").use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun seedAppSettings(db: SupportSQLiteDatabase) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        AppSettingsSeedData.items.forEach { item ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO app_settings (
                    id,
                    setting_key,
                    setting_value,
                    description,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(
                    item.id,
                    item.key,
                    item.value,
                    item.description,
                    now
                )
            )
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
