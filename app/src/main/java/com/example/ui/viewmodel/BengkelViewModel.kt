package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BengkelDatabase
import com.example.data.model.Branch
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MechanicProfile
import com.example.data.model.SolutionCommentEntity
import com.example.data.model.SolutionPostEntity
import com.example.data.model.SopDocument
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
import com.example.data.model.UserRole
import com.example.data.repository.BengkelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BengkelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BengkelRepository

    init {
        val db = BengkelDatabase.getDatabase(application)
        repository = BengkelRepository(db.ticketDao(), db.chatMessageDao(), db.solutionDao())
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
    }

    // 18 Cabang BengkelKu
    val branches18: List<Branch> = repository.get18Branches()

    // Login & Active Mechanic State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentMechanic = MutableStateFlow(
        MechanicProfile(
            name = "Budi Santoso",
            branchId = 1,
            branchName = "Cabang 1 - Montecarlo Solo",
            role = UserRole.MECHANIC,
            phone = "62 821-4012-8796",
            solvedTicketsCount = 28,
            sharedSolutionsCount = 19
        )
    )
    val currentMechanic: StateFlow<MechanicProfile> = _currentMechanic.asStateFlow()

    val currentBranchId: StateFlow<Int> = _currentMechanic.map { it.branchId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // Filter in Dashboard / Forum: "ALL", "MY_BRANCH", "URGENT", "MESIN", "MATIC", "KELISTRIKAN"
    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Search Query for Tickets & Solutions
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Solution Posts (Wadah Kasus & Solusi Berhadiah Owner)
    private val _solutionFilter = MutableStateFlow("ALL") // "ALL", "REWARDED", "HELPFUL", "MESIN", "MATIC", "KELISTRIKAN", "AC"
    val solutionFilter: StateFlow<String> = _solutionFilter.asStateFlow()

    private val _solutionSearchQuery = MutableStateFlow("")
    val solutionSearchQuery: StateFlow<String> = _solutionSearchQuery.asStateFlow()

    val allSolutionPosts: StateFlow<List<SolutionPostEntity>> = repository.allSolutionPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSolutionPosts: StateFlow<List<SolutionPostEntity>> = combine(
        repository.allSolutionPosts,
        _solutionFilter,
        _solutionSearchQuery
    ) { posts, filter, query ->
        var list = if (query.isBlank()) {
            posts
        } else {
            posts.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.vehicleModel.contains(query, ignoreCase = true) ||
                it.vehicleBrand.contains(query, ignoreCase = true) ||
                it.dtcCode.contains(query, ignoreCase = true) ||
                it.symptomDescription.contains(query, ignoreCase = true) ||
                it.solutionSteps.contains(query, ignoreCase = true) ||
                it.partsReplaced.contains(query, ignoreCase = true)
            }
        }

        when (filter) {
            "REWARDED" -> list.filter { it.isOwnerRewarded }
            "HELPFUL" -> list.sortedByDescending { it.helpfulCount }
            "MESIN" -> list.filter { it.category.contains("Mesin", ignoreCase = true) || it.category.contains("Diesel", ignoreCase = true) }
            "MATIC" -> list.filter { it.category.contains("Matic", ignoreCase = true) || it.category.contains("Transmisi", ignoreCase = true) }
            "KELISTRIKAN" -> list.filter { it.category.contains("Kelistrikan", ignoreCase = true) || it.category.contains("Sensor", ignoreCase = true) }
            "AC" -> list.filter { it.category.contains("AC", ignoreCase = true) || it.category.contains("Pendingin", ignoreCase = true) }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selected Solution Post
    private val _activeSolutionId = MutableStateFlow<Long?>(null)
    val activeSolutionId: StateFlow<Long?> = _activeSolutionId.asStateFlow()

    private val _activeSolution = MutableStateFlow<SolutionPostEntity?>(null)
    val activeSolution: StateFlow<SolutionPostEntity?> = _activeSolution.asStateFlow()

    private val _solutionComments = MutableStateFlow<List<SolutionCommentEntity>>(emptyList())
    val solutionComments: StateFlow<List<SolutionCommentEntity>> = _solutionComments.asStateFlow()

    val totalOwnerBonusPaid: StateFlow<Long> = repository.allSolutionPosts.map { posts ->
        posts.filter { it.isOwnerRewarded }.sumOf { it.rewardAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 350000L)


    val allTickets: StateFlow<List<TicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTickets: StateFlow<List<TicketEntity>> = combine(
        repository.allTickets,
        _selectedFilter,
        _currentMechanic,
        _searchQuery
    ) { tickets, filter, mechanic, query ->
        val searchFiltered = if (query.isBlank()) {
            tickets
        } else {
            tickets.filter {
                it.vehicleModel.contains(query, ignoreCase = true) ||
                it.vehicleBrand.contains(query, ignoreCase = true) ||
                it.licensePlate.contains(query, ignoreCase = true) ||
                it.dtcCode.contains(query, ignoreCase = true) ||
                it.complaint.contains(query, ignoreCase = true) ||
                it.solutionSummary.contains(query, ignoreCase = true) ||
                it.ticketNumber.contains(query, ignoreCase = true) ||
                it.mechanicName.contains(query, ignoreCase = true) ||
                it.branchName.contains(query, ignoreCase = true)
            }
        }
        when (filter) {
            "MY_BRANCH" -> searchFiltered.filter { it.branchId == mechanic.branchId }
            "URGENT" -> searchFiltered.filter { it.urgency != TicketUrgency.NORMAL }
            "MESIN" -> searchFiltered.filter { it.category.contains("Mesin", ignoreCase = true) }
            "MATIC" -> searchFiltered.filter { it.category.contains("Matic", ignoreCase = true) || it.category.contains("Transmisi", ignoreCase = true) }
            "KELISTRIKAN" -> searchFiltered.filter { it.category.contains("Kelistrikan", ignoreCase = true) || it.category.contains("Sensor", ignoreCase = true) }
            "OPEN" -> searchFiltered.filter { it.status != TicketStatus.RESOLVED }
            "RESOLVED" -> searchFiltered.filter { it.status == TicketStatus.RESOLVED }
            else -> searchFiltered
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Ticket for Discussion Thread
    private val _activeTicketId = MutableStateFlow<Long?>(null)
    val activeTicketId: StateFlow<Long?> = _activeTicketId.asStateFlow()

    private val _activeTicket = MutableStateFlow<TicketEntity?>(null)
    val activeTicket: StateFlow<TicketEntity?> = _activeTicket.asStateFlow()

    private val _ticketMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val ticketMessages: StateFlow<List<ChatMessageEntity>> = _ticketMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    val searchResults: StateFlow<List<TicketEntity>> = combine(
        repository.allTickets,
        _searchQuery
    ) { tickets, query ->
        if (query.isBlank()) {
            tickets.filter { it.status == TicketStatus.RESOLVED }
        } else {
            tickets.filter {
                it.vehicleModel.contains(query, ignoreCase = true) ||
                it.vehicleBrand.contains(query, ignoreCase = true) ||
                it.dtcCode.contains(query, ignoreCase = true) ||
                it.complaint.contains(query, ignoreCase = true) ||
                it.solutionSummary.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SOP Library
    val sopList: List<SopDocument> = repository.getSopLibrary()
    private val _selectedSop = MutableStateFlow<SopDocument?>(null)
    val selectedSop: StateFlow<SopDocument?> = _selectedSop.asStateFlow()

    fun validateAdminPin(role: com.example.data.model.UserRole, pin: String): Boolean {
        return when (role) {
            com.example.data.model.UserRole.MECHANIC -> true
            com.example.data.model.UserRole.ADMIN_OWNER -> pin == "9988"
        }
    }

    fun login(name: String, branchId: Int, role: com.example.data.model.UserRole = com.example.data.model.UserRole.MECHANIC) {
        val branch = repository.getBranchById(branchId)
        val validName = if (name.isNotBlank()) name.trim() else when (role) {
            com.example.data.model.UserRole.ADMIN_OWNER -> "Pak Hendra (Owner / Admin)"
            else -> "Mekanik Cabang"
        }
        
        _currentMechanic.value = MechanicProfile(
            name = validName,
            branchId = branch.id,
            branchName = branch.name,
            role = role,
            phone = branch.phone
        )
        _selectedFilter.value = "ALL"
        _searchQuery.value = ""
        _solutionFilter.value = "ALL"
        _solutionSearchQuery.value = ""
        _isLoggedIn.value = true
    }

    fun logout() {
        _selectedFilter.value = "ALL"
        _searchQuery.value = ""
        _solutionFilter.value = "ALL"
        _solutionSearchQuery.value = ""
        _isLoggedIn.value = false
    }

    fun updateProfile(name: String, branchId: Int) {
        val branch = repository.getBranchById(branchId)
        _currentMechanic.value = _currentMechanic.value.copy(
            name = name.ifBlank { _currentMechanic.value.name },
            branchId = branch.id,
            branchName = branch.name,
            phone = branch.phone
        )
    }

    fun switchBranch(branchId: Int) {
        val branch = repository.getBranchById(branchId)
        _currentMechanic.value = _currentMechanic.value.copy(
            branchId = branch.id,
            branchName = branch.name
        )
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun selectTicket(ticketId: Long) {
        _activeTicketId.value = ticketId
        viewModelScope.launch {
            repository.observeTicket(ticketId).collect { ticket ->
                _activeTicket.value = ticket
            }
        }
        viewModelScope.launch {
            repository.observeMessages(ticketId).collect { messages ->
                _ticketMessages.value = messages
            }
        }
    }

    fun createNewTicket(
        brand: String,
        model: String,
        plate: String,
        year: String,
        dtc: String,
        complaint: String,
        urgency: TicketUrgency,
        category: String,
        photoUri: String?,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val mechanic = _currentMechanic.value
            val randomNum = (1000..9999).random()
            val ticketNumber = "#TKT-$randomNum"
            val newTicket = TicketEntity(
                ticketNumber = ticketNumber,
                branchId = mechanic.branchId,
                branchName = mechanic.branchName,
                mechanicName = mechanic.name,
                vehicleBrand = brand,
                vehicleModel = model,
                licensePlate = plate.uppercase(),
                year = year,
                dtcCode = dtc.uppercase(),
                complaint = complaint,
                urgency = urgency,
                category = category,
                status = TicketStatus.OPEN,
                photoUri = photoUri,
                createdAt = System.currentTimeMillis()
            )
            val id = repository.createTicket(newTicket)
            selectTicket(id)
            onSuccess(id)
        }
    }

    fun sendMechanicMessage(ticketId: Long, text: String) {
        if (text.isBlank()) return
        val mechanic = _currentMechanic.value
        viewModelScope.launch {
            repository.sendMechanicMessage(
                ticketId = ticketId,
                senderName = mechanic.name,
                branchName = mechanic.branchName,
                message = text,
                isCurrentUser = true
            )
        }
    }

    fun askAiAssistant(ticketId: Long, prompt: String = "") {
        viewModelScope.launch {
            _isAiGenerating.value = true
            try {
                repository.triggerAiResponse(ticketId, prompt)
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun markTicketResolved(ticketId: Long, solution: String) {
        viewModelScope.launch {
            repository.markTicketResolved(ticketId, solution)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSop(sop: SopDocument?) {
        _selectedSop.value = sop
    }

    // Solution Posts Methods
    fun setSolutionFilter(filter: String) {
        _solutionFilter.value = filter
    }

    fun setSolutionSearchQuery(query: String) {
        _solutionSearchQuery.value = query
    }

    fun selectSolutionPost(postId: Long) {
        _activeSolutionId.value = postId
        viewModelScope.launch {
            repository.observeSolutionPost(postId).collect { post ->
                _activeSolution.value = post
            }
        }
        viewModelScope.launch {
            repository.observeSolutionComments(postId).collect { comments ->
                _solutionComments.value = comments
            }
        }
    }

    fun createSolutionPost(
        title: String,
        brand: String,
        model: String,
        year: String,
        dtc: String,
        category: String,
        symptom: String,
        rootCause: String,
        steps: String,
        parts: String,
        costOrSavings: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val mechanic = _currentMechanic.value
            val newPost = SolutionPostEntity(
                title = title.ifBlank { "Solusi Kendala $brand $model ($dtc)" },
                vehicleBrand = brand,
                vehicleModel = model,
                year = year,
                dtcCode = dtc.uppercase(),
                category = category,
                symptomDescription = symptom,
                rootCause = rootCause,
                solutionSteps = steps,
                partsReplaced = parts,
                estimatedSavingsOrCost = costOrSavings,
                mechanicName = mechanic.name,
                branchId = mechanic.branchId,
                branchName = mechanic.branchName,
                helpfulCount = 1,
                isOwnerRewarded = false,
                rewardAmount = 0L,
                ownerNote = "",
                commentsCount = 0,
                createdAt = System.currentTimeMillis()
            )
            val id = repository.createSolutionPost(newPost)
            selectSolutionPost(id)
            onSuccess(id)
        }
    }

    fun upvoteSolutionHelpful(postId: Long) {
        viewModelScope.launch {
            repository.upvoteHelpful(postId)
        }
    }

    fun rewardSolutionPostByOwner(postId: Long, amount: Long, ownerNote: String) {
        viewModelScope.launch {
            val note = if (ownerNote.isNotBlank()) ownerNote else "Solusi sangat aplikatif dan bermanfaat untuk 18 cabang."
            repository.rewardSolutionPost(postId, amount, note)
            
            // If the reward is given to current mechanic, increase their totalBonusEarned
            val current = _activeSolution.value
            if (current != null && current.mechanicName == _currentMechanic.value.name) {
                _currentMechanic.value = _currentMechanic.value.copy(
                    totalBonusEarned = _currentMechanic.value.totalBonusEarned + amount
                )
            }
        }
    }

    fun addSolutionComment(postId: Long, text: String, isOwner: Boolean = false) {
        if (text.isBlank()) return
        val mechanic = _currentMechanic.value
        val authorName = if (isOwner) "Pak Hendra (Owner Bengkel)" else mechanic.name
        val branchName = if (isOwner) "Pusat Manajemen" else mechanic.branchName
        viewModelScope.launch {
            repository.addSolutionComment(
                postId = postId,
                authorName = authorName,
                branchName = branchName,
                commentText = text,
                isOwner = isOwner
            )
        }
    }
}

