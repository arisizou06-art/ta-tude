package com.tudecitrus.feature.info.data

import com.tudecitrus.data.local.dao.DiseaseInfoDao
import com.tudecitrus.data.model.DiseaseInfoEntity
import kotlinx.coroutines.flow.Flow

interface InfoRepository {
    fun observeDiseases(query: String): Flow<List<DiseaseInfoEntity>>
    suspend fun getDiseaseById(id: Int): DiseaseInfoEntity?
    suspend fun seedIfEmpty()
}

class DefaultInfoRepository(
    private val diseaseInfoDao: DiseaseInfoDao
) : InfoRepository {

    override fun observeDiseases(query: String): Flow<List<DiseaseInfoEntity>> {
        return if (query.isBlank()) {
            diseaseInfoDao.getAllDiseases()
        } else {
            diseaseInfoDao.searchDiseases(query.trim())
        }
    }

    override suspend fun getDiseaseById(id: Int): DiseaseInfoEntity? {
        return diseaseInfoDao.getDiseaseById(id)
    }

    override suspend fun seedIfEmpty() {
        // Re-seed jika jumlah entry di DB tidak sama dengan jumlah seed terbaru.
        // Ini memastikan entry baru (mis. "Daun Sehat" id=5) ke-insert ke DB lama
        // yang sebelumnya cuma punya 4 entry penyakit.
        // DAO pakai OnConflictStrategy.REPLACE jadi entry existing aman ter-update.
        val expected = defaultDiseaseSeedData.size
        if (diseaseInfoDao.countDiseases() == expected) {
            return
        }
        diseaseInfoDao.insertAll(defaultDiseaseSeedData)
    }
}

private val defaultDiseaseSeedData = listOf(
    DiseaseInfoEntity(
        id = 1,
        diseaseName = "Citrus Canker",
        diseaseNameId = "citrus_canker",
        description = "Penyakit bakteri yang menimbulkan lesi pada daun, batang, dan buah jeruk.",
        symptoms = "Bercak kecil menonjol pada daun, halo kekuningan, dan gugur daun dini.",
        causes = "Infeksi bakteri Xanthomonas citri yang menyebar melalui percikan air dan angin.",
        treatment = "Pangkas bagian terinfeksi, gunakan bakterisida berbasis tembaga, dan sanitasi kebun.",
        prevention = "Gunakan bibit sehat, kontrol kelembapan kebun, dan disinfeksi alat secara berkala.",
        severityLevel = "Sedang"
    ),
    DiseaseInfoEntity(
        id = 2,
        diseaseName = "Greening (HLB)",
        diseaseNameId = "greening_hlb",
        description = "Penyakit sistemik serius pada jeruk yang mengganggu distribusi nutrisi tanaman.",
        symptoms = "Daun belang tidak simetris, buah kecil tidak seragam, dan rasa buah pahit.",
        causes = "Bakteri Candidatus Liberibacter spp. yang ditularkan oleh kutu psyllid.",
        treatment = "Eliminasi tanaman yang terinfeksi berat dan lakukan pengendalian vektor secara terpadu.",
        prevention = "Monitoring rutin psyllid, pemakaian benih bersertifikat, dan karantina tanaman baru.",
        severityLevel = "Tinggi"
    ),
    DiseaseInfoEntity(
        id = 3,
        diseaseName = "Citrus Black Spot",
        diseaseNameId = "citrus_black_spot",
        description = "Penyakit jamur yang memicu bercak hitam pada daun dan buah jeruk.",
        symptoms = "Muncul bintik hitam melingkar pada buah dan daun disertai perubahan warna jaringan.",
        causes = "Jamur Phyllosticta citricarpa yang berkembang pada kelembapan tinggi.",
        treatment = "Aplikasi fungisida terjadwal, pemangkasan tajuk, dan pembersihan daun gugur.",
        prevention = "Perbaiki sirkulasi udara, kurangi kelembapan berlebih, dan lakukan sanitasi lahan.",
        severityLevel = "Sedang"
    ),
    DiseaseInfoEntity(
        id = 4,
        diseaseName = "Melanosis",
        diseaseNameId = "melanose",
        description = "Penyakit jamur yang menyebabkan bercak kasar kecil pada permukaan daun dan buah.",
        symptoms = "Bercak coklat kehitaman bertekstur kasar dan pola titik menyebar.",
        causes = "Infeksi jamur Diaporthe citri dari ranting mati atau jaringan tanaman lemah.",
        treatment = "Buang ranting mati, lakukan pemangkasan sanitasi, dan aplikasikan fungisida sesuai dosis.",
        prevention = "Jaga kebersihan kebun, kurangi stres tanaman, dan hindari kelembapan berlebih.",
        severityLevel = "Rendah"
    ),
    DiseaseInfoEntity(
        id = 5,
        diseaseName = "Daun Sehat",
        diseaseNameId = "healthy",
        description = "Daun jeruk dalam kondisi sehat tanpa indikasi penyakit. Warna hijau merata dan permukaan halus.",
        symptoms = "Tidak ada gejala penyakit. Daun berwarna hijau segar, bentuk simetris, dan permukaan bersih dari bercak.",
        causes = "Kondisi sehat dihasilkan dari praktik budidaya yang baik dan lingkungan yang mendukung.",
        treatment = "Tidak diperlukan tindakan kuratif karena daun dalam kondisi sehat.",
        prevention = "Pertahankan praktik budidaya yang baik: penyiraman teratur, pemupukan seimbang, sanitasi kebun, monitoring rutin, dan pengendalian hama secara terpadu.",
        severityLevel = "Rendah"
    )
)
