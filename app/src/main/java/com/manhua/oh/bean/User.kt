package com.manhua.oh.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var userId = ""

    @ColumnInfo
    var cookie = ""

    @ColumnInfo
    var nickName = ""

    @ColumnInfo
    var username = ""

    @ColumnInfo
    var password = ""

    @ColumnInfo
    var likes = ""

    override fun toString(): String {
        return "User(userId='$userId', cookie='$cookie', nickName='$nickName')"
    }


}