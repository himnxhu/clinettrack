package com.example.data.repository

import com.example.data.database.ClientTrackDao
import com.example.data.models.Customer
import com.example.data.models.Milestone
import com.example.data.models.PaymentRecord
import kotlinx.coroutines.flow.Flow

class ClientTrackRepository(private val dao: ClientTrackDao) {

    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allPayments: Flow<List<PaymentRecord>> = dao.getAllPayments()

    fun getCustomerById(customerId: Int): Flow<Customer?> = dao.getCustomerById(customerId)

    fun getPaymentsForCustomer(customerId: Int): Flow<List<PaymentRecord>> = dao.getPaymentsForCustomer(customerId)

    fun getMilestonesForCustomer(customerId: Int): Flow<List<Milestone>> = dao.getMilestonesForCustomer(customerId)

    suspend fun insertCustomer(customer: Customer): Long = dao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) {
        // Cascade delete payments and milestones
        dao.deletePaymentsForCustomer(customer.id)
        dao.deleteMilestonesForCustomer(customer.id)
        dao.deleteCustomer(customer)
    }

    suspend fun insertPayment(payment: PaymentRecord) = dao.insertPayment(payment)

    suspend fun deletePaymentsForCustomer(customerId: Int) = dao.deletePaymentsForCustomer(customerId)

    suspend fun insertMilestone(milestone: Milestone) = dao.insertMilestone(milestone)

    suspend fun updateMilestone(milestone: Milestone) = dao.updateMilestone(milestone)

    suspend fun deleteMilestonesForCustomer(customerId: Int) = dao.deleteMilestonesForCustomer(customerId)

    suspend fun deleteMilestoneById(milestoneId: Int) = dao.deleteMilestoneById(milestoneId)
}
