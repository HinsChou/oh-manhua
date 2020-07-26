package com.manhua.oh.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Record {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var userId = "" // 用户id

    @ColumnInfo
    var dataId = "" // 漫画id

    @ColumnInfo
    var chapterId = "1" // 章节id

    @ColumnInfo
    var timestamp = 0L

    @ColumnInfo
    var page = 0
}