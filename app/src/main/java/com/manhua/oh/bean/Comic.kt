package com.manhua.oh.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Comic() {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var title = "" // 标题

    @ColumnInfo
    var src = "" // 封面

    @ColumnInfo
    var href = "" // 链接

    @ColumnInfo
    var type = "" // 类型

    @ColumnInfo
    var author = "" // 作者

    @ColumnInfo
    var tags = "" // 标签

    @ColumnInfo
    var lastDate = "" // 最后日期

    @ColumnInfo
    var lastChapter = "" // 最后章节

    var lastHref = ""

    @ColumnInfo
    var chapter = 0 // 章节数

    @ColumnInfo
    var brief = ""

    @ColumnInfo
    var dataId = "" // 漫画id

    var readChapter = ""

    var readTime = ""

    var readHref = ""
}