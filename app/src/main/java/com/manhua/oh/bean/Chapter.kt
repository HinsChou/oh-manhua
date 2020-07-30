package com.manhua.oh.bean

class Chapter {
    var id = 0 // 本地id

    var href = ""
    var title = ""
    var page = 0
    var prev = ""
    var next = ""
    var prefix = ""

    var pageId = "" // 服务器数据库章节id
    var dataId = "" // 服务器数据库漫画id
    var chapterId = "" // 服务器漫画章节id
}