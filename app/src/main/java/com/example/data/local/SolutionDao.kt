package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SolutionCommentEntity
import com.example.data.model.SolutionPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolutionDao {
    @Query("SELECT * FROM solution_posts ORDER BY isOwnerRewarded DESC, createdAt DESC")
    fun getAllSolutionPosts(): Flow<List<SolutionPostEntity>>

    @Query("SELECT * FROM solution_posts WHERE isOwnerRewarded = 1 ORDER BY rewardAmount DESC")
    fun getRewardedPosts(): Flow<List<SolutionPostEntity>>

    @Query("SELECT * FROM solution_posts WHERE id = :id")
    fun observePostById(id: Long): Flow<SolutionPostEntity?>

    @Query("SELECT * FROM solution_posts WHERE id = :id")
    suspend fun getPostById(id: Long): SolutionPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SolutionPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPosts(posts: List<SolutionPostEntity>)

    @Update
    suspend fun updatePost(post: SolutionPostEntity)

    @Query("UPDATE solution_posts SET helpfulCount = helpfulCount + 1 WHERE id = :id")
    suspend fun incrementHelpful(id: Long)

    @Query("UPDATE solution_posts SET isOwnerRewarded = 1, rewardAmount = :amount, ownerNote = :note, ownerRewardedAt = :timestamp WHERE id = :id")
    suspend fun rewardPostByOwner(id: Long, amount: Long, note: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM solution_posts WHERE title LIKE '%' || :query || '%' OR vehicleModel LIKE '%' || :query || '%' OR dtcCode LIKE '%' || :query || '%' OR symptomDescription LIKE '%' || :query || '%' OR solutionSteps LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSolutionPosts(query: String): Flow<List<SolutionPostEntity>>

    // Comments
    @Query("SELECT * FROM solution_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<SolutionCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: SolutionCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllComments(comments: List<SolutionCommentEntity>)

    @Query("UPDATE solution_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: Long)
}
