package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.SolutionCommentEntity
import com.example.data.model.SolutionPostEntity
import com.example.data.model.TicketEntity

@Database(
    entities = [TicketEntity::class, ChatMessageEntity::class, SolutionPostEntity::class, SolutionCommentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BengkelDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun solutionDao(): SolutionDao

    companion object {
        @Volatile
        private var INSTANCE: BengkelDatabase? = null

        fun getDatabase(context: Context): BengkelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BengkelDatabase::class.java,
                    "bengkelku_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
