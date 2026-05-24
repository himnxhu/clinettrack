package com.example.data.database

import androidx.room.*
import com.example.data.models.Customer
import com.example.data.models.Milestone
import com.example.data.models.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientTrackDao {
    // Customers
    @Query("SELECT * FROM customers ORDER BY joinDateMillis DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Int): Flow<Customer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Payments
    @Query("SELECT * FROM payment_records WHERE customerId = :customerId ORDER BY paymentDateMillis DESC")
    fun getPaymentsForCustomer(customerId: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records ORDER BY paymentDateMillis DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord)

    @Query("DELETE FROM payment_records WHERE customerId = :customerId")
    suspend fun deletePaymentsForCustomer(customerId: Int)

    // Milestones
    @Query("SELECT * FROM milestones WHERE customerId = :customerId ORDER BY id ASC")
    fun getMilestonesForCustomer(customerId: Int): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Query("DELETE FROM milestones WHERE customerId = :customerId")
    suspend fun deleteMilestonesForCustomer(customerId: Int)

    @Query("DELETE FROM milestones WHERE id = :milestoneId")
    suspend fun deleteMilestoneById(milestoneId: Int)
}
