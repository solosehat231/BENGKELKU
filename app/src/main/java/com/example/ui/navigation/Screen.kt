package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Login Mekanik")
    data object Dashboard : Screen("dashboard", "Beranda")
    data object Forum : Screen("forum", "Forum Tiket")
    data object SopLibrary : Screen("sop_library", "Buku SOP")
    data object ProfileSchedule : Screen("profile_schedule", "Profil Mekanik")
    data object CreateTicket : Screen("create_ticket", "Buat Kendala")
    data object TicketDetail : Screen("ticket_detail/{ticketId}", "Ruang Diskusi") {
        fun createRoute(ticketId: Long) = "ticket_detail/$ticketId"
    }
}
