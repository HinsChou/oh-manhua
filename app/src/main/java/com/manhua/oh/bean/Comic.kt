package com.manhua.oh.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Comic() : Parcelable {
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

    @ColumnInfo
    var chapter = 0 // 章节数

    @ColumnInfo
    var brief = ""

    @ColumnInfo
    var dataId = "" // 漫画id

    var vote = 0 // 评分人数
    var rate = 0f // 评分

    constructor(parcel: Parcel) : this() {
        vote = parcel.readInt()
        title = parcel.readString()
        src = parcel.readString()
        rate = parcel.readFloat()
        href = parcel.readString()
        type = parcel.readString()
        author = parcel.readString()
        tags = parcel.readString()
        lastDate = parcel.readString()
        lastChapter = parcel.readString()
        chapter = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(vote)
        parcel.writeString(title)
        parcel.writeString(src)
        parcel.writeFloat(rate)
        parcel.writeString(href)
        parcel.writeString(type)
        parcel.writeString(author)
        parcel.writeString(tags)
        parcel.writeString(lastDate)
        parcel.writeString(lastChapter)
        parcel.writeInt(chapter)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comic> {
        override fun createFromParcel(parcel: Parcel): Comic {
            return Comic(parcel)
        }

        override fun newArray(size: Int): Array<Comic?> {
            return arrayOfNulls(size)
        }
    }
}