package com.manhua.oh.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.manhua.oh.bean.Comic
import com.manhua.oh.bean.Record
import com.manhua.oh.bean.User

@Database(entities = [User::class, Record::class, Comic::class], version = 1)
abstract class OhDatabase : RoomDatabase() {

    companion object {
        lateinit var db: OhDatabase

        fun init(application: Context) {
            db = Room.databaseBuilder(application.applicationContext, OhDatabase::class.java, application.packageName)
                    .allowMainThreadQueries().build()
        }
    }
    abstract fun ohDao() : OhDao

    fun getLogin(): User {
        val users = ohDao().queryLogin()
        return if (users.isEmpty())
            User()
        else
            users[0]
    }

    fun getRecordChapter(dataId: String, chapterId: String): Record {
        val user = getLogin()
        val records = ohDao().queryRecordByChapter(user.userId, dataId, chapterId)
        return if (records.isEmpty())
            Record()
        else
            records[0]
    }

    fun getRecordComic(dataId: String): Record {
        val user = getLogin()
        val records = ohDao().queryRecordByComic(user.userId, dataId)
        return if (records.isEmpty())
            Record()
        else
            records[0]
    }

    fun getComic(dataId: String) : Comic{
        val comics = ohDao().queryComic(dataId)
        return if (comics.isEmpty())
            Comic()
        else
            comics[0]
    }

}