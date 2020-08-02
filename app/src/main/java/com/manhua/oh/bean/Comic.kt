package com.manhua.oh.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Comic {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var title = "" // 标题

    @ColumnInfo
    var src = "" // 封面地址

    @ColumnInfo
    var href = "" // 页面地址

    @ColumnInfo
    var type = "" // 类型

    @ColumnInfo
    var author = "" // 作者

    @ColumnInfo
    var tags = "" // 标签

    @ColumnInfo
    var lastDate = "" // 最后日期

    @ColumnInfo
    var lastChapter = "" // 最新章节

    @ColumnInfo
    var lastHref = "" // 最新章节地址

    @ColumnInfo
    var brief = "" // 简介

    @ColumnInfo
    var dataId = "" // 漫画id

    @ColumnInfo
    var dataLongId = "" // 取消收藏的id
}