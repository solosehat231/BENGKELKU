package com.example.data.ai

import com.example.data.model.TicketEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AiDiagnosticEngine {

    suspend fun generateMasterMechanicResponse(
        ticket: TicketEntity,
        chatHistory: List<String>,
        lastPrompt: String
    ): DiagnosticAiResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            com.example.BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val detectedSopAlert = detectSmartSopAlert(lastPrompt + " " + ticket.complaint + " " + ticket.dtcCode)

        // Try calling Gemini API if a valid key is provided
        if (apiKey.isNotBlank() && !apiKey.equals("MY_GEMINI_API_KEY", ignoreCase = true)) {
            try {
                val apiResponse = callGeminiRestApi(apiKey, ticket, chatHistory, lastPrompt)
                if (apiResponse.isNotBlank()) {
                    return@withContext DiagnosticAiResult(
                        replyText = apiResponse,
                        sopWarning = detectedSopAlert
                    )
                }
            } catch (e: Exception) {
                // Fallback to offline automotive AI engine
            }
        }

        // Offline automotive intelligence engine
        val localResponse = generateLocalAutomotiveResponse(ticket, lastPrompt)
        DiagnosticAiResult(
            replyText = localResponse,
            sopWarning = detectedSopAlert
        )
    }

    private fun callGeminiRestApi(
        apiKey: String,
        ticket: TicketEntity,
        history: List<String>,
        prompt: String
    ): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 20000

        val systemInstruction = """
            Anda adalah 'Master Mekanik AI' dari sistem bengkel BENGKELKU yang membantu teknisi dan mekanik di 18 cabang.
            Karakteristik jawaban Anda:
            - Berikan jawaban to-the-point, praktis, dan profesional ala mekanik senior.
            - Format jawaban dengan poin-poin (bullet points), tebalkan istilah kunci.
            - Sertakan perkiraan nilai spesifikasi teknis (tahanan Ohm, tegangan Volt, torsi Nm, gap celah busi).
            - Sebutkan alur pengecekan dari yang termudah/termurah dulu sebelum memvonis part mahal.
            - Bahasa Indonesia baku campur istilah teknis bengkel yang lazim (e.g. brebet, pincang, soket, skun, kompresi).
        """.trimIndent()

        val fullPrompt = buildString {
            append("KENDARAAN: ${ticket.vehicleBrand} ${ticket.vehicleModel} (${ticket.year})\n")
            append("PLAT: ${ticket.licensePlate} | KODE DTC: ${ticket.dtcCode.ifBlank { "Tidak ada DTC" }}\n")
            append("KELUHAN AWAL: ${ticket.complaint}\n")
            if (history.isNotEmpty()) {
                append("\nRIWAYAT DISKUSI MEKANIK:\n")
                history.takeLast(4).forEach { append("- $it\n") }
            }
            append("\nPERTANYAAN / UPDATE MEKANIK TERBARU: $prompt\n")
            append("Berikan panduan diagnosis teknis lengkap dalam bentuk bullet points:")
        }

        val jsonRequest = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", fullPrompt)))
                }
            ))
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonRequest.toString())
            writer.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val jsonResponse = JSONObject(responseText)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    fun detectSmartSopAlert(text: String): String? {
        val lower = text.lowercase()
        return when {
            lower.contains("part") || lower.contains("sparepart") || lower.contains("gudang") || lower.contains("ambil") || lower.contains("ganti sensor") || lower.contains("stok") -> {
                "⚠️ SOP Gudang: Wajib cross-checking invoice kasir / SPK aktif sebelum mengambil sparepart dari gudang utama!"
            }
            lower.contains("oli") || lower.contains("kuras") || lower.contains("coolant") || lower.contains("minyak rem") || lower.contains("limbah") -> {
                "⚠️ SOP Limbah B3: Cairan oli bekas/coolant wajib ditampung di drum Semenanjung B3 berizin. Dilarang membuang ke selokan umum!"
            }
            lower.contains("turun mesin") || lower.contains("overhaul") || lower.contains("kolong") || lower.contains("jack") || lower.contains("transmisi turun") -> {
                "⚠️ SOP Keselamatan (K3): Pasang ganjal roda & Jack Stand bersertifikat sebelum masuk kolong mobil. Dilarang bertumpu pada dongkrak hidrolik saja!"
            }
            lower.contains("torsi") || lower.contains("baut") || lower.contains("head") || lower.contains("silinder kop") -> {
                "⚠️ SOP Torsi: Gunakan kunci torsi terkalibrasi. Lakukan pengencangan baut silinder kop secara silang dalam 3 tahapan pengencangan!"
            }
            lower.contains("ecu") || lower.contains("kelistrikan") || lower.contains("kabel") || lower.contains("aki") || lower.contains("short") -> {
                "⚠️ SOP Kelistrikan: Lepas kabel kutub negatif (-) aki dan tunggu 3 menit sebelum melepas atau memasang socket harness ECU."
            }
            else -> null
        }
    }

    private fun generateLocalAutomotiveResponse(ticket: TicketEntity, prompt: String): String {
        val dtc = ticket.dtcCode.trim().uppercase()
        val vehicle = "${ticket.vehicleBrand} ${ticket.vehicleModel}"
        val text = (ticket.complaint + " " + prompt).lowercase()

        return when {
            dtc.contains("P0300") || dtc.contains("P0301") || dtc.contains("P0302") || dtc.contains("P0303") || dtc.contains("P0304") || text.contains("pincang") || text.contains("brebet") || text.contains("misfire") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                Gejala *Misfire / Mesin Pincang* pada **$vehicle**:
                
                • **Langkah 1 (Sistem Pengapian):**
                  - Cek hambatan Ignition Coil primer (0.5 - 0.9 Ω) dan sekunder (10 - 15 kΩ).
                  - Periksa celah busi (standar Iridium: 1.0 - 1.1 mm). Jika elektroda basah oli, periksa seal paking cover valve.
                  - Lakukan tes tukar koil (swap coil cylinder 1 ke cylinder 2) untuk memastikan apakah kode silinder berpindah.
                
                • **Langkah 2 (Injektor Bahan Bakar):**
                  - Ukur resistansi coil injektor pada soket (11.5 - 14.5 Ω).
                  - Dengarkan bunyi 'klik' injektor menggunakan stetoskop mekanik saat starter.
                
                • **Langkah 3 (Kompresi Mesin):**
                  - Jika koil & injektor normal, ukur tekanan kompresi silinder (standar min. 11.5 bar / 165 psi).
                
                💡 *Tips Cabang:* Part Koil & Busi Denso Iridium tersedia di rak Buffer Cabang 4 dan Cabang 2.
                """.trimIndent()
            }
            dtc.contains("P0171") || text.contains("lean") || text.contains("boros") || text.contains("ndut-ndutan") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                DTC **P0171 (System Too Lean - Bank 1)** pada **$vehicle**:
                
                • **Langkah 1 (Pengecekan Kebocoran Vakum):**
                  - Semprotkan cairan karburator cleaner di sekitar intake manifold gasket & selang PCV saat idle.
                  - Jika RPM naik mendadak, ditemukan titik kebocoran udara palsu (*unmetered air*).
                
                • **Langkah 2 (Sensor MAF / MAP):**
                  - Bersihkan kawat sensor MAF dengan MAF cleaner khusus (jangan sentuh dengan jari/kuas).
                  - Nilai output tegangan MAF saat kunci kontak ON: 0.9 - 1.2 Volt, saat Idle: 1.2 - 1.6 Volt.
                
                • **Langkah 3 (Tekanan Fuel Pump):**
                  - Pasang Pressure Gauge pada fuel rail. Standar tekanan fuel line: **3.1 - 3.5 bar (45 - 50 psi)**.
                  - Jika tekanan turun < 2.5 bar saat digas spontan, filter bensin atau fuel pump melemah.
                """.trimIndent()
            }
            dtc.contains("P0420") || text.contains("katalis") || text.contains("catalyst") || text.contains("knalpot") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                DTC **P0420 (Catalyst System Efficiency Below Threshold)** pada **$vehicle**:
                
                • **Langkah 1 (Live Data O2 Sensor Graph):**
                  - Pantau grafik tegangan O2 Sensor 1 (Upstream: berfluktuasi cepat 0.1V - 0.9V).
                  - Pantau grafik O2 Sensor 2 (Downstream: harus stabil di ~0.45V - 0.6V jika catalytic converter berfungsi baik).
                
                • **Langkah 2 (Pengecekan Fisik):**
                  - Periksa apakah ada retakan pada exhaust manifold atau kebocoran paking knalpot sebelum sensor O2.
                  - Cek suhu tabung katalis menggunakan Infrared Thermometer (suhu pipa outlet harus 15-30°C lebih panas dari inlet).
                """.trimIndent()
            }
            dtc.contains("P0700") || text.contains("transmisi") || text.contains("matic") || text.contains("atf") || text.contains("dnyut") || text.contains("slip") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                Diagnosa *Transmisi Otomatis / CVT* pada **$vehicle**:
                
                • **Langkah 1 (Pengecekan Level & Kualitas Oli Matic):**
                  - Posisikan mobil di permukaan rata, mesin suhu kerja (70-80°C), posisi tuas di P/N.
                  - Cek warna oli ATF/CVTF: Jika kecokelatan berbau gosong, plat kopling kopling (*clutch pack*) aus.
                
                • **Langkah 2 (Solenoid Valve Body):**
                  - Lakukan scan modul TCM (Transmission Control Module).
                  - Ukur resistansi Solenoid Shift A & B (standar 11 - 15 Ω).
                
                • **Langkah 3 (Stall Speed Test):**
                  - Lakukan stall test maksimal 5 detik. RPM normal pada stall test: 2.100 - 2.400 RPM.
                """.trimIndent()
            }
            text.contains("rem") || text.contains("abs") || dtc.contains("C1201") || text.contains("getar") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                Diagnosa *Sistem Pengereman & Sensor ABS* pada **$vehicle**:
                
                • **Langkah 1 (Pemeriksaan Rotor Disc & Kampas):**
                  - Ukur ketebalan disc brake dengan micrometer (batas toleransi runout maks. 0.05 mm dengan dial gauge).
                  - Periksa ketebalan brake pad (min. 3 mm). Jika aus tidak rata, bersihkan dan lumasi sliding pin kaliper.
                
                • **Langkah 2 (Sensor Speed ABS):**
                  - Lepas sensor kecepatan di knuckle roda, bersihkan ujung magnetik dari serbuk gram besi.
                  - Nilai resistansi sensor ABS koil pasif: **1.000 - 1.600 Ω**.
                """.trimIndent()
            }
            text.contains("ac") || text.contains("freon") || text.contains("panas") || text.contains("kompresor") -> {
                """
                🔧 **Hasil Analisis Diagnostik Master Mekanik:**
                Diagnosa *Sistem Pendingin AC Mobil* pada **$vehicle**:
                
                • **Langkah 1 (Pemeriksaan Manifold Gauge):**
                  - Low Pressure (Sisi Rendah): Standar **25 - 35 psi**.
                  - High Pressure (Sisi Tinggi): Standar **150 - 220 psi** (pada 1.500 RPM).
                
                • **Langkah 2 (Extra Fan & Magnetic Clutch):**
                  - Pastikan motor extra fan berputar kencang pada high-speed saat AC ON.
                  - Cek celah magnetic clutch kompresor (standar 0.35 - 0.60 mm).
                """.trimIndent()
            }
            else -> {
                """
                🔧 **Hasil Panduan Master Mekanik AI:**
                Untuk unit **$vehicle** dengan keluhan: *${ticket.complaint}*:
                
                • **Langkah 1 (Pemeriksaan Visual & Scanner):**
                  - Sambungkan scanner OBD-2 ke port DLC3 di bawah dashboard.
                  - Catat data *Freeze Frame* untuk melihat kondisi putaran RPM, suhu ECT, dan Short Term Fuel Trim (STFT) saat kendala terekam.
                
                • **Langkah 2 (Pengecekan Suplai Tegangan):**
                  - Ukur voltase baterai saat idle: **13.8 - 14.4 Volt**. Pastikan alternator tidak overcharge atau undercharge.
                  - Periksa ground kabel massa mesin ke bodi sasis (tegangan drop harus < 0.2 Volt).
                
                • **Langkah 3 (Diskusi Antar Cabang):**
                  - Mekanik Cabang 1 dan Cabang 7 pernah menyelesaikan kasus mirip pada model ini dengan membersihkan Throttle Body dan kalibrasi Idle Learning.
                """.trimIndent()
            }
        }
    }
}

data class DiagnosticAiResult(
    val replyText: String,
    val sopWarning: String?
)
