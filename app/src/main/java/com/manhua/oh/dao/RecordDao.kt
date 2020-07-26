package com.manhua.oh.dao

import androidx.room.*
import com.manhua.oh.bean.Record

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: Record): Long

    @Query("select * from Record where userId == :userId and dataId == :dataId and chapterId == :chapterId")
    fun queryChapter(userId: String, dataId: String, chapterId: String): List<Record>

    @Query("select * from Record where userId == :userId and dataId == :dataId order by timestamp desc")
    fun queryComic(userId: String, dataId: String): List<Record>

    @Update
    fun update(user: Record)
}