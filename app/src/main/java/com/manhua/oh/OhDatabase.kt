package com.manhua.oh

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.manhua.oh.bean.Record
import com.manhua.oh.bean.User
import com.manhua.oh.dao.RecordDao
import com.manhua.oh.dao.UserDao

@Database(entities = [User::class, Record::class], version = 1)
abstract class OhDatabase : RoomDatabase() {

    companion object {
        lateinit var db: OhDatabase

        fun init(application: Context) {
            db = Room.databaseBuilder(application.applicationContext, OhDatabase::class.java, application.packageName)
                    .allowMainThreadQueries().build()
        }
    }

    abstract fun userDao(): UserDao
    abstract fun recordDao(): RecordDao

    fun getLogin(): User {
        val users = userDao().queryLogin()
        return if (users.isEmpty())
            User()
        else
            users[0]
    }

    fun getRecordChapter(dataId: String, chapterId: String): Record {
        val user = getLogin()
        val records = recordDao().queryChapter(user.userId, dataId, chapterId)
        return if (records.isEmpty())
            Record()
        else
            records[0]
    }

    fun getRecordComic(dataId: String): Record {
        val user = getLogin()
        val records = recordDao().queryComic(user.userId, dataId)
        return if (records.isEmpty())
            Record()
        else
            records[0]
    }
}