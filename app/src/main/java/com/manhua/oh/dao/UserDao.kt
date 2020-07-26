package com.manhua.oh.dao

import androidx.room.*
import com.manhua.oh.bean.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Query("select * from User where cookie != ''")
    fun queryLogin(): List<User>

    @Query("select * from User where :key == :value")
    fun queryUsers(key: String, value: String): List<User>

    @Update
    fun update(user: User)
}