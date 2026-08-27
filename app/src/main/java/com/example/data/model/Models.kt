package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TicketUrgency(val label: String, val colorHex: Long) {
    NORMAL("Normal", 0xFF0284C7),
    URGENT("Urgent", 0xFFD97706),
    EMERGENCY_MOGOK("Mogok / Darurat", 0xFFDC2626)
}

enum class TicketStatus(val label: String) {
    OPEN("Terbuka"),
    DISCUSSING("Sedang Diskusi"),
    RESOLVED("Selesai")
}

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketNumber: String,
    val branchId: Int,
    val branchName: String,
    val mechanicName: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val licensePlate: String,
    val year: String,
    val dtcCode: String,
    val complaint: String,
    val urgency: TicketUrgency = TicketUrgency.NORMAL,
    val category: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val solutionSummary: String = "",
    val photoUri: String? = null,
    val audioDurationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val repliesCount: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: Long,
    val senderType: String, // "MECHANIC", "OTHER_MECHANIC", "AI_MASTER"
    val senderName: String,
    val branchName: String = "",
    val message: String,
    val sopWarning: String? = null, // Smart SOP alert (Cross-check part, Limbah B3, Torsi dll.)
    val timestamp: Long = System.currentTimeMillis()
)

data class SopStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val warningNote: String? = null
) {
    val instruction: String get() = title
}

data class SopDocument(
    val id: String,
    val category: String,
    val title: String,
    val code: String,
    val summary: String,
    val iconName: String,
    val isFeatured: Boolean = false,
    val steps: List<SopStep> = emptyList(),
    val relatedParts: List<String> = emptyList(),
    val torqueSpecs: List<String> = emptyList()
)

data class Branch(
    val id: Int,
    val name: String,
    val city: String,
    val address: String,
    val phone: String,
    val headMechanic: String,
    val activeTicketsCount: Int = 0
) {
    val pic: String get() = headMechanic
}

enum class UserRole(val label: String, val badgeColorHex: Long) {
    MECHANIC("Mekanik Cabang", 0xFF0284C7),
    ADMIN_OWNER("Admin / Owner Bengkel", 0xFFD97706)
}

data class MechanicProfile(
    val name: String,
    val branchId: Int,
    val branchName: String,
    val role: UserRole = UserRole.MECHANIC,
    val phone: String = "0812-3456-7890",
    val solvedTicketsCount: Int = 24,
    val sharedSolutionsCount: Int = 18,
    val totalBonusEarned: Long = 350000L
)

@Entity(tableName = "solution_posts")
data class SolutionPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val year: String,
    val dtcCode: String,
    val category: String,
    val symptomDescription: String,
    val rootCause: String,
    val solutionSteps: String,
    val partsReplaced: String,
    val estimatedSavingsOrCost: String,
    val mechanicName: String,
    val branchId: Int,
    val branchName: String,
    val helpfulCount: Int = 0,
    val isOwnerRewarded: Boolean = false,
    val rewardAmount: Long = 0L,
    val ownerNote: String = "",
    val ownerRewardedAt: Long? = null,
    val commentsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "solution_comments")
data class SolutionCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val branchName: String,
    val comment: String,
    val isOwner: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

