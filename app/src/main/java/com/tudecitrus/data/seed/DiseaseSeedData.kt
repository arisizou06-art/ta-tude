package com.tudecitrus.data.seed

import com.tudecitrus.data.model.DiseaseInfoEntity

/**
 * Data awal penyakit. ID HARUS konsisten dengan pemetaan di
 * LocalAIModelService.diseaseNameToId():
 *   1=canker, 2=greening, 3=blackspot, 4=melanose, 5=healthy
 * (Sebelumnya hanya 4 entri & healthy salah di id=4 → menyebabkan
 *  "Data penyakit tidak ditemukan" saat model memprediksi healthy/melanose.)
 */
object DiseaseSeedData {
    val items: List<DiseaseInfoEntity> = listOf(
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
}
