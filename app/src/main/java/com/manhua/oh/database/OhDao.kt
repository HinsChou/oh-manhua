package com.manhua.oh.database

import androidx.room.*
import com.manhua.oh.bean.Comic
import com.manhua.oh.bean.Record
import com.manhua.oh.bean.User

@Dao
interface OhDao {
    // user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Long

    @Query("select * from User where cookie != ''")
    fun queryLogin(): List<User>

    @Update
    fun updateUser(user: User)

    // comic
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertComic(comic : Comic)

    @Query("select * from Comic where dataId == :dataId")
    fun queryComic(dataId : String) : List<Comic>

    // record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(user: Record): Long

    @Query("select * from Record where userId == :userId and dataId == :dataId and chapterId == :chapterId")
    fun queryRecordByChapter(userId: String, dataId: String, chapterId: String): List<Record>

    @Query("select * from Record where userId == :userId and dataId == :dataId order by timestamp desc")
    fun queryRecordByComic(userId: String, dataId: String): List<Record>

    @Update
    fun updateRecord(record: Record)
}