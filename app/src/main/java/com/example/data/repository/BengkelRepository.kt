package com.example.data.repository

import com.example.data.ai.AiDiagnosticEngine
import com.example.data.local.ChatMessageDao
import com.example.data.local.TicketDao
import com.example.data.model.Branch
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MechanicProfile
import com.example.data.model.SopDocument
import com.example.data.model.SopStep
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BengkelRepository(
    private val ticketDao: TicketDao,
    private val chatMessageDao: ChatMessageDao,
    private val aiEngine: AiDiagnosticEngine = AiDiagnosticEngine()
) {
    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllTickets()
    val openTickets: Flow<List<TicketEntity>> = ticketDao.getOpenTickets()
    val resolvedTickets: Flow<List<TicketEntity>> = ticketDao.getResolvedTickets()

    fun observeTicket(id: Long): Flow<TicketEntity?> = ticketDao.observeTicketById(id)
    fun observeMessages(ticketId: Long): Flow<List<ChatMessageEntity>> = chatMessageDao.getMessagesForTicket(ticketId)

    suspend fun createTicket(ticket: TicketEntity): Long {
        val id = ticketDao.insertTicket(ticket)
        // Add initial system & AI welcome message
        val welcomeAi = ChatMessageEntity(
            ticketId = id,
            senderType = "AI_MASTER",
            senderName = "Master Mekanik AI",
            message = """
                👋 Halo ${ticket.mechanicName}! Tiket kendala **${ticket.ticketNumber}** berhasil dibuka untuk unit **${ticket.vehicleBrand} ${ticket.vehicleModel}**.
                
                Tekan tombol petir **'Tanya AI'** di atas kolom ketik untuk mendapatkan panduan diagnosa instan atau diskusikan bersama mekanik dari 18 cabang lainnya.
            """.trimIndent(),
            sopWarning = aiEngine.detectSmartSopAlert(ticket.complaint + " " + ticket.dtcCode)
        )
        chatMessageDao.insertMessage(welcomeAi)
        return id
    }

    suspend fun sendMechanicMessage(
        ticketId: Long,
        senderName: String,
        branchName: String,
        message: String,
        isCurrentUser: Boolean = true
    ) {
        val smartAlert = aiEngine.detectSmartSopAlert(message)
        val entity = ChatMessageEntity(
            ticketId = ticketId,
            senderType = if (isCurrentUser) "MECHANIC" else "OTHER_MECHANIC",
            senderName = senderName,
            branchName = branchName,
            message = message,
            sopWarning = smartAlert
        )
        chatMessageDao.insertMessage(entity)
        ticketDao.incrementRepliesCount(ticketId)
    }

    suspend fun triggerAiResponse(ticketId: Long, userPrompt: String = "") {
        val ticket = ticketDao.getTicketById(ticketId) ?: return
        val currentMessages = chatMessageDao.getMessagesForTicket(ticketId).first()
        val historyList = currentMessages.takeLast(6).map { "${it.senderName}: ${it.message}" }

        val aiResult = aiEngine.generateMasterMechanicResponse(
            ticket = ticket,
            chatHistory = historyList,
            lastPrompt = userPrompt.ifBlank { "Tolong berikan panduan diagnosa dan troubleshooting untuk kendala ini." }
        )

        val aiMessage = ChatMessageEntity(
            ticketId = ticketId,
            senderType = "AI_MASTER",
            senderName = "Master Mekanik AI",
            message = aiResult.replyText,
            sopWarning = aiResult.sopWarning
        )
        chatMessageDao.insertMessage(aiMessage)
        ticketDao.incrementRepliesCount(ticketId)
    }

    suspend fun markTicketResolved(ticketId: Long, finalSolution: String) {
        ticketDao.markResolved(ticketId, finalSolution)
        val closeMessage = ChatMessageEntity(
            ticketId = ticketId,
            senderType = "AI_MASTER",
            senderName = "Master Mekanik AI",
            message = "✅ **Tiket Ditandai Selesai!**\nSolusi terverifikasi: *$finalSolution*\n\nData solusi ini telah disimpan ke sistem pengetahuan 'Cari Solusi Cepat' untuk referensi mekanik 18 cabang lainnya."
        )
        chatMessageDao.insertMessage(closeMessage)
    }

    fun searchTickets(query: String): Flow<List<TicketEntity>> {
        return ticketDao.searchTickets(query)
    }

    fun get18Branches(): List<Branch> = listOf(
        Branch(1, "Cabang 1 - Montecarlo Solo", "Surakarta", "Jl. Dr. Supomo No. 6A Surakarta", "62 821-4012-8796", "Aris Suhartanto (manager)", 2),
        Branch(2, "Cabang 2 - Mega Merapi", "Kartasura", "Jl. Yogya No. 180 Kartasura", "62 889-8313-0125", "Aulia Prawira Negara (manager)", 3),
        Branch(3, "Cabang 3 - Montecarlo Sukoharjo", "Sukoharjo", "Jl. Raya Grogol, Telukan, Sukoharjo", "62 812-1582-5220", "Muhammad Wildan (manager)", 1),
        Branch(4, "Cabang 4 - Montecarlo Salatiga", "Salatiga", "Jl. Diponegoro No. 168 Salatiga", "62 815-4894-0133", "Gustin (Kormin)", 2),
        Branch(5, "Cabang 5 - MOntecarlo Semarang", "Semarang", "Jl. Majapahit No. 297 Semarang", "62 815-7802-0251", "Berliyansyah (manager)", 1),
        Branch(6, "Cabang 6 - Montecarlo Klaten", "Klaten", "Jl. Yogya - Solo Km 2 Jonggrangan, Klaten", "62 812-2839-1860", "Suyatno (manager)", 2),
        Branch(7, "Cabang 7 - Otoclinic", "Palur", "JJl. Raya Solo - Tawangwangu Km 7 Palur, Karanganyar", "62 895-3935-85923", "Supomo (manager)", 0),
        Branch(8, "Cabang 8 - Semeru Motor", "Magelang", "Jl. Urip Sumoharjo No. 118 Magelang", "62 823-2486-3515", "Aris Sriyanto (manager)", 1),
        Branch(9, "Cabang 9 - Montecarlo Boyolali", "Boyolali", "Jl Raya Solo - Semarang km 20, Teras, Boyolali", "62 852-8616-1763", "Lugut (Kepala Mekanik)", 1),
        Branch(10, "Cabang 10 - Montecarlo Madiun", "Madiun", "Jl. Ponorogo No.14 Kel. Demangan, Kec. Taman, Kota Madiun", "62 821-4295-9950", "Randy (manager)r", 0),
        Branch(11, "Cabang 11 - Montecarlo Express", "Pabelan", "Halaman parkir assalam hypermarket, Pabalen,Kartasura", "0", "Aulia Prawira Negara (manager)", 2),
        Branch(12, "Cabang 12 - Montecalo Palagan", "Yogyakarta", "Jl  Palagan  Tentara  Pelajar, Sleman", "0822 2682 5338", "Catur (Kepala Mekanik)", 1),
        Branch(13, "Cabang 13 - Graha Service Engginering", "Slawi", "Jl. Jendral Sudirman No.17,Kec Slawi Kab Tegal", "0823-2477-6419", "Teguh (manager)", 1),
        Branch(14, "Cabang 14 - Otoclinic Sinergi", "Papahan", "Jl. Raya Solo-Tawangmangu Km. 23, Papahan,Karanganyar", "0822 4182 4326", "Aris Wahyu (kepala mekanik)", 0),
        Branch(15, "Cabang 15 - Montecarlo Berbah", "Yogyakarta", ", Jl nasional 3 no 16  tegaltirto, berbah, sleman", "62 821-4758-6390", "Wisnu (kepala mekanik)", 2),
        Branch(16, "Cabang 16 - Montecarlo Sragen", "Sragen", "Jl. Sukowati No.15, Kebayan 2, Jetak, Sragen", "62 821-3590-2573", "Anton (Kepala mekanik)", 1),
        Branch(17, "Cabang 17 - Montecarlo Lampung", "Lampung", "Jl. Sultan Agung No. 21 Bandar Lampung", "(0721) 770475", "0", 0),
        Branch(18, "Cabang 18 - Montecarlo Jakarta", "Jakarta", "Jl Pangeran tubagus angke No 30,Kec.Grogol Petamburan,Jakarta Barat", "085655559325", "0", 1)
    )

    fun getBranchById(id: Int): Branch = get18Branches().find { it.id == id } ?: get18Branches()[3]

    fun getSopLibrary(): List<SopDocument> = listOf(
        SopDocument(
            id = "sop-01",
            category = "Alur Part & Gudang",
            title = "Flowchart Prosedur Pengeluaran Sparepart & Retur",
            code = "SOP-GUD-01",
            summary = "Prosedur wajib sebelum mengambil part dari rak buffer / gudang sentral untuk mencegah selisih stok.",
            iconName = "inventory",
            isFeatured = true,
            steps = listOf(
                SopStep(1, "Pemeriksaan SPK / Work Order", "Mekanik wajib membawa printout / digital SPK yang sudah ditandatangani Service Advisor.", "Jangan ambil part tanpa nomor SPK terdaftar!"),
                SopStep(2, "Cross-check Kasir & Approval Sistem", "Pastikan part sudah di-input kasir dan status 'Approved' di sistem kasir.", "Verifikasi kode part di kotak part vs barcode fisik."),
                SopStep(3, "Scan Barcode Part di Meja Petugas Gudang", "Petugas gudang memindai serial number & mencatat pengeluaran unit."),
                SopStep(4, "Penyimpanan Part Bekas (Old Part Box)", "Part lama wajib dimasukkan ke plastik bening bersegel untuk bukti konsumen saat serah terima unit.")
            ),
            relatedParts = listOf("Semua Fast & Slow Moving Parts", "Oli & Fluida", "Busi Iridium & Filter")
        ),
        SopDocument(
            id = "sop-02",
            category = "K3 & Lingkungan",
            title = "Pengelolaan & Penampungan Limbah B3 Bengkel",
            code = "SOP-K3-04",
            summary = "Prosedur penanganan oli bekas, aki drop, filter oli, majun berminyak, dan cairan pendingin radiator.",
            iconName = "delete_sweep",
            isFeatured = true,
            steps = listOf(
                SopStep(1, "Penampungan Cairan Menggunakan Oil Catcher", "Gunakan bak penampung portable dengan corong anti tumpah tepat di bawah karter oli/radiator."),
                SopStep(2, "Pemindahan ke Drum Semenanjung B3", "Pompa oli bekas ke dalam drum berkapasitas 200L di area beratap dan bertanggul Semenanjung Limbah B3.", "Dilarang keras membuang tetesan oli ke selokan atau tanah!"),
                SopStep(3, "Pemisahan Majun & Filter Oli", "Tempatkan filter oli bekas pada drum khusus penirisan minimal 24 jam sebelum dimasukkan ke karung B3."),
                SopStep(4, "Pencatatan Logbook B3 Harian", "Mekanik piket wajib mencatat volume liter oli bekas yang masuk pada buku mutasi B3.")
            )
        ),
        SopDocument(
            id = "sop-03",
            category = "Standar Mekanikal & Torsi",
            title = "Spesifikasi & Alur Pengencangan Baut Mesin (Torsi)",
            code = "SOP-ENG-08",
            summary = "Tabel standar torsi pengencangan silinder kop, roda, busi, dan baut karter oli mesin.",
            iconName = "build",
            isFeatured = true,
            steps = listOf(
                SopStep(1, "Pembersihan Drat & Ulir Baut", "Pastikan lubang drat blok mesin bebas dari sisa oli, kerak karbon, atau cairan pendingin."),
                SopStep(2, "Pelumasan Tipis Ulir Baut", "Oleskan tipis oli mesin bersih pada ulir baut head (jangan berlebihan)."),
                SopStep(3, "Pengencangan Silang Bertahap (3 Step)", "Tahap 1: 30 Nm, Tahap 2: 60 Nm, Tahap 3: Putaran sudut 90° + 90° (Tipe TTY Bolt).", "Gunakan kunci torsi terkalibrasi!")
            ),
            torqueSpecs = listOf(
                "Baut Silinder Kop (1NR/2NR Avanza/Calya): Step 1 = 32 Nm, Step 2 = 90°, Step 3 = 90°",
                "Baut Silinder Kop (2GD Innova Diesel): Step 1 = 45 Nm, Step 2 = 90 Nm, Step 3 = 90° + 90°",
                "Baut Roda Velg Standar: 103 - 108 Nm (Kunci 19/21)",
                "Baut Busi (Drat 14mm): 18 - 22 Nm",
                "Baut Baut Pembuangan Oli (Drain Plug): 35 - 40 Nm (Ganti ring gasket baru)"
            )
        ),
        SopDocument(
            id = "sop-04",
            category = "Kelistrikan & Diagnostik",
            title = "Prosedur Diagnosa Kelistrikan, ECU & Scan DTC",
            code = "SOP-ELE-02",
            summary = "Panduan pengukuran multimeter, kalibrasi sensor, reset idle learning, dan penelusuran jalur wiring.",
            iconName = "electric_bolt",
            steps = listOf(
                SopStep(1, "Pemasangan Memory Saver / Baterai Stabilizer", "Pastikan tegangan suplai ECU stabil di atas 12.4 Volt sebelum scanning atau reflashing."),
                SopStep(2, "Penyambungan OBD-2 Scanner", "Colok kabel OBD-2 saat kontak OFF, lalu putar kunci ke kontak ON (mesin mati)."),
                SopStep(3, "Pencatatan Freeze Frame Data", "Screenshoot atau catat data STFT, LTFT, ECT, dan RPM saat DTC trigger."),
                SopStep(4, "Pemeriksaan Kontinuitas Jalur (Back-probing)", "Gunakan pin jarum tipis pada bagian belakang socket. Jangan merusak karet seal waterproof socket!")
            )
        ),
        SopDocument(
            id = "sop-05",
            category = "5R & Area Kerja",
            title = "Standar Kebersihan 5R & Tool Keberangkatan Bengkel",
            code = "SOP-5R-01",
            summary = "Ringkas, Rapi, Resik, Rawat, Rajin: Penataan caddy tool trolley, fender cover, dan kebersihan stall.",
            iconName = "cleaning_services",
            isFeatured = true,
            steps = listOf(
                SopStep(1, "Pemasangan Sarung Pelindung (Protective Cover)", "Wajib pasang Fender Cover, Grill Cover, Seat Cover, dan Steering Wheel Cover sebelum mulai kerja."),
                SopStep(2, "Penempatan Tool Trolley", "Alat diletakkan di baki shadow board. Tidak boleh ada kunci yang ditaruh di atas cover valve atau aki mobil."),
                SopStep(3, "Pembersihan Tumpahan Oli Segera", "Jika ada ceceran oli di lantai, segera taburkan serbuk gergaji / floor cleaner agar tidak licin."),
                SopStep(4, "Pengecekan Akhir Hand Tools (Closing Shift)", "Pastikan semua kunci sok, rachet, dan obeng kembali lengkap ke kotaknya sebelum pulang.")
            )
        )
    )

    suspend fun populateInitialDataIfEmpty() {
        val count = ticketDao.getTicketById(1)
        if (count == null) {
            val initialTickets = listOf(
                TicketEntity(
                    id = 1,
                    ticketNumber = "#TKT-0841",
                    branchId = 2,
                    branchName = "Cabang 2 - Semarang Majapahit",
                    mechanicName = "Joko Widodo (Senior)",
                    vehicleBrand = "Toyota",
                    vehicleModel = "Innova Reborn 2.4 Diesel A/T",
                    licensePlate = "H 9182 QP",
                    year = "2020",
                    dtcCode = "P0087",
                    complaint = "Mesin mati mendadak saat akselerasi kickdown di jalan tol, indikator check engine menyala. Saat distarter ulang agak panjang.",
                    urgency = TicketUrgency.EMERGENCY_MOGOK,
                    category = "Mesin / Diesel Commonrail",
                    status = TicketStatus.DISCUSSING,
                    repliesCount = 3,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 45
                ),
                TicketEntity(
                    id = 2,
                    ticketNumber = "#TKT-0842",
                    branchId = 4,
                    branchName = "Cabang 4 - Solo Slamet Riyadi",
                    mechanicName = "Budi Santoso (Saya)",
                    vehicleBrand = "Honda",
                    vehicleModel = "Brio RS 1.2 CVT",
                    licensePlate = "AD 4821 QA",
                    year = "2021",
                    dtcCode = "P0300 / P0303",
                    complaint = "Mesin bergetar keras (pincang) saat RPM 1500 - 2500, konsumsi bensin terasa boros. Busi silinder 3 agak basah hitam.",
                    urgency = TicketUrgency.URGENT,
                    category = "Mesin & Pengapian",
                    status = TicketStatus.OPEN,
                    repliesCount = 2,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 20
                ),
                TicketEntity(
                    id = 3,
                    ticketNumber = "#TKT-0839",
                    branchId = 5,
                    branchName = "Cabang 5 - Yogyakarta Gejayan",
                    mechanicName = "Ahmad Fauzi",
                    vehicleBrand = "Mitsubishi",
                    vehicleModel = "Pajero Sport Dakar 4x2",
                    licensePlate = "AB 1290 XY",
                    year = "2018",
                    dtcCode = "P0420",
                    complaint = "Lampu check engine nyala setelah ganti knalpot downpipe, tarikan atas terasa agak tertahan.",
                    urgency = TicketUrgency.NORMAL,
                    category = "Knalpot & Sensor",
                    status = TicketStatus.RESOLVED,
                    solutionSummary = "O2 sensor downstream tertutup jelaga jelaga karbon & paking knalpot bocor halus. Ganti paking knalpot baru dan pasang O2 spacer simulator, DTC terhapus permanen.",
                    repliesCount = 4,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 18,
                    resolvedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                ),
                TicketEntity(
                    id = 4,
                    ticketNumber = "#TKT-0840",
                    branchId = 1,
                    branchName = "Cabang 1 - Semarang Pemuda",
                    mechanicName = "Slamet Raharjo",
                    vehicleBrand = "Suzuki",
                    vehicleModel = "Ertiga Dreza 1.4 A/T",
                    licensePlate = "H 8832 CZ",
                    year = "2017",
                    dtcCode = "P0700 / P0705",
                    complaint = "Transmisi matic menyentak keras saat pindah tuas dari N ke D, indikator O/D kadang berkedip.",
                    urgency = TicketUrgency.URGENT,
                    category = "Transmisi / Matic",
                    status = TicketStatus.DISCUSSING,
                    repliesCount = 5,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 90
                ),
                TicketEntity(
                    id = 5,
                    ticketNumber = "#TKT-0835",
                    branchId = 8,
                    branchName = "Cabang 8 - Boyolali Pandanaran",
                    mechanicName = "Rudi Hartono",
                    vehicleBrand = "Toyota",
                    vehicleModel = "Avanza 1.3 G Grand New",
                    licensePlate = "AD 9031 BD",
                    year = "2019",
                    dtcCode = "C1201",
                    complaint = "Lampu rem & ABS menyala bersamaan setelah cuci steam kolong mobil.",
                    urgency = TicketUrgency.NORMAL,
                    category = "Kaki-Kaki & Rem",
                    status = TicketStatus.RESOLVED,
                    solutionSummary = "Soket sensor speed ABS roda kiri depan kemasukan air bertekanan. Dikeringkan dengan contact cleaner & diberi silicone grease waterproof, indikator ABS normal kembali.",
                    repliesCount = 3,
                    createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 26,
                    resolvedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 12
                )
            )
            ticketDao.insertAllTickets(initialTickets)

            // Seed initial chat messages for Ticket 1 and 2
            val messages1 = listOf(
                ChatMessageEntity(
                    ticketId = 1,
                    senderType = "AI_MASTER",
                    senderName = "Master Mekanik AI",
                    message = "👋 Selamat pagi Cabang 2! Tiket #TKT-0841 untuk Innova Diesel 2GD-FTV DTC P0087 (Fuel Rail/System Pressure - Too Low) telah aktif.",
                    sopWarning = "⚠️ SOP Gudang: Wajib cross-checking invoice kasir sebelum mengambil sparepart Suction Control Valve (SCV) dari gudang!"
                ),
                ChatMessageEntity(
                    ticketId = 1,
                    senderType = "MECHANIC",
                    senderName = "Joko Widodo (Senior)",
                    branchName = "Cabang 2 - Semarang Majapahit",
                    message = "Izin lapor rekan-rekan, filter solar bawah & atas sudah diganti baru orisinil, tapi tekanan rail saat akselerasi masih drop di 25 MPa (standar harus 135+ MPa). Ada saran?"
                ),
                ChatMessageEntity(
                    ticketId = 1,
                    senderType = "OTHER_MECHANIC",
                    senderName = "Hendra (Cabang 3 Solo)",
                    branchName = "Cabang 3 - Solo Veteran",
                    message = "Coba cek Suction Control Valve (SCV) di Supply Pump mas Joko. Seringkali plunger SCV macet karena solar subsidi kotor. Di Cabang 3 kemarin ada stok SCV long type."
                )
            )
            chatMessageDao.insertAllMessages(messages1)

            val messages2 = listOf(
                ChatMessageEntity(
                    ticketId = 2,
                    senderType = "AI_MASTER",
                    senderName = "Master Mekanik AI",
                    message = """
                        👋 Halo Budi! Panduan diagnosa awal untuk Brio RS (P0300/P0303 Misfire):
                        
                        • **Pengecekan Koil & Busi:**
                          - Tukar ignition coil silinder 3 ke silinder 1.
                          - Ukur celah busi silinder 3 (standar 1.1 mm).
                        • **Pengecekan Injektor Silinder 3:**
                          - Cek hambatan injektor (12.2 - 14.5 Ω).
                        • **Uji Kompresi Kering & Basah:**
                          - Tekanan kompresi silinder minimal 11.0 bar.
                    """.trimIndent(),
                    sopWarning = "⚠️ SOP Torsi: Kencangkan busi dengan kunci torsi pada 18 - 22 Nm. Jangan mengencangkan berlebihan agar tidak merusak ulir head silinder aluminium!"
                ),
                ChatMessageEntity(
                    ticketId = 2,
                    senderType = "MECHANIC",
                    senderName = "Budi Santoso",
                    branchName = "Cabang 4 - Solo Slamet Riyadi",
                    message = "Siap Master, sudah tukar koil 3 ke 1, ternyata DTC pindah ke P0301! Positif koil nomor 3 bocor isolatornya. Mau minta part koil baru ke gudang."
                )
            )
            chatMessageDao.insertAllMessages(messages2)
        }
    }
}
