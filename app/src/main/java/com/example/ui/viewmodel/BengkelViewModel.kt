package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BengkelDatabase
import com.example.data.model.Branch
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MechanicProfile
import com.example.data.model.SopDocument
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
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
        repository = BengkelRepository(db.ticketDao(), db.chatMessageDao())
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
    }

    // 18 Cabang BengkelKu
    val branches18: List<Branch> = repository.get18Branches()

    // Login & Active Mechanic State
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentMechanic = MutableStateFlow(
        MechanicProfile(
            name = "Budi Santoso",
            branchId = 1,
            branchName = "Cabang 1 - Montecarlo Solo",
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

    val allTickets: StateFlow<List<TicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTickets: StateFlow<List<TicketEntity>> = combine(
        repository.allTickets,
        _selectedFilter,
        _currentMechanic
    ) { tickets, filter, mechanic ->
        when (filter) {
            "MY_BRANCH" -> tickets.filter { it.branchId == mechanic.branchId }
            "URGENT" -> tickets.filter { it.urgency != TicketUrgency.NORMAL }
            "MESIN" -> tickets.filter { it.category.contains("Mesin", ignoreCase = true) }
            "MATIC" -> tickets.filter { it.category.contains("Matic", ignoreCase = true) || it.category.contains("Transmisi", ignoreCase = true) }
            "KELISTRIKAN" -> tickets.filter { it.category.contains("Kelistrikan", ignoreCase = true) || it.category.contains("Sensor", ignoreCase = true) }
            "OPEN" -> tickets.filter { it.status != TicketStatus.RESOLVED }
            "RESOLVED" -> tickets.filter { it.status == TicketStatus.RESOLVED }
            else -> tickets
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

    // Search Query for Quick Solutions
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun login(name: String, branchId: Int) {
        val branch = repository.getBranchById(branchId)
        val validName = if (name.isNotBlank()) name.trim() else "Mekanik"
        
        _currentMechanic.value = MechanicProfile(
            name = validName,
            branchId = branch.id,
            branchName = branch.name,
            phone = branch.phone
        )
        _isLoggedIn.value = true
    }

    fun logout() {
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
}
