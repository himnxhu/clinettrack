package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.Customer
import com.example.data.models.Milestone
import com.example.data.models.PaymentRecord
import com.example.data.repository.ClientTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ClientTrackViewModel(
    application: Application,
    private val repository: ClientTrackRepository
) : AndroidViewModel(application) {

    // Tab state
    private val _activeTab = MutableStateFlow("dashboard")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    private val prefs = application.getSharedPreferences("client_track_prefs", android.content.Context.MODE_PRIVATE)

    private val _isSignedUp = MutableStateFlow(prefs.getBoolean("is_signed_up", false))
    val isSignedUp: StateFlow<Boolean> = _isSignedUp.asStateFlow()

    private val _ownerName = MutableStateFlow(prefs.getString("owner_name", "") ?: "")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _ownerEmail = MutableStateFlow(prefs.getString("owner_email", "") ?: "")
    val ownerEmail: StateFlow<String> = _ownerEmail.asStateFlow()

    private val _ownerWhatsapp = MutableStateFlow(prefs.getString("owner_whatsapp", "") ?: "")
    val ownerWhatsapp: StateFlow<String> = _ownerWhatsapp.asStateFlow()

    private val _businessType = MutableStateFlow(prefs.getString("business_type", "Gym / Fitness") ?: "Gym / Fitness")
    val businessType: StateFlow<String> = _businessType.asStateFlow()

    private val _defaultFee = MutableStateFlow(prefs.getFloat("default_fee", 1500f).toDouble())
    val defaultFee: StateFlow<Double> = _defaultFee.asStateFlow()

    private val _feeDueDay = MutableStateFlow(prefs.getInt("fee_due_day", 5))
    val feeDueDay: StateFlow<Int> = _feeDueDay.asStateFlow()

    // Business info
    private val _businessName = MutableStateFlow(prefs.getString("business_name", "Apex Gym & Coaching") ?: "Apex Gym & Coaching")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    fun updateBusinessName(newName: String) {
        if (newName.isNotBlank()) {
            _businessName.value = newName
            prefs.edit().putString("business_name", newName).apply()
        }
    }

    fun completeSignup(
        ownerName: String,
        ownerEmail: String,
        passwordPlain: String,
        whatsapp: String,
        bizType: String,
        bizName: String,
        defFee: Double,
        dueDay: Int
    ) {
        _ownerName.value = ownerName
        _ownerEmail.value = ownerEmail
        _ownerWhatsapp.value = whatsapp
        _businessType.value = bizType
        _businessName.value = bizName
        _defaultFee.value = defFee
        _feeDueDay.value = dueDay
        _isSignedUp.value = true

        prefs.edit()
            .putString("owner_name", ownerName)
            .putString("owner_email", ownerEmail)
            .putString("owner_password", passwordPlain)
            .putString("owner_whatsapp", whatsapp)
            .putString("business_type", bizType)
            .putString("business_name", bizName)
            .putFloat("default_fee", defFee.toFloat())
            .putInt("fee_due_day", dueDay)
            .putBoolean("is_signed_up", true)
            .apply()
    }

    fun logout() {
        _isSignedUp.value = false
        prefs.edit().putBoolean("is_signed_up", false).apply()
    }

    // Active subscription plan state
    private val _currentPlan = MutableStateFlow("Free Trial") // Starts on Free Trial
    val currentPlan: StateFlow<String> = _currentPlan.asStateFlow()

    private val _trialDaysLeft = MutableStateFlow(10) // 10 days remaining of 14
    val trialDaysLeft: StateFlow<Int> = _trialDaysLeft.asStateFlow()

    fun upgradePlan(planName: String) {
        _currentPlan.value = planName
    }

    fun resetTrial() {
        _currentPlan.value = "Free Trial"
        _trialDaysLeft.value = 14
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val businessTypeFilter = MutableStateFlow("All")

    // Selected customer for detail sheet
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    // Reactively load current customer's payments and milestones
    val selectedCustomerPayments: Flow<List<PaymentRecord>> = _selectedCustomer
        .flatMapLatest { customer ->
            if (customer != null) repository.getPaymentsForCustomer(customer.id)
            else flowOf(emptyList())
        }

    val selectedCustomerMilestones: Flow<List<Milestone>> = _selectedCustomer
        .flatMapLatest { customer ->
            if (customer != null) repository.getMilestonesForCustomer(customer.id)
            else flowOf(emptyList())
        }

    fun getMilestonesForCustomer(customerId: Int): Flow<List<Milestone>> {
        return repository.getMilestonesForCustomer(customerId)
    }

    // Reactive lists from Repository
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentRecord>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered customer list
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        customers,
        searchQuery,
        businessTypeFilter
    ) { customerList, query, filterType ->
        customerList.filter { customer ->
            val matchesQuery = customer.name.contains(query, ignoreCase = true) ||
                    customer.phone.contains(query, ignoreCase = true) ||
                    customer.email.contains(query, ignoreCase = true)
            val matchesFilter = filterType == "All" || customer.businessType == filterType
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overdue fees computation (currentTime is in millis)
    val overdueCustomers: StateFlow<List<Customer>> = customers
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.nextDueDateMillis < now }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming due (within next 7 days, and not already overdue)
    val upcomingCustomers: StateFlow<List<Customer>> = customers
        .map { list ->
            val now = System.currentTimeMillis()
            val sevenDaysFromNow = now + (7L * 24 * 60 * 60 * 1000)
            list.filter { it.nextDueDateMillis in (now + 1)..sevenDaysFromNow }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial calculations
    val totalRevenue: StateFlow<Double> = payments
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPendingFeesAmount: StateFlow<Double> = overdueCustomers
        .map { list -> list.sumOf { it.feeAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Actions
    fun addCustomer(
        name: String,
        phone: String,
        email: String,
        joinDateMillis: Long,
        businessType: String,
        feePlanName: String,
        feeAmount: Double,
        notes: String,
        nextDueDateMillis: Long
    ) {
        viewModelScope.launch {
            val customer = Customer(
                name = name,
                phone = phone,
                email = email,
                joinDateMillis = joinDateMillis,
                businessType = businessType,
                feePlanName = feePlanName,
                feeAmount = feeAmount,
                notes = notes,
                nextDueDateMillis = nextDueDateMillis
            )
            val newId = repository.insertCustomer(customer).toInt()

            // Automatically populate typical progress milestones based on selected Business Type
            val defaultMilestones = when (businessType) {
                "Gym / Fitness" -> listOf(
                    "Onboarding & Body Assessment Done",
                    "Week 1 Evaluation Complete",
                    "Month 1 Review & Goal Adjustment",
                    "3-Month Progression Milestone Reached"
                )
                "Coaching / Classes" -> listOf(
                    "Joined & Syllabus Shared",
                    "First Assessment Passed",
                    "Mid-term Review Done",
                    "Final Certifications / Portfolio Complete"
                )
                "Beauty / Academy" -> listOf(
                    "Kit Issued & Basics Understood",
                    "Practical Assignment 1 Passed",
                    "Live Session Feedback Acquired",
                    "Graduation Showcase Completed"
                )
                else -> listOf(
                    "Onboarding Completed",
                    "First Month Check-In Accomplished",
                    "Mid-Cycle Target Met",
                    "Renewal & Progress Evaluated"
                )
            }

            for (title in defaultMilestones) {
                repository.insertMilestone(
                    Milestone(
                        customerId = newId,
                        title = title,
                        isCompleted = false
                    )
                )
            }
        }
    }

    fun updateCustomerNextDueDate(customer: Customer, newDueDateMillis: Long) {
        viewModelScope.launch {
            val updated = customer.copy(nextDueDateMillis = newDueDateMillis)
            repository.updateCustomer(updated)
            // If the customer is currently selected, refresh the selection state
            if (_selectedCustomer.value?.id == customer.id) {
                _selectedCustomer.value = updated
            }
        }
    }

    fun updateCustomerDetails(
        id: Int,
        name: String,
        phone: String,
        email: String,
        businessType: String,
        feePlanName: String,
        feeAmount: Double,
        notes: String,
        nextDueDateMillis: Long
    ) {
        viewModelScope.launch {
            val updated = Customer(
                id = id,
                name = name,
                phone = phone,
                email = email,
                joinDateMillis = System.currentTimeMillis(), // keep past or current
                businessType = businessType,
                feePlanName = feePlanName,
                feeAmount = feeAmount,
                notes = notes,
                nextDueDateMillis = nextDueDateMillis
            )
            repository.updateCustomer(updated)
            if (_selectedCustomer.value?.id == id) {
                _selectedCustomer.value = updated
            }
        }
    }

    fun recordPayment(customerId: Int, amount: Double, notes: String) {
        viewModelScope.launch {
            val payment = PaymentRecord(
                customerId = customerId,
                amount = amount,
                paymentDateMillis = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertPayment(payment)

            // Auto-extend next due date if we can find the customer
            val existingCustomerList = customers.value
            val target = existingCustomerList.find { it.id == customerId }
            if (target != null) {
                // Determine length to extend based on fee plan name (monthly, quarterly, annual)
                val cal = Calendar.getInstance()
                cal.timeInMillis = Math.max(System.currentTimeMillis(), target.nextDueDateMillis)
                when (target.feePlanName.lowercase()) {
                    "quarterly" -> cal.add(Calendar.MONTH, 3)
                    "annually", "annual" -> cal.add(Calendar.YEAR, 1)
                    else -> cal.add(Calendar.MONTH, 1) // Default to Monthly extension
                }
                
                val updatedCustomer = target.copy(
                    nextDueDateMillis = cal.timeInMillis
                )
                repository.updateCustomer(updatedCustomer)
                
                // Refresh selection state if loaded
                if (_selectedCustomer.value?.id == customerId) {
                    _selectedCustomer.value = updatedCustomer
                }
            }
        }
    }

    fun toggleMilestoneSelection(milestone: Milestone) {
        viewModelScope.launch {
            val updated = milestone.copy(
                isCompleted = !milestone.isCompleted,
                completedDateMillis = if (!milestone.isCompleted) System.currentTimeMillis() else 0L
            )
            repository.updateMilestone(updated)
        }
    }

    fun addCustomMilestone(customerId: Int, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val milestone = Milestone(
                customerId = customerId,
                title = title,
                isCompleted = false
            )
            repository.insertMilestone(milestone)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            if (_selectedCustomer.value?.id == customer.id) {
                _selectedCustomer.value = null
            }
        }
    }

    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.deleteMilestoneById(milestone.id)
        }
    }
}

class ClientTrackViewModelFactory(
    private val application: Application,
    private val repository: ClientTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientTrackViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
