package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE status != 'RESOLVED' ORDER BY createdAt DESC")
    fun getOpenTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE branchId = :branchId ORDER BY createdAt DESC")
    fun getTicketsByBranch(branchId: Int): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT * FROM tickets WHERE id = :id")
    fun observeTicketById(id: Long): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets WHERE status = 'RESOLVED' ORDER BY resolvedAt DESC")
    fun getResolvedTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE (vehicleModel LIKE '%' || :query || '%' OR dtcCode LIKE '%' || :query || '%' OR complaint LIKE '%' || :query || '%' OR solutionSummary LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchTickets(query: String): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTickets(tickets: List<TicketEntity>)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET repliesCount = repliesCount + 1 WHERE id = :ticketId")
    suspend fun incrementRepliesCount(ticketId: Long)

    @Query("UPDATE tickets SET status = 'RESOLVED', solutionSummary = :solution, resolvedAt = :resolvedAt WHERE id = :ticketId")
    suspend fun markResolved(ticketId: Long, solution: String, resolvedAt: Long = System.currentTimeMillis())
}
